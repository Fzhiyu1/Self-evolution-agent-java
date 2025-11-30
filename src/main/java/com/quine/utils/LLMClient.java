package com.quine.utils;

import com.google.gson.*;
import okhttp3.*;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class LLMClient {
    private static final Properties CONFIG = loadConfig();
    private static final String API_URL = CONFIG.getProperty("API_URL", "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions");
    private static final String API_KEY = CONFIG.getProperty("API_KEY");
    private static final String MODEL = CONFIG.getProperty("MODEL", "qwen3-coder-plus");

    private static Properties loadConfig() {
        Properties props = new Properties();
        try {
            File configFile = new File("config.properties");
            if (configFile.exists()) {
                props.load(new FileInputStream(configFile));
            }
        } catch (IOException e) {
            System.err.println("Warning: Failed to load config.properties");
        }
        // API_KEY 可以从环境变量覆盖
        String envKey = System.getenv("API_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            props.setProperty("API_KEY", envKey);
        }
        return props;
    }

    private final OkHttpClient client;
    private final Gson gson;

    public LLMClient() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
        this.gson = new Gson();
    }

    /**
     * 调用 LLM 进行代码变异
     * @param sourceCode 当前源代码
     * @return 变异后的源代码
     */
    public String mutateCode(String sourceCode) throws IOException {
        String prompt = buildPrompt(sourceCode);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);

        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 1.0);

        Request request = new Request.Builder()
            .url(API_URL)
            .header("Authorization", "Bearer " + API_KEY)
            .header("Content-Type", "application/json")
            .post(RequestBody.create(
                gson.toJson(requestBody),
                MediaType.parse("application/json")
            ))
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("LLM API 调用失败: " + response.code() + " " + response.body().string());
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            String content = jsonResponse
                .getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

            return cleanCode(content);
        }
    }

    private String buildPrompt(String sourceCode) {
        return """
            你是一个算法优化专家。请对以下排序代码进行改进。

            改进策略（按优先级）：
            1. 可以对现有算法进行优化（如添加提前退出、减少比较次数、减少交换次数）
            2. 可以尝试相近的算法变体（如冒泡排序→鸡尾酒排序→梳排序）
            3. 可以逐步引入更高效的思想（如引入"间隔"概念、分区思想、递归分治）
            4. 如果当前算法已经很难优化，可以尝试更高效的排序算法

            注意：
            - 每次改动要有明确的优化意图
            - 尽量渐进式改进，避免一步跳跃太大
            - 不要添加任何注释

            严格约束：
            1. 必须实现 TaskSolver 接口
            2. 类名必须保持为 TargetSubject
            3. 包名必须是 com.quine.sandbox
            4. 【重要】绝对禁止使用 java.util.Arrays、java.util.Collections 等标准库的排序方法
            5. 只能使用基础 JDK 类（如 System、Math），不要引入第三方依赖
            6. 只返回完整的 Java 代码，不要 Markdown 代码块，不要解释

            当前代码：
            """ + sourceCode + """

            请进行优化，直接返回完整的 Java 代码：
            """;
    }

    /**
     * 清洗 LLM 返回的代码（去除 Markdown 包裹）
     */
    private String cleanCode(String code) {
        // 去除 ```java 和 ``` 包裹
        code = code.replaceAll("```java\\s*", "");
        code = code.replaceAll("```\\s*$", "");
        code = code.trim();
        return code;
    }
}

package com.quine;

import com.quine.core.Evaluator;
import com.quine.utils.LLMClient;

import java.io.IOException;
import java.nio.file.*;
import java.util.Random;

public class Main {
    private static final String TARGET_FILE = "src/main/java/com/quine/sandbox/TargetSubject.java";
    private static final String BACKUP_DIR = "target/generations";

    // 测试数据规模
    private static final int TEST_DATA_SIZE = 10000;     // 小数据集，让冒泡排序能跑完

    private static final int MAX_GENERATIONS = 200;

    public static void main(String[] args) {
        System.out.println("🧬 Project Quine: 自进化算法引擎启动");
        System.out.println("=" .repeat(60));

        try {
            // 0. 清空并重建备份目录
            Path backupPath = Paths.get(BACKUP_DIR);
            if (Files.exists(backupPath)) {
                // 删除旧的变异记录
                Files.walk(backupPath)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        try { Files.delete(p); } catch (IOException ignored) {}
                    });
            }
            Files.createDirectories(backupPath);

            // 1. 生成测试数据
            int[] testData = generateTestData(TEST_DATA_SIZE);
            System.out.println("✓ 生成测试数据: " + TEST_DATA_SIZE + " 个随机整数");

            // 2. 评估初始代码
            String currentCode = readSourceFile();
            Evaluator evaluator = new Evaluator();

            Evaluator.EvalResult baseline = evaluator.evaluate(currentCode, testData);

            if (!baseline.success) {
                System.err.println("✗ 初始代码评估失败: " + baseline.error);
                return;
            }

            long baselineTime = baseline.timeUs;
            System.out.println("✓ 初始基准: " + formatTime(baselineTime));
            System.out.println("=" .repeat(60));

            // 3. 进化循环
            LLMClient llmClient = new LLMClient();
            int generation = 1;

            while (generation <= MAX_GENERATIONS) {
                System.out.println("\n[Gen " + generation + "] 开始变异...");

                try {
                    // 变异
                    String mutatedCode = llmClient.mutateCode(currentCode);
                    System.out.println("[Gen " + generation + "] LLM 变异完成");

                    // 评估
                    Evaluator.EvalResult result = evaluator.evaluate(mutatedCode, testData);

                    if (!result.success) {
                        System.out.println("[Gen " + generation + "] ✗ 失败: " + result.error);
                        generation++;
                        continue;
                    }

                    System.out.println("[Gen " + generation + "] ✓ 编译成功，性能: " + formatTime(result.timeUs));

                    // 自然选择（允许 5% 以内的性能波动）
                    double tolerance = 1.05;  // 允许性能下降 5%
                    if (result.timeUs <= baselineTime * tolerance) {
                        boolean isImprovement = result.timeUs < baselineTime;
                        long diff = Math.abs(baselineTime - result.timeUs);
                        double ratio = (double) baselineTime / result.timeUs;

                        System.out.println("=" .repeat(60));
                        if (isImprovement) {
                            System.out.println("🎉 进化成功！");
                            System.out.println("   提升: " + formatTime(diff) + " (" + String.format("%.2fx", ratio) + " 倍速)");
                        } else {
                            System.out.println("🔄 中性变异（性能相近，保留多样性）");
                            System.out.println("   差异: +" + formatTime(diff) + " (" + String.format("%.1f%%", (ratio - 1) * -100) + ")");
                        }
                        System.out.println("   新基准: " + formatTime(result.timeUs));
                        System.out.println("=" .repeat(60));

                        // 备份当前代码
                        backupCode(generation, mutatedCode);

                        // 替换源文件
                        writeSourceFile(mutatedCode);
                        currentCode = mutatedCode;
                        baselineTime = result.timeUs;
                    } else {
                        double regression = ((double) result.timeUs / baselineTime - 1) * 100;
                        System.out.println("[Gen " + generation + "] 性能倒退 (" + String.format("+%.1f%%", regression) + ")，丢弃变异");
                    }

                } catch (Exception e) {
                    System.out.println("[Gen " + generation + "] ✗ 异常: " + e.getMessage());
                }

                generation++;
            }

            System.out.println("\n" + "=" .repeat(60));
            System.out.println("进化完成！最终性能: " + formatTime(baselineTime));

        } catch (Exception e) {
            System.err.println("系统错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int[] generateTestData(int size) {
        Random random = new Random(42);
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = random.nextInt(100000);
        }
        return data;
    }

    private static String readSourceFile() throws IOException {
        return Files.readString(Paths.get(TARGET_FILE));
    }

    private static void writeSourceFile(String content) throws IOException {
        Files.writeString(Paths.get(TARGET_FILE), content);
    }

    private static void backupCode(int generation, String code) throws IOException {
        String backupFile = BACKUP_DIR + "/gen_" + generation + "_TargetSubject.java";
        Files.writeString(Paths.get(backupFile), code);
        System.out.println("   备份: " + backupFile);
    }

    /**
     * 格式化时间显示（微秒 -> 毫秒或微秒）
     */
    private static String formatTime(long timeUs) {
        if (timeUs >= 1000) {
            return String.format("%.2f ms", timeUs / 1000.0);
        } else {
            return timeUs + " μs";
        }
    }
}

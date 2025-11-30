# Self-Evolution Agent Java

一个实验性的 Java 自进化算法引擎，通过 LLM 驱动的代码变异和自然选择机制，使排序算法在运行时自动从 O(n²) 进化为 O(n log n)。

## 项目概述

本项目（代号 Project Quine）构建了一个 Java 运行时环境，实现代码的自我进化。系统通过 LLM 对源代码进行变异，然后通过动态编译、热加载和性能评估，自动选择更优的实现，整个过程无需重启 JVM。

### 核心特性

- **动态编译**: 使用 `javax.tools.JavaCompiler` API 在内存中编译 Java 源码
- **热加载**: 每代变异创建新的 `URLClassLoader` 实例，实现类的热替换
- **LLM 变异**: 集成阿里云 Qwen API，通过大语言模型生成代码变异
- **自然选择**: 只有性能更优且正确性验证通过的变异才会被保留

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                     进化循环 (Evolution Loop)                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐             │
│   │ Mutator  │───>│ Compiler │───>│Evaluator │             │
│   │ (LLM)    │    │ (动态)   │    │ (选择)   │             │
│   └──────────┘    └──────────┘    └──────────┘             │
│        │                               │                    │
│        │         ┌──────────┐          │                    │
│        └────────>│ 源代码   │<─────────┘                    │
│                  │ (基因)   │   (性能更优则替换)            │
│                  └──────────┘                               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 项目结构

```
Self-evolution-agent-java/
├── src/main/java/com/quine/
│   ├── Main.java                 # 主程序入口，进化循环控制
│   ├── ResetToOrigin.java        # 重置工具，恢复初始状态
│   ├── core/
│   │   ├── TaskSolver.java       # 基因接口（不可变）
│   │   └── Evaluator.java        # 评估器，编译/验证/性能测试
│   ├── sandbox/
│   │   └── TargetSubject.java    # 被进化的目标代码
│   └── utils/
│       ├── CompilerUtils.java    # 动态编译工具
│       └── LLMClient.java        # LLM API 客户端
├── target/
│   └── generations/              # 进化代码备份
├── pom.xml
└── README.md
```

## 核心模块

### 1. 基因接口 (TaskSolver)

所有变异代码必须实现的不可变接口：

```java
public interface TaskSolver {
    int[] solve(int[] input);
}
```

### 2. 评估器 (Evaluator)

负责编译、验证和性能测试：

- **禁止 API 检查**: 禁用 `Arrays.sort`、`Collections.sort` 等标准库排序
- **正确性验证**: 4 个测试用例验证排序结果
- **性能测试**: 3 次预热 + 5 次正式测量取平均值
- **超时保护**: 10 秒强制超时，防止死循环

### 3. LLM 客户端 (LLMClient)

调用阿里云 Qwen API 进行代码变异：

- 强制实现 `TaskSolver` 接口
- 类名必须保持 `TargetSubject`
- 禁止使用标准库排序方法
- 自动清洗 Markdown 代码块

### 4. 动态编译工具 (CompilerUtils)

使用 JDK 内置编译器 API：

- 内存中编译源代码字符串
- 输出到 `target/classes` 目录
- 捕获并报告编译错误

## 快速开始

### 环境要求

- Java 25+
- Maven 3.6+
- 阿里云 DashScope API Key

### 配置 API Key

**方式一：配置文件（推荐）**

```bash
# 复制示例配置文件
cp config.properties.example config.properties

# 编辑 config.properties，填入你的 API Key
DASHSCOPE_API_KEY=your-api-key-here
```

**方式二：环境变量**

Windows (PowerShell):
```powershell
$env:DASHSCOPE_API_KEY="your-api-key-here"
```

Windows (CMD):
```cmd
set DASHSCOPE_API_KEY=your-api-key-here
```

Linux/Mac:
```bash
export DASHSCOPE_API_KEY="your-api-key-here"
```

### 构建项目

```bash
mvn clean compile
```

### 运行进化

```bash
mvn exec:java -Dexec.mainClass="com.quine.Main"
```

### 重置到初始状态

```bash
mvn exec:java -Dexec.mainClass="com.quine.ResetToOrigin"
```

## 配置说明

### LLM API 配置

在 `LLMClient.java` 中配置：

| 配置项 | 值 |
|--------|-----|
| 端点 | `https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions` |
| 模型 | `qwen3-coder-plus` |

### 进化参数

在 `Main.java` 中配置：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `TEST_DATA_SIZE` | 10000 | 测试数据规模 |
| `MAX_GENERATIONS` | 50 | 最大进化代数 |

## 进化流程

1. **Genesis (初始化)**
   - 加载初始冒泡排序实现
   - 生成随机测试数据
   - 记录基准性能

2. **Mutation (变异)**
   - 读取当前源代码
   - 调用 LLM 生成变异代码

3. **Compilation (编译)**
   - 动态编译变异代码
   - 编译失败则跳过本轮

4. **Verification (验证)**
   - 正确性测试
   - 性能基准测试

5. **Selection (选择)**
   - 性能更优则替换源文件
   - 备份成功变异到 `target/generations/`

## 进化成果示例

| 代数 | 算法 | 时间复杂度 |
|------|------|-----------|
| 初始 | 冒泡排序 (Bubble Sort) | O(n²) |
| Gen 1 | 快速排序 (Quick Sort) | O(n log n) |
| Gen 14 | 堆排序 (Heap Sort) | O(n log n) |

## 风险防护

- **死循环防护**: `ExecutorService` + `Future.get(timeout)` 强制超时
- **幻觉代码防护**: Prompt 限制只用 JDK 标准库
- **类加载泄露防护**: 每代测试后清理 ClassLoader
- **代码格式防护**: 正则清洗 LLM 返回的 Markdown 包裹

## 技术栈

- Java 25
- OkHttp 4.12.0 (HTTP 客户端)
- Gson 2.10.1 (JSON 解析)
- javax.tools (动态编译)

## 许可证

MIT License

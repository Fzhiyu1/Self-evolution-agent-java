# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

**Project Quine: Java 自进化算法引擎**

这是一个实验性项目，目标是构建一个 Java 运行时环境，使代码（排序算法）能够在不重启 JVM 的情况下，通过 LLM 的变异和评估，自动从 O(n²) 进化为 O(n log n)。

## 核心架构

系统由三个核心模块组成进化循环：

1. **Phenotype (表现型)**: 实现 `TaskSolver` 接口的 Java 源代码
   - 文件名: `TargetSubject.java`
   - 位置: `src/main/java/com/quine/sandbox/`
   - 初始实现: 冒泡排序（故意低效）

2. **Mutator (变异器)**: 读取源码 → 调用 LLM 修改 → 回写文件
   - 使用 LangChain4j 或 OkHttp 调用 DeepSeek/Qwen API
   - Prompt 约束: 必须实现 `TaskSolver` 接口，类名保持 `TargetSubject`，只返回纯 Java 代码

3. **Evaluator (评估器)**: 动态编译 → 加载运行 → 性能打分
   - 使用 `javax.tools.JavaCompiler` 进行内存编译
   - 使用 `URLClassLoader` 热加载（每代创建新实例避免类加载冲突）
   - 性能测试: 只有当新代码比基准更快时才替换源文件

## 关键技术约束

### 基因接口（不可变）
```java
package com.quine.core;

public interface TaskSolver {
    int[] solve(int[] input);
}
```

### 动态编译与热加载
- **编译**: 使用 `javax.tools.JavaCompiler` API（不使用命令行 javac）
- **热加载**: 每一代变异创建新的 `URLClassLoader` 实例
- **内存管理**: 测试完后将 mutant 对象置 null 并 close ClassLoader，防止 Metaspace 溢出

### LLM Prompt 策略
- 强调只能使用 JDK 标准库（防止幻觉引入第三方依赖）
- 要求返回纯 Java 代码（需要正则清洗 Markdown 代码块）
- 明确接口和类名约束

## 进化流程

1. **Genesis**: 加载初始 BubbleSort，生成大数组测试集，记录 baselineTime
2. **Loop**:
   - Mutation → Compilation（失败则跳过）
   - Loading → Verification（正确性测试）
   - Benchmark → Selection（性能更优则替换源文件并更新基准）

## 风险防护

- **死循环**: 使用 `ExecutorService` + `Future.get(timeout)` 强制超时
- **幻觉代码**: Prompt 中限制只用 JDK 标准库
- **类加载泄露**: 确保每代测试后清理 ClassLoader
- **代码格式**: 正则清洗 LLM 返回的 Markdown 包裹

## 技术栈

- Java 21
- LangChain4j 或 OkHttp（LLM 调用）
- JDK 自带 `javax.tools`（编译）
- 无其他外部依赖

## LLM API 配置

- **模型**: qwen3-coder-plus
- **端点**: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
- **API Key**: sk-71aa4fef90764b0c84c6a9dea29a2dc8

## 开发优先级

1. 先实现 `CompilerUtils`：字符串源码 → 编译成 Class → 反射调用
2. 接入 LLM API 测试代码变异
3. 串联完整进化循环

## 特殊注意

- **DialogWebSocketHandler** 和 **newDialogue.vue** 文件非常大，使用搜索而非完整读取
- 编译输出可选: 内存编译（最优）或临时目录 `target/generations/gen_X/`（MVP 妥协方案）

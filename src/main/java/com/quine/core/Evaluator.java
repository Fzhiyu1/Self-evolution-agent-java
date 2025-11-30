package com.quine.core;

import com.quine.utils.CompilerUtils;

import java.util.Arrays;
import java.util.concurrent.*;

public class Evaluator {
    private static final int TIMEOUT_SECONDS = 20;

    /**
     * 评估结果
     */
    public static class EvalResult {
        public final boolean success;
        public final long timeUs;  // 改用微秒精度
        public final String error;

        public EvalResult(boolean success, long timeUs, String error) {
            this.success = success;
            this.timeUs = timeUs;
            this.error = error;
        }
    }

    /**
     * 评估代码：编译 -> 加载 -> 正确性测试 -> 性能测试
     */
    public EvalResult evaluate(String sourceCode, int[] testData) {
        try {
            // 0. 代码检查：禁止使用标准库排序
            if (containsForbiddenAPIs(sourceCode)) {
                return new EvalResult(false, 0, "代码违规：禁止使用 Arrays.sort 或 Collections.sort");
            }

            // 1. 编译并加载
            Class<?> clazz = CompilerUtils.compileAndLoad("com.quine.sandbox.TargetSubject", sourceCode);
            TaskSolver solver = (TaskSolver) clazz.getDeclaredConstructor().newInstance();

            // 2. 正确性测试
            if (!verifyCorrectness(solver)) {
                return new EvalResult(false, 0, "正确性测试失败");
            }

            // 3. 性能测试（带超时保护）
            long timeUs = benchmark(solver, testData);
            return new EvalResult(true, timeUs, null);

        } catch (Exception e) {
            return new EvalResult(false, 0, "编译或运行错误: " + e.getMessage());
        }
    }

    /**
     * 检查代码是否使用了禁止的 API
     */
    private boolean containsForbiddenAPIs(String sourceCode) {
        return sourceCode.contains("Arrays.sort") ||
               sourceCode.contains("Collections.sort") ||
               sourceCode.contains("Arrays.parallelSort");
    }

    /**
     * 正确性验证
     */
    private boolean verifyCorrectness(TaskSolver solver) {
        int[][] testCases = {
            {5, 3, 1, 4, 2},
            {1},
            {2, 1},
            {-5, 0, 3, -2, 10}
        };

        for (int[] testCase : testCases) {
            int[] expected = testCase.clone();
            Arrays.sort(expected);

            int[] result = solver.solve(testCase.clone());

            if (!Arrays.equals(result, expected)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 性能基准测试（带预热和多次测量）
     */
    private long benchmark(TaskSolver solver, int[] testData) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<Long> future = executor.submit(() -> {
                // 1. 预热阶段：跑 3 次让 JIT 编译器介入
                for (int i = 0; i < 3; i++) {
                    solver.solve(testData.clone());
                }

                // 2. 正式测量：跑 5 次取平均值
                long totalTime = 0;
                for (int i = 0; i < 5; i++) {
                    int[] input = testData.clone();
                    long start = System.nanoTime();
                    solver.solve(input);
                    long end = System.nanoTime();
                    totalTime += (end - start);
                }

                // 返回平均时间（纳秒转微秒，保留更高精度）
                return totalTime / 5 / 1_000;
            });

            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            throw new RuntimeException("执行超时（超过 " + TIMEOUT_SECONDS + " 秒）");
        } finally {
            executor.shutdownNow();
        }
    }
}

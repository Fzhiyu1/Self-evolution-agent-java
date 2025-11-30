package com.quine;

import java.io.IOException;
import java.nio.file.*;

/**
 * 重置工具：将代码恢复为初始的冒泡排序
 * 运行此类可以重新开始进化
 */
public class ResetToOrigin {
    private static final String TARGET_FILE = "src/main/java/com/quine/sandbox/TargetSubject.java";

    // 初始的冒泡排序代码
    private static final String INITIAL_CODE = """
package com.quine.sandbox;

import com.quine.core.TaskSolver;

public class TargetSubject implements TaskSolver {
    @Override
    public int[] solve(int[] input) {
        // 冒泡排序 - 故意低效的实现
        int[] arr = input.clone();
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
        return arr;
    }
}
""";

    public static void main(String[] args) {
        try {
            // 备份当前代码（如果存在）
            Path targetPath = Paths.get(TARGET_FILE);
            if (Files.exists(targetPath)) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                String backupFile = "target/backup_before_reset_" + timestamp + ".java";
                Files.createDirectories(Paths.get("target"));
                Files.copy(targetPath, Paths.get(backupFile));
                System.out.println("✓ 当前代码已备份到: " + backupFile);
            }

            // 写入初始代码
            Files.writeString(targetPath, INITIAL_CODE);

            System.out.println("✓ 代码已重置为初始冒泡排序");
            System.out.println("✓ 现在可以重新运行 Main.java 开始进化");

        } catch (IOException e) {
            System.err.println("✗ 重置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

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

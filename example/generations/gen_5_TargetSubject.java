package com.quine.sandbox;

import com.quine.core.TaskSolver;

public class TargetSubject implements TaskSolver {
    @Override
    public int[] solve(int[] input) {
        int[] arr = input.clone();
        int n = arr.length;
        
        if (n <= 1) {
            return arr;
        }
        
        int gap = 1;
        while (gap < n / 3) {
            gap = gap * 3 + 1;
        }
        
        while (gap >= 1) {
            for (int i = gap; i < n; i++) {
                int temp = arr[i];
                int j = i;
                
                while (j >= gap && arr[j - gap] > temp) {
                    arr[j] = arr[j - gap];
                    j -= gap;
                }
                
                arr[j] = temp;
            }
            gap /= 3;
        }
        
        return arr;
    }
}
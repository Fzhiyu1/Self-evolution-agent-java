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
        
        int gap = n;
        boolean swapped = true;
        
        while (gap > 1 || swapped) {
            if (gap > 1) {
                gap = Math.max(1, (int)(gap / 1.3));
            }
            
            int i = 0;
            swapped = false;
            
            while (i + gap < n) {
                if (arr[i] > arr[i + gap]) {
                    int temp = arr[i];
                    arr[i] = arr[i + gap];
                    arr[i + gap] = temp;
                    swapped = true;
                }
                i++;
            }
        }
        
        return arr;
    }
}
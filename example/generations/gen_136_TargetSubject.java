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

        dualPivotQuicksort(arr, 0, n - 1);
        return arr;
    }

    private void dualPivotQuicksort(int[] arr, int left, int right) {
        if (left < right) {
            if (right - left < 27) {
                insertionSort(arr, left, right);
                return;
            }

            int sixth = (right - left + 1) / 6;
            int e1 = left  + sixth;
            int e2 = e1     + sixth;
            int e3 = e2     + sixth;
            int e4 = e3     + sixth;
            int e5 = e4     + sixth;

            if (arr[e1] > arr[e2]) { swap(arr, e1, e2); }
            if (arr[e4] > arr[e5]) { swap(arr, e4, e5); }
            if (arr[e1] > arr[e3]) { swap(arr, e1, e3); }
            if (arr[e2] > arr[e3]) { swap(arr, e2, e3); }
            if (arr[e1] > arr[e4]) { swap(arr, e1, e4); }
            if (arr[e3] > arr[e4]) { swap(arr, e3, e4); }
            if (arr[e2] > arr[e5]) { swap(arr, e2, e5); }
            if (arr[e2] > arr[e3]) { swap(arr, e2, e3); }
            if (arr[e4] > arr[e5]) { swap(arr, e4, e5); }

            arr[left] = arr[e2];
            arr[right] = arr[e4];

            int pivot1 = arr[left];
            int pivot2 = arr[right];

            if (pivot1 > pivot2) {
                swap(arr, left, right);
                int tmp = pivot1;
                pivot1 = pivot2;
                pivot2 = tmp;
            }

            int less  = left + 1;
            int great = right - 1;

            for (int k = less; k <= great; k++) {
                if (arr[k] < pivot1) {
                    swap(arr, k, less++);
                } else if (arr[k] > pivot2) {
                    while (k < great && arr[great] > pivot2) great--;
                    swap(arr, k, great--);
                    if (arr[k] < pivot1) {
                        swap(arr, k, less++);
                    }
                }
            }
            int dist = great - less;
            if (dist < 13) {
               insertionSort(arr, left, right);
               return;
            }
            swap(arr, less - 1, left);
            swap(arr, great + 1, right);

            dualPivotQuicksort(arr, left, less - 2);
            dualPivotQuicksort(arr, great + 2, right);

            if (dist > right - left - 13 && pivot1 != pivot2) {
                while (less < great && arr[less] == pivot1) less++;
                while (less < great && arr[great] == pivot2) great--;
                for (int k = less; k <= great; k++) {
                    if (arr[k] == pivot1 || arr[k] == pivot2) {
                        swap(arr, k, less++);
                    }
                }
            }
            dualPivotQuicksort(arr, less, great);
        }
    }

    private void insertionSort(int[] arr, int low, int high) {
        for (int i = low + 1; i <= high; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= low && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
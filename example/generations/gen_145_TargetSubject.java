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

        optimizedSort(arr, 0, n - 1);
        return arr;
    }

    private void optimizedSort(int[] arr, int left, int right) {
        while (left < right) {
            if (right - left < 16) {
                insertionSort(arr, left, right);
                return;
            }

            int pivotIndex = medianOfThree(arr, left, right);
            swap(arr, pivotIndex, right);

            int partitionIndex = partition(arr, left, right);

            if (partitionIndex - left < right - partitionIndex) {
                optimizedSort(arr, left, partitionIndex - 1);
                left = partitionIndex + 1;
            } else {
                optimizedSort(arr, partitionIndex + 1, right);
                right = partitionIndex - 1;
            }
        }
    }

    private int medianOfThree(int[] arr, int left, int right) {
        int mid = (left + right) >>> 1;
        if (arr[left] > arr[mid]) swap(arr, left, mid);
        if (arr[mid] > arr[right]) swap(arr, mid, right);
        if (arr[left] > arr[mid]) swap(arr, left, mid);
        return mid;
    }

    private int partition(int[] arr, int left, int right) {
        int pivot = arr[right];
        int i = left - 1;

        for (int j = left; j < right; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, right);
        return i + 1;
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
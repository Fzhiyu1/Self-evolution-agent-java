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

        quickSort(arr, 0, n - 1);

        return arr;
    }

    private void quickSort(int[] arr, int low, int high) {
        while (low < high) {
            if (high - low < 10) {
                insertionSort(arr, low, high);
                break;
            } else {
                int pivotIndex = medianOfThreePartition(arr, low, high);
                if (pivotIndex - low < high - pivotIndex) {
                    quickSort(arr, low, pivotIndex - 1);
                    low = pivotIndex + 1;
                } else {
                    quickSort(arr, pivotIndex + 1, high);
                    high = pivotIndex - 1;
                }
            }
        }
    }

    private int medianOfThreePartition(int[] arr, int low, int high) {
        int mid = low + (high - low) / 2;
        if (arr[mid] < arr[low]) {
            swap(arr, low, mid);
        }
        if (arr[high] < arr[low]) {
            swap(arr, low, high);
        }
        if (arr[high] < arr[mid]) {
            swap(arr, mid, high);
        }
        swap(arr, mid, high);
        return partition(arr, low, high);
    }

    private int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }

        swap(arr, i + 1, high);
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
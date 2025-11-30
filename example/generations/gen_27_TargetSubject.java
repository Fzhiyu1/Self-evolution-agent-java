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

        introsort(arr, 0, n - 1, 2 * floorLog2(n));

        return arr;
    }

    private void introsort(int[] arr, int low, int high, int depthLimit) {
        while (low < high) {
            int size = high - low + 1;
            if (size <= 16) {
                insertionSort(arr, low, high);
                return;
            } else if (depthLimit == 0) {
                heapSort(arr, low, high);
                return;
            } else {
                if (high - low < 10) {
                    insertionSort(arr, low, high);
                    return;
                } else {
                    int pivotIndex = medianOfThreePartition(arr, low, high);
                    if (pivotIndex - low < high - pivotIndex) {
                        introsort(arr, low, pivotIndex - 1, depthLimit - 1);
                        low = pivotIndex + 1;
                    } else {
                        introsort(arr, pivotIndex + 1, high, depthLimit - 1);
                        high = pivotIndex - 1;
                    }
                }
            }
        }
    }

    private int floorLog2(int n) {
        int log = 0;
        while ((n >>= 1) != 0) {
            log++;
        }
        return log;
    }

    private void heapSort(int[] arr, int low, int high) {
        int n = high - low + 1;
        for (int i = low + (n / 2 - 1); i >= low; i--) {
            heapify(arr, n, i, low);
        }
        for (int i = low + n - 1; i > low; i--) {
            swap(arr, low, i);
            heapify(arr, i - low, low, low);
        }
    }

    private void heapify(int[] arr, int n, int i, int offset) {
        int largest = i;
        int left = 2 * (i - offset) + 1 + offset;
        int right = 2 * (i - offset) + 2 + offset;

        if (left < offset + n && arr[left] > arr[largest]) {
            largest = left;
        }
        if (right < offset + n && arr[right] > arr[largest]) {
            largest = right;
        }
        if (largest != i) {
            swap(arr, i, largest);
            heapify(arr, n, largest, offset);
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
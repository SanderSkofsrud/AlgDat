import java.util.Random;

public class Quicksort {

  // Constants for array size, bound size, and the threshold for using ShellSort
  private static final int SIZE = 10000000;
  private static final int BOUND_SIZE = 10000000;
  private static final int THRESHOLD = 50;

  /**
   * Quicksort algorithm.
   * If the array size is smaller than the threshold, it uses ShellSort.
   * Otherwise, it uses Dual Pivot Quicksort.
   *
   * Time Complexity: Θ(n log n) in the average case.
   * This is the average-case time complexity. In the worst case, it can be O(n^2).
   *
   * @param arr   The array to be sorted.
   * @param left  The left index of the subarray to be sorted.
   * @param right The right index of the subarray to be sorted.
   */
  public static void quicksortWithShellSortHelper(int[] arr, int left, int right) {
    if (right - left + 1 < THRESHOLD) {
      shellSort(arr, left, right);
    } else {
      dualPivotQuicksort(arr, left, right, THRESHOLD);
    }
  }

  /**
   * Dual Pivot Quicksort algorithm.
   *
   * @param arr       The array to be sorted.
   * @param low       The left index of the subarray to be sorted.
   * @param high      The right index of the subarray to be sorted.
   * @param threshold The threshold for switching to ShellSort.
   */
  public static void dualPivotQuicksort(int[] arr, int low, int high, int threshold) {
    if (low < high) {
      int[] pivots = new int[2]; // pivots[] holds the dual pivot elements

      median3sort(arr, low, high); // Use median-of-three to select pivots
      partitionDualPivot(arr, low, high, pivots);

      dualPivotQuicksort(arr, low, pivots[0] - 1, threshold);
      dualPivotQuicksort(arr, pivots[0] + 1, pivots[1] - 1, threshold);
      dualPivotQuicksort(arr, pivots[1] + 1, high, threshold);
    }
  }

  /**
   * Partition the array for Dual Pivot Quicksort.
   *
   * @param arr    The array to be partitioned.
   * @param low    The left index of the subarray to be partitioned.
   * @param high   The right index of the subarray to be partitioned.
   * @param pivots An array to store the pivot positions.
   */
  private static void partitionDualPivot(int[] arr, int low, int high, int[] pivots) {
    if (arr[low] > arr[high]) {
      swap(arr, low, high);
    }

    int j = low + 1;
    int g = high - 1, k = low + 1;
    int p = arr[low], q = arr[high];

    while (k <= g) {
      if (arr[k] < p) {
        swap(arr, k, j);
        j++;
      } else if (arr[k] >= q) {
        while (arr[g] > q && k < g) {
          g--;
        }
        swap(arr, k, g);
        g--;
        if (arr[k] < p) {
          swap(arr, k, j);
          j++;
        }
      }
      k++;
    }
    j--;
    g++;

    swap(arr, low, j);
    swap(arr, high, g);

    pivots[0] = j;
    pivots[1] = g;
  }

  /**
   * Sorts the subarray using median-of-three pivot selection.
   *
   * @param arr  The array to be sorted.
   * @param low  The left index of the subarray.
   * @param high The right index of the subarray.
   */
  private static void median3sort(int[] arr, int low, int high) {
    int mid = (low + high) / 2;
    if (arr[low] > arr[mid]) {
      swap(arr, low, mid);
    }
    if (arr[low] > arr[high]) {
      swap(arr, low, high);
    }
    if (arr[mid] > arr[high]) {
      swap(arr, mid, high);
    }
    swap(arr, mid, high);
  }

  /**
   * ShellSort algorithm.
   *
   * @param arr   The array to be sorted.
   * @param left  The left index of the subarray to be sorted.
   * @param right The right index of the subarray to be sorted.
   */
  public static void shellSort(int[] arr, int left, int right) {
    int n = right - left + 1;
    for (int gap = n / 2; gap > 0; gap /= 2) {
      for (int i = left + gap; i <= right; i++) {
        int temp = arr[i];
        int j;
        for (j = i; j >= left + gap && arr[j - gap] > temp; j -= gap) {
          arr[j] = arr[j - gap];
        }
        arr[j] = temp;
      }
    }
  }

  /**
   * Swap two elements in an array.
   *
   * @param arr The array in which elements are to be swapped.
   * @param i   Index of the first element to be swapped.
   * @param j   Index of the second element to be swapped.
   */
  private static void swap(int[] arr, int i, int j) {
    int temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
  }

  /**
   * Check if the array is sorted correctly.
   *
   * @param arr The array to be checked for sorted order.
   * @return True if the array is sorted, false otherwise.
   */
  public static boolean isSorted(int[] arr) {
    for (int i = 1; i < arr.length; i++) {
      if (arr[i] < arr[i - 1]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Get checksum of the array.
   *
   * @param arr The array for which the checksum needs to be calculated.
   * @return The checksum value.
   */
  public static int checksum(int[] arr) {
    int sum = 0;
    for (int num : arr) {
      sum += num;
    }
    return sum;
  }

  /**
   * Generate a random data array.
   *
   * @param bound The upper bound for random values.
   * @param size  The size of the array.
   * @return The generated random data array.
   */
  public static int[] randomDataArray(int bound, int size) {
    Random rnd = new Random();
    int[] array = new int[size];
    for (int i = 0; i < size; i++) {
      array[i] = rnd.nextInt(bound);
    }
    return array;
  }

  /**
   * Generate an array with duplicates.
   *
   * @param bound The upper bound for random values (for non-duplicate elements).
   * @param size  The size of the array (including duplicates).
   * @return The generated array with duplicates.
   */
  public static int[] arrayWithDuplicates(int bound, int size) {
    Random rnd = new Random();
    int[] array = new int[size];
    for (int i = 0; i < size; i += 2) {
      array[i] = rnd.nextInt(bound);
      array[i + 1] = 42;  // Duplicate value
    }
    return array;
  }

  /**
   * Generate a sorted data array.
   *
   * @param size The size of the sorted array.
   * @return The generated sorted data array.
   */
  public static int[] sortedDataArray(int size) {
    int[] array = new int[size];
    for (int i = 0; i < size; i++) {
      array[i] = i;
    }
    return array;
  }

  /**
   * Test sorting performance on a random dataset.
   * It prints the time taken to sort the dataset and checks if the sorting is correct.
   */
  public static void testSortingPerformance() {
    int[] data = randomDataArray(BOUND_SIZE, SIZE);
    int initialChecksum = checksum(data);

    long startTime = System.currentTimeMillis();
    quicksortWithShellSortHelper(data, 0, SIZE - 1);
    long endTime = System.currentTimeMillis();

    assert isSorted(data) : "Sorting failed on random dataset!";
    assert initialChecksum == checksum(data) : "Checksum failed on random dataset! Some values might have been overwritten.";

    System.out.println("Sorting " + SIZE + " elements took " + (endTime - startTime) + " milliseconds.");
  }

  /**
   * Test sorting performance on a dataset with duplicates.
   * It prints the time taken to sort the dataset and checks if the sorting is correct.
   */
  public static void testSortingWithDuplicates() {
    int[] data = arrayWithDuplicates(BOUND_SIZE, SIZE);
    int initialChecksum = checksum(data);

    long startTime = System.currentTimeMillis();
    quicksortWithShellSortHelper(data, 0, SIZE - 1);
    long endTime = System.currentTimeMillis();

    assert isSorted(data) : "Sorting failed on dataset with duplicates!";
    assert initialChecksum == checksum(data) : "Checksum failed on dataset with duplicates! Some values might have been overwritten.";

    System.out.println("Sorting " + SIZE + " elements with duplicates took " + (endTime - startTime) + " milliseconds.");
  }

  /**
   * Test sorting performance on an already sorted dataset.
   * It prints the time taken to sort the dataset and checks if the sorting is correct.
   */
  public static void testSortingAlreadySortedData() {
    int[] data = sortedDataArray(SIZE);
    int initialChecksum = checksum(data);

    long startTime = System.currentTimeMillis();
    quicksortWithShellSortHelper(data, 0, SIZE - 1);
    long endTime = System.currentTimeMillis();

    assert isSorted(data) : "Sorting failed on already sorted dataset!";
    assert initialChecksum == checksum(data) : "Checksum failed on already sorted dataset! Some values might have been overwritten.";

    System.out.println("Sorting " + SIZE + " elements that are already sorted took " + (endTime - startTime) + " milliseconds.");
  }

//    Code used to find out the best threshold value. Can be used to generate graphs.
//
//    /**
//     * Test sorting performance on different datasets and generate graphs.
//     * It prints the time taken to sort the dataset and checks if the sorting is correct.
//     * It can be used to generate graphs for the report.
//     */
//    public static void testSortingPerformanceWithGraphs() {
//        int[] sizes = { 1000, 5000, 10000, 50000, 100000, 500000, 1000000, 5000000, 10000000 };
//        int[] thresholds = { 10, 30, 50, 70, 100 }; // Try different threshold values
//        long[][] results = new long[thresholds.length][sizes.length]; // To store times for different thresholds and sizes
//
//        for (int thresholdIndex = 0; thresholdIndex < thresholds.length; thresholdIndex++) {
//            int threshold = thresholds[thresholdIndex];
//            System.out.println("Threshold: " + threshold);
//            for (int sizeIndex = 0; sizeIndex < sizes.length; sizeIndex++) {
//                int size = sizes[sizeIndex];
//                System.out.println("Testing size: " + size);
//                long[] timeResults = new long[5]; // To store times for 5 runs
//                int[] data = randomDataArray(boundSize, size);
//
//                int initialChecksum = checksum(data); // Calculate initial checksum
//
//                for (int i = 0; i < 5; i++) {
//                    int[] copy = data.clone(); // Create a copy to ensure fairness in testing
//                    long startTime = System.currentTimeMillis();
//                    quicksortWithShellSortHelper(copy, 0, size - 1);
//                    long endTime = System.currentTimeMillis();
//                    assert isSorted(copy) : "Sorting failed on random dataset!";
//                    assert initialChecksum == checksum(copy) : "Checksum failed on random dataset! Some values might have been overwritten.";
//                    timeResults[i] = endTime - startTime;
//                }
//
//                // Calculate the average time for this test case
//                long avgTime = 0;
//                for (long time : timeResults) {
//                    avgTime += time;
//                }
//                avgTime /= 5; // Average over 5 runs
//
//                results[thresholdIndex][sizeIndex] = avgTime; // Store the result. Can be used to generate graphs
//
//                System.out.println("Average time for size " + size + ": " + avgTime + " milliseconds");
//            }
//        }
//    }

  /**
   * Main method to run performance tests with different thresholds.
   * It tests sorting performance with varying threshold values and prints the best threshold.
   */
  public static void main(String[] args) {
    System.out.println("Starting performance tests...\n");

    testSortingPerformance();
    testSortingWithDuplicates();
    testSortingAlreadySortedData();
    //testSortingPerformanceWithGraphs();

    System.out.println("\nAll tests completed successfully!");
  }
}

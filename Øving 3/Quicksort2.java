import java.util.Random;

/**
 * This class provides an implementation of the quicksort algorithm with enhancements.
 */
public class Quicksort2 {

  private static final Random random = new Random();
  private static int[] unsortedNumbers = new int[10000000];

  /**
   * Standard quicksort algorithm.
   *
   * @param t The array to sort.
   * @param v The starting index.
   * @param h The ending index.
   */
  public static void quicksort(int[] t, int v, int h) {
    if (h - v > 2) {
      int k = split(t, v, h);
      quicksort(t, v, k - 1);
      quicksort(t, k + 1, h);
    } else {
      median3sort(t, v, h);
    }
  }

  /**
   * Quicksort algorithm with a threshold to switch to shell sort.
   * Time Complexity: Θ(n log n) in the average case.
   * This is the average-case time complexity. In the worst case, it can be O(n^2).
   *
   * @param t The array to sort.
   * @param v The starting index.
   * @param h The ending index.
   * @param threshold The threshold for switching to shell sort.
   */
  public static void quicksortWithShellSort(int[] t, int v, int h, int threshold) {
    if (h - v > threshold) {
      int k = split(t, v, h);
      quicksortWithShellSort(t, v, k - 1, threshold);
      quicksortWithShellSort(t, k + 1, h, threshold);
    } else {
      shellSort(t, v, h);
    }
  }

  /**
   * Shell sort algorithm.
   *
   * @param t The array to sort.
   * @param v The starting index.
   * @param h The ending index.
   */
  private static void shellSort(int[] t, int v, int h) {
    int n = h - v + 1;
    for (int gap = n / 2; gap > 0; gap /= 2) {
      for (int i = v + gap; i <= h; i++) {
        int temp = t[i];
        int j;
        for (j = i; j >= v + gap && t[j - gap] > temp; j -= gap) {
          t[j] = t[j - gap];
        }
        t[j] = temp;
      }
    }
  }

  /**
   * Splits an array segment into two parts.
   *
   * @param t The array segment.
   * @param v The starting index.
   * @param h The ending index.
   * @return The index of the pivot.
   */
  private static int split(int[] t, int v, int h) {
    int iv;
    int ih;
    int m = median3sort(t, v, h);
    int dv = t[m];
    swap(t, m, h - 1);
    for (iv = v, ih = h - 1;;) {
      while (t[++iv] < dv) ;
      while (t[--ih] > dv) ;
      if (iv >= ih) {
        break;
      }
      swap(t, iv, ih);
    }
    swap(t, iv, h - 1);
    return iv;
  }

  /**
   * Sorts the first, middle, and last element of an array segment and returns the median.
   *
   * @param t The array segment.
   * @param v The starting index.
   * @param h The ending index.
   * @return The index of the median element.
   */
  private static int median3sort(int[] t, int v, int h) {
    int m = (v + h) / 2;
    if (t[v] > t[m]) {
      swap(t, v, m);
    }
    if (t[m] > t[h]) {
      swap(t, m, h);
      if (t[v] > t[m]) {
        swap(t, v, m);
      }
    }
    return m;
  }

  /**
   * Swaps the elements of an array at the given indices.
   *
   * @param t The array.
   * @param i The first index.
   * @param j The second index.
   */
  private static void swap(int[] t, int i, int j) {
    int k = t[j];
    t[j] = t[i];
    t[i] = k;
  }

  /**
   * Fills an array with random numbers.
   *
   * @param array The array to fill.
   * @param withDuplicates If true, every second element is set to 42.
   */
  private static void fillArrayWithRandomNumbers(int[] array, boolean withDuplicates) {
    for (int i = 0; i < array.length; i++) {
      if (withDuplicates && i % 2 == 0) {
        array[i] = 42;
      } else {
        array[i] = random.nextInt(1000000);
      }
    }
  }

  /**
   * Computes the sum of elements in an array.
   *
   * @param array The array.
   * @return The sum of the array's elements.
   */
  private static int sumOfArray(int[] array) {
    int sum = 0;
    for (int j : array) {
      sum += j;
    }
    return sum;
  }

  /**
   * Tests if an array is sorted in ascending order.
   *
   * @param array The array to test.
   * @return True if the array is sorted, false otherwise.
   */
  private static boolean test(int[] array) {
    for (int i = 0; i < array.length - 1; i++) {
      if (array[i + 1] < array[i]) {
        return false;
      }
    }
    return true;
  }

  public static void main(String[] args) {
    // The commented code below was used to calculate an optimal threshold.
    // Int threshold = 44 is the average of the optimal thresholds found.
    //    int[] optimalThreshold = new int[10];
    //    for (int i = 0; i < 10; i++) {
    //      fillArrayWithRandomNumbers(unsortedNumbers, false);
    //      optimalThreshold[i] = findOptimalThreshold(100, unsortedNumbers);
    //      System.out.println("Optimal threshold test " + (i + 1) + ": " + optimalThreshold[i]);
    //    }
    //    int threshold = 0;
    //    for (int i : optimalThreshold) {
    //      threshold += i;
    //    }
    //    threshold /= optimalThreshold.length;
    //    System.out.println("Optimal threshold: " + threshold + "\n");
    int threshold = 44;
    fillArrayWithRandomNumbers(unsortedNumbers, false);
    testQuicksort(unsortedNumbers);
    int[] duplicateNumbers = new int[10000000];
    fillArrayWithRandomNumbers(duplicateNumbers, true);
    testQuicksort(duplicateNumbers);
    testQuickSortWithNThreshold(threshold);
    for (int i = 2; i <= threshold; i++) {
      System.out.println();
      testQuickSortWithNThreshold(i);
    }
  }

//  /**
//   * Finds the optimal threshold for quicksort to switch to shell sort.
//   *
//   * @param maxThreshold The maximum threshold to test.
//   * @param testData The array to test the sorting on.
//   * @return The optimal threshold.
//   */
//  public static int findOptimalThreshold(int maxThreshold, int[] testData) {
//    long minTime = Long.MAX_VALUE;
//    int optimalThreshold = -1;
//
//    for (int threshold = 2; threshold <= maxThreshold; threshold++) {
//      int[] copiedData = testData.clone();
//      long startTime = System.currentTimeMillis();
//      quicksortWithShellSort(copiedData, 0, copiedData.length - 1, threshold);
//      long endTime = System.currentTimeMillis();
//
//      long timeTaken = endTime - startTime;
//      if (timeTaken < minTime) {
//        minTime = timeTaken;
//        optimalThreshold = threshold;
//      }
//    }
//    return optimalThreshold;
//  }

  /**
   * Test the standard quicksort algorithm.
   */
  public static void testQuicksort(int[] array) {
    int[] copiedArray = array.clone();
    int sumArrayBefore = sumOfArray(copiedArray);

    long startTime = System.currentTimeMillis();
    quicksort(copiedArray, 0, copiedArray.length - 1);
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;

    System.out.println("Used " +  totalTime + " ms sorting with normal quicksort");
    int sumArrayAfter = sumOfArray(copiedArray);
    System.out.println("Test if sorted correctly: " + test(copiedArray));
    System.out.println("Same numbers before as after sorting: " + (sumArrayBefore - sumArrayAfter == 0) + "\n");

    // Test with sorted array
    long startTime2 = System.currentTimeMillis();
    quicksort(copiedArray, 0, copiedArray.length - 1);
    long endTime2 = System.currentTimeMillis();
    long totalTime2 = endTime2 - startTime2;

    System.out.println("Used " +  totalTime2 + " ms sorting with normal quicksort on sorted array");
    sumArrayAfter = sumOfArray(copiedArray);
    System.out.println("Test if sorted correctly: " + test(copiedArray));
    System.out.println("Same numbers before as after sorting: " + (sumArrayBefore - sumArrayAfter == 0) + "\n");
  }

  /**
   * Test the quicksort algorithm with a threshold to switch to shell sort.
   *
   * @param threshold The threshold for switching to shell sort.
   */
  public static void testQuickSortWithNThreshold(int threshold) {
    int[] copiedArray = unsortedNumbers.clone();
    int sumArrayBefore = sumOfArray(copiedArray);

    long startTime = System.currentTimeMillis();
    quicksortWithShellSort(copiedArray, 0, copiedArray.length - 1, threshold);
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;

    System.out.printf("Used " + totalTime + " ms with threshold of " + threshold + "\n");
    int sumArrayAfter = sumOfArray(copiedArray);
    System.out.println("Test if sorted correctly: " + test(copiedArray));
    System.out.println("Same numbers before and after sorting: " + (sumArrayBefore - sumArrayAfter == 0));

    // Test with sorted array
    sumArrayBefore = sumOfArray(copiedArray);

    long startTime2 = System.currentTimeMillis();
    quicksortWithShellSort(copiedArray, 0, copiedArray.length - 1, threshold);
    long endTime2 = System.currentTimeMillis();
    long totalTime2 = endTime2 - startTime2;

    System.out.printf("Used " + totalTime2 + " ms with threshold of " + threshold + " on sorted array\n");
    sumArrayAfter = sumOfArray(copiedArray);
    System.out.println("Test if sorted correctly: " + test(copiedArray));
    System.out.println("Same numbers before and after sorting: " + (sumArrayBefore - sumArrayAfter == 0));
  }
}

/**
 * Abstract base class representing a hash table.
 */
abstract class AbstractHashTable {
  protected static final int TABLE_SIZE = 20_000_000; // Approximated size
  protected Integer[] table = new Integer[TABLE_SIZE];
  protected int collisionCount = 0;

  /**
   * Insert a value into the hash table.
   * @param value The value to be inserted.
   */
  public abstract void insert(int value);

  /**
   * Get the number of collisions encountered during insertions.
   * @return The number of collisions.
   */
  public int getCollisionCount() {
    return collisionCount;
  }
}

/**
 * Hash table implementation using linear probing for collision resolution.
 */
class LinearProbingHashTable extends AbstractHashTable {

  private int hashFunction(int value) {
    return Math.abs(value) % TABLE_SIZE;
  }

  @Override
  public void insert(int value) {
    int index = hashFunction(value);
    while (table[index] != null) {
      collisionCount++;
      index = (index + 1) % TABLE_SIZE;
    }
    table[index] = value;
  }
}

/**
 * Hash table implementation using double hashing for collision resolution.
 */
class DoubleHashingHashTable extends AbstractHashTable {

  private int hashFunction1(int value) {
    return Math.abs(value) % TABLE_SIZE;
  }

  private int hashFunction2(int value) {
    return 1 + (Math.abs(value) % (TABLE_SIZE - 1));
  }

  @Override
  public void insert(int value) {
    int index = hashFunction1(value);
    int stepSize = hashFunction2(value);
    while (table[index] != null) {
      collisionCount++;
      index = (index + stepSize) % TABLE_SIZE;
    }
    table[index] = value;
  }
}

/**
 * Class to test and analyze the performance of the hash tables.
 */
public class HashTableTest {

  private static final int NUM_COUNT = 10_000_000;
  private static int[] randomNumbers = new int[NUM_COUNT];

  /**
   * Generate and shuffle 10 million unique random numbers.
   */
  private static void generateRandomNumbers() {
    java.util.Random rand = new java.util.Random();
    for (int i = 0; i < NUM_COUNT; i++) {
      randomNumbers[i] = i + rand.nextInt(1000);
    }
    for (int i = 0; i < NUM_COUNT; i++) {
      int swapIndex = rand.nextInt(NUM_COUNT);
      int temp = randomNumbers[i];
      randomNumbers[i] = randomNumbers[swapIndex];
      randomNumbers[swapIndex] = temp;
    }
  }

  /**
   * Test the hash tables for a given fill factor and print results.
   * @param fillFactor The percentage of the total numbers to be inserted.
   * @param linearHashTable The hash table using linear probing.
   * @param doubleHashTable The hash table using double hashing.
   */
  private static void testHashTables(double fillFactor, LinearProbingHashTable linearHashTable, DoubleHashingHashTable doubleHashTable) {
    int insertCount = (int) (NUM_COUNT * fillFactor);

    long startTime = System.currentTimeMillis();
    for (int i = 0; i < insertCount; i++) {
      linearHashTable.insert(randomNumbers[i]);
    }
    long endTime = System.currentTimeMillis();
    System.out.println("Linear HashTable (" + fillFactor * 100 + "% fill): Collisions = "
            + linearHashTable.getCollisionCount() + ", Time = " + (endTime - startTime) + "ms");

    startTime = System.currentTimeMillis();
    for (int i = 0; i < insertCount; i++) {
      doubleHashTable.insert(randomNumbers[i]);
    }
    endTime = System.currentTimeMillis();
    System.out.println("Double Hashing HashTable (" + fillFactor * 100 + "% fill): Collisions = "
            + doubleHashTable.getCollisionCount() + ", Time = " + (endTime - startTime) + "ms");
  }

  /**
   * Main method to run the tests.
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    generateRandomNumbers();

    // Test with various fill factors
    double[] fillFactors = {0.50, 0.80, 0.90, 0.99, 1.00};
    for (double fillFactor : fillFactors) {
      LinearProbingHashTable linearHashTable = new LinearProbingHashTable();
      DoubleHashingHashTable doubleHashTable = new DoubleHashingHashTable();
      testHashTables(fillFactor, linearHashTable, doubleHashTable);
      System.out.println("------------------------------------");
    }
  }
}

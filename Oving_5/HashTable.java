import java.util.LinkedList;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class represents a hash table implementation using linked lists for collision resolution.
 * It provides basic operations to insert and check the presence of keys.
 */
public class HashTable {

  /** The size of the hash table. */
  private final int SIZE;

  /** The actual table, which is an array of linked lists. */
  private LinkedList<String>[] table;

  /** Number of elements inserted into the table. */
  private int numOfElements;

  /** Counter for the number of collisions that have occurred. */
  private int numOfCollisions;

  /**
   * Constructs a new HashTable.
   *
   * @param size The initial size of the hash table.
   */
  @SuppressWarnings("unchecked")
  public HashTable(int size) {
    this.SIZE = size;
    table = (LinkedList<String>[]) new LinkedList[SIZE];
    for (int i = 0; i < SIZE; i++) {
      table[i] = new LinkedList<>();
    }
    this.numOfElements = 0;
    this.numOfCollisions = 0;
  }

  /**
   * The primary hash function.
   *
   * @param key The key to be hashed.
   * @return The hash value.
   */
  private int hash(String key) {
    int hashValue = 0;
    for (int i = 0; i < key.length(); i++) {
      hashValue = (hashValue * 37 + key.charAt(i)) % SIZE;
    }
    return hashValue;
  }

  /**
   * Inserts a key into the hash table.
   *
   * @param key The key to be inserted.
   */
  public void put(String key) {
    int index = hash(key);
    if (!table[index].isEmpty()) {
      numOfCollisions++;
      System.out.println("Collision occurred for: " + key + " with " + table[index].getFirst());
    }
    table[index].add(key);
    numOfElements++;
  }

  /**
   * Checks if a key is present in the hash table.
   *
   * @param key The key to be checked.
   * @return true if the key is present, false otherwise.
   */
  public boolean contains(String key) {
    int index = hash(key);
    return table[index].contains(key);
  }

  /**
   * Reads names from a file and inserts them into the hash table.
   *
   * @param filename The path to the file.
   */
  public void readFromFileAndInsert(String filename) {
    try {
      List<String> lines = Files.readAllLines(Paths.get(filename));
      for (String line : lines) {
        put(line.trim());
      }
    } catch (Exception e) {
      System.out.println("Error reading the file: " + e.getMessage());
    }
  }

  /**
   * Returns the load factor of the hash table.
   *
   * @return The current load factor.
   */
  public double getLoadFactor() {
    return (double) numOfElements / SIZE;
  }

  /**
   * Calculates the average number of collisions per person.
   *
   * @return The average collisions per person.
   */
  public double getCollisionsPerPerson() {
    return (double) numOfCollisions / numOfElements;
  }

  /**
   * Checks if a number is prime.
   *
   * @param n The number to be checked.
   * @return true if the number is prime, false otherwise.
   */
  private static boolean isPrime(int n) {
    if (n <= 1) return false;
    if (n <= 3) return true;
    if (n % 2 == 0 || n % 3 == 0) return false;

    int i = 5;
    while (i * i <= n) {
      if (n % i == 0 || n % (i + 2) == 0) return false;
      i += 6;
    }
    return true;
  }

  /**
   * Finds the next prime number after the given number.
   *
   * @param n The starting number.
   * @return The next prime number.
   */
  private static int nextPrime(int n) {
    while (!isPrime(n)) {
      n++;
    }
    return n;
  }

  /**
   * Main method to test the performance and functionality of the hash table.
   *
   * @param args Command-line arguments (not used).
   */
  public static void main(String[] args) {
    int estimatedSize = 2 * 135;
    int optimizedSize = nextPrime(estimatedSize);
    HashTable ht = new HashTable(optimizedSize);

    //TODO: Fiks bedre løsning enn dette:)
    String filename = "C:\\Users\\sande\\OneDrive\\Dokumenter\\Skole\\H23\\AlgDat\\Oving_5\\navn.txt";
    ht.readFromFileAndInsert(filename);

    System.out.println("Load Factor: " + ht.getLoadFactor());
    System.out.println("Total Collisions: " + ht.numOfCollisions);
    System.out.println("Collisions Per Person: " + ht.getCollisionsPerPerson());

    // Test: Check if all names from the file are in the hash table
    try {
      List<String> lines = Files.readAllLines(Paths.get(filename));
      int notFoundCount = 0;
      for (String line : lines) {
        if (!ht.contains(line.trim())) {
          notFoundCount++;
          System.out.println("Name not found in hash table: " + line.trim());
        }
      }
      if (notFoundCount == 0) {
        System.out.println("All names from the file are present in the hash table!");
      } else {
        System.out.println("Some names from the file are missing in the hash table.");
      }
    } catch (Exception e) {
      System.out.println("Error reading the file for testing: " + e.getMessage());
    }

    // Test: Check if lookup function works
    if (ht.contains("Sander Rom Skofsrud")) {
      System.out.println("Name lookup successful!");
    } else {
      System.out.println("Name lookup failed.");
    }
  }
}

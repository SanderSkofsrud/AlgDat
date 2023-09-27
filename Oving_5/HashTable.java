import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

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
  public HashTable(int size) {
    this.SIZE = size;
    table = new LinkedList[SIZE];
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
    if (n % 2 == 0) n++;
    while (!isPrime(n)) {
      n += 2;
    }
    return n;
  }

  /**
   * Reads names from a URL and inserts them into the hash table.
   *
   * @param fileURL The URL to the file.
   */
  public void readFromURLAndInsert(String fileURL) {
    try {
      URL url = new URL(fileURL);
      Scanner scanner = new Scanner(url.openStream());
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        put(line.trim());
      }
      scanner.close();
    } catch (Exception e) {
      System.out.println("Error reading the file: " + e.getMessage());
    }
  }

  /**
   * Reads content from a URL and returns it as a list of strings.
   *
   * @param fileURL The URL to the file.
   * @return List of strings read from the URL.
   */
  public static List<String> readLinesFromURL(String fileURL) {
    List<String> lines = new ArrayList<>();
    try {
      URL url = new URL(fileURL);
      Scanner scanner = new Scanner(url.openStream());
      while (scanner.hasNextLine()) {
        lines.add(scanner.nextLine().trim());
      }
      scanner.close();
    } catch (Exception e) {
      System.out.println("Error reading the file: " + e.getMessage());
    }
    return lines;
  }

  /**
   * Main method to test the performance and functionality of the hash table.
   *
   * @param args Command-line arguments (not used).
   */
  public static void main(String[] args) {
    int estimatedSize = (int) Math.floor(1.25 * 135);
    int optimizedSize = nextPrime(estimatedSize);
    HashTable ht = new HashTable(optimizedSize);

    ht.readFromURLAndInsert("https://www.idi.ntnu.no/emner/idatt2101/hash/navn.txt");

    System.out.println("Load Factor: " + ht.getLoadFactor());
    System.out.println("Total Collisions: " + ht.numOfCollisions);
    System.out.println("Collisions Per Person: " + ht.getCollisionsPerPerson());

    // Test: Check if all names from the file are in the hash table
    try {
      List<String> lines = readLinesFromURL("https://www.idi.ntnu.no/emner/idatt2101/hash/navn.txt");
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

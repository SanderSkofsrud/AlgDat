/**
 * The JosephusCircle class provides a solution to the Josephus problem using a circular linked list.
 * The problem is described as follows:
 * A group of people stand in a circle. Starting with a given person, you count around the circle
 * n times, removing every n-th person. This continues until only one person remains.
 *
 * The time complexity for constructing the circle is O(n) and for solving the Josephus problem is O(nm),
 * where n is the number of people and m is the interval at which people are removed.
 */
public class JosephusCircle {

  /**
   * The Node class represents an element in the circular linked list used for the Josephus problem.
   */
  private class Node {
    int data;
    Node next;

    /**
     * Constructs a new Node with the given data.
     *
     * @param data The data contained in the node.
     */
    public Node(int data) {
      this.data = data;
    }
  }

  private Node head;

  /**
   * Constructs a new JosephusCircle with n people.
   * Time complexity: O(n)
   *
   * @param n The number of people in the circle.
   */
  public JosephusCircle(int n) {
    if (n <= 0) return;

    head = new Node(1);
    Node current = head;

    for (int i = 2; i <= n; i++) {
      current.next = new Node(i);
      current = current.next;
    }

    // Make the list circular by pointing the last node back to the head
    current.next = head;
  }

  /**
   * Solves the Josephus problem for the given interval m.
   * Time complexity: O(nm)
   *
   * @param m The interval at which people are removed from the circle.
   * @return The position that is safe (the position of the last remaining person).
   */
  public int solve(int m) {
    Node ptr = head;
    Node prev = null;

    System.out.println("Initial circle:");
    printCircle();

    while (ptr.next != ptr) {
      for (int count = 1; count < m; count++) {
        prev = ptr;
        ptr = ptr.next;
      }
      System.out.println("Person at position " + ptr.data + " is removed.");
      if(ptr == head) { // If the current node to be removed is the head
        head = ptr.next;
      }
      prev.next = ptr.next;  // Remove the node
      ptr = prev.next;

      System.out.println("Circle after removal:");
      printCircle();
    }

    return ptr.data;
  }

  /**
   * Helper method to print the current state of the circle.
   */
  private void printCircle() {
    if (head == null) return;

    Node temp = head;
    do {
      System.out.print(temp.data + " ");
      temp = temp.next;
    } while (temp != head);
    System.out.println();
  }

  /**
   * Main method to demonstrate the solution to the Josephus problem.
   *
   * @param args Command line arguments (not used).
   */
  public static void main(String[] args) {
    int n = 10; // Number of people
    int m = 4;  // Interval

    JosephusCircle circle = new JosephusCircle(n);
    int safePosition = circle.solve(m);

    System.out.println("Josephus should stand at position: " + safePosition);
  }
}

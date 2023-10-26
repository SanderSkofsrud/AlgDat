import java.io.*;
import java.util.PriorityQueue;

/**
 * The Compress class provides methods for compressing and decompressing files
 * using a combination of Huffman coding and LZ77 (if implemented). TODO: Endre fra LZ77 hvis ikke dette brukes
 */
public class Compress {

  /**
   * The Node class represents nodes in the Huffman tree.
   * Each node has a value (frequency of character) and a character associated with it.
   */
  static class Node implements Comparable<Node> {
    final int value;
    final char ch;
    Node left = null;
    Node right = null;

    /**
     * Constructs a new node with the given value and character.
     */
    Node(int value, char ch) {
      this.value = value;
      this.ch = ch;
    }

    /**
     * Constructs a new node by merging two nodes (used for Huffman tree construction).
     */
    Node(Node left, Node right) {
      this.value = left.value + right.value;
      this.ch = '\0';
      this.left = left;
      this.right = right;
    }

    @Override
    public int compareTo(Node that) {
      return this.value - that.value;
    }
  }

  /**
   * Builds a Huffman tree based on the provided frequencies.
   *
   * @param freq The frequencies of each character.
   * @return The root node of the Huffman tree.
   */
  private static Node buildTree(int[] freq) {
    PriorityQueue<Node> nodes = new PriorityQueue<>();

    for (char i = 0; i < 256; i++) {
      if (freq[i] > 0) {
        nodes.offer(new Node(freq[i], i));
      }
    }

    while (nodes.size() > 1) {
      Node left = nodes.poll();
      Node right = nodes.poll();
      Node parent = new Node(left, right);
      nodes.offer(parent);
    }
    return nodes.poll();
  }

  /**
   * Generates Huffman codes for each character by traversing the Huffman tree.
   *
   * @param root The root node of the Huffman tree.
   * @param prefix The current Huffman code being built.
   * @param st The array storing the Huffman code for each character.
   */
  private static void writeCodes(Node root, StringBuilder prefix, String[] st) {
    if (!isLeaf(root)) {
      writeCodes(root.left, prefix.append('0'), st);
      prefix.deleteCharAt(prefix.length() - 1);

      writeCodes(root.right, prefix.append('1'), st);
      prefix.deleteCharAt(prefix.length() - 1);
    } else {
      st[root.ch] = prefix.toString();
    }
  }

  /**
   * Checks if the given node is a leaf node in the Huffman tree.
   *
   * @param x The node to check.
   * @return True if the node is a leaf, otherwise false.
   */
  private static boolean isLeaf(Node x) {
    return (x.left == null && x.right == null);
  }

  /**
   * Compresses the input file using Huffman coding and writes the compressed data to the output file.
   *
   * @param inputFile The path to the file to be compressed.
   * @param outputFile The path to the file to write the compressed data.
   * @throws IOException if an I/O error occurs.
   */
  public static void compress(String inputFile, String outputFile) throws IOException {
    // TODO: Integrate LZ77 compression before this step, if required.

    InputStream is = new FileInputStream(inputFile);
    int[] freq = new int[256];

    int byteValue;
    while ((byteValue = is.read()) != -1) {
      freq[byteValue]++;
    }

    Node root = buildTree(freq);
    String[] st = new String[256];
    StringBuilder prefix = new StringBuilder();
    writeCodes(root, prefix, st);

    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile));
    oos.writeObject(freq);

    is.close();
    is = new FileInputStream(inputFile);
    BitOutputStream bos = new BitOutputStream(oos);

    while ((byteValue = is.read()) != -1) {
      String code = st[byteValue];
      for (int i = 0; i < code.length(); i++) {
        if (code.charAt(i) == '0') {
          bos.write(false);
        } else if (code.charAt(i) == '1') {
          bos.write(true);
        }
      }
    }
    is.close();
    bos.close();
  }

  /**
   * Decompresses the input file using Huffman coding and writes the decompressed data to the output file.
   *
   * @param inputFile The path to the file to be decompressed.
   * @param outputFile The path to the file to write the decompressed data.
   * @throws IOException if an I/O error occurs.
   * @throws ClassNotFoundException if the class of a serialized object cannot be found.
   */
  public static void decompress(String inputFile, String outputFile) throws IOException, ClassNotFoundException {
    // TODO: Integrate LZ77 decompression after this step, if required.

    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFile));
    int[] freq = (int[]) ois.readObject();
    Node root = buildTree(freq);

    int length = 0;
    for (int j : freq) {
      length += j;
    }

    BitInputStream bis = new BitInputStream(ois);
    OutputStream os = new FileOutputStream(outputFile);

    while (length > 0) {
      Node x = root;
      while (!isLeaf(x)) {
        boolean bit = bis.read();
        if (bit) x = x.right;
        else     x = x.left;
      }
      os.write(x.ch);
      length--;
    }
    os.close();
    bis.close();
  }

  // Nested classes for bit manipulation

  /**
   * The BitOutputStream class provides methods to write individual bits to an ObjectOutputStream.
   */
  static class BitOutputStream implements AutoCloseable {
    private final ObjectOutputStream oos;
    private int currentByte;
    private int numBitsFilled;

    public BitOutputStream(ObjectOutputStream oos) {
      this.oos = oos;
      this.currentByte = 0;
      this.numBitsFilled = 0;
    }

    /**
     * Writes a bit to the stream.
     *
     * @param bit The bit to write (true for 1, false for 0).
     * @throws IOException if an I/O error occurs.
     */
    public void write(boolean bit) throws IOException {
      if (bit) {
        currentByte |= (1 << (7 - numBitsFilled));
      }
      numBitsFilled++;
      if (numBitsFilled == 8) {
        oos.write(currentByte);
        currentByte = 0;
        numBitsFilled = 0;
      }
    }

    @Override
    public void close() throws IOException {
      while (numBitsFilled != 0) {
        write(false);
      }
      oos.close();
    }
  }

  /**
   * The BitInputStream class provides methods to read individual bits from an ObjectInputStream.
   */
  static class BitInputStream implements AutoCloseable {
    private final ObjectInputStream ois;
    private int currentByte;
    private int numBitsRemaining;

    public BitInputStream(ObjectInputStream ois) throws IOException {
      this.ois = ois;
      currentByte = 0;
      numBitsRemaining = 0;
    }

    /**
     * Reads a bit from the stream.
     *
     * @return True if the bit is 1, otherwise false.
     * @throws IOException if an I/O error occurs.
     */
    public boolean read() throws IOException {
      if (numBitsRemaining == 0) {
        currentByte = ois.read();
        if (currentByte == -1) {
          throw new EOFException();
        }
        numBitsRemaining = 8;
      }
      numBitsRemaining--;
      return ((currentByte >>> numBitsRemaining) & 1) == 1;
    }

    @Override
    public void close() throws IOException {
      ois.close();
    }
  }

  /**
   * The main method to test the compression and decompression.
   *
   * @param args The command line arguments.
   */
  public static void main(String[] args) {
    String action = args[0];
    String inputFile = args[1];
    String outputFile = args[2];

    try {
      if ("compress".equals(action) || "c".equals(action)) {
        compress(inputFile, outputFile);
      } else if ("decompress".equals(action) || "d".equals(action)) {
        decompress(inputFile, outputFile);
      } else {
        System.out.println("Unknown action");
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
}

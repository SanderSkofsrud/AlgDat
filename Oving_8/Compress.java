import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * The Compress class provides methods for compressing and decompressing files
 * using a combination of Huffman coding and LZ78.
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
    // LZ78 compression
    String inputData = new String(Files.readAllBytes(Paths.get(inputFile)));
    List<LZ78.Token> lz78Tokens = LZ78.compress(inputData);
    byte[] lz78CompressedData = LZ78.serializeLZ78Tokens(lz78Tokens);

    // Frequency calculation for Huffman compression on LZ78 bytes
    int[] freq = new int[256];
    for (byte b : lz78CompressedData) {
      freq[b & 0xFF]++;
    }

    // The root node of the Huffman tree.
    Node root = buildTree(freq);
    String[] st = new String[256];
    StringBuilder prefix = new StringBuilder();
    writeCodes(root, prefix, st);

    // Write the frequency table and Huffman codes to the output file
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))) {
      oos.writeObject(freq);

      // Huffman compression of LZ78 bytes
      BitOutputStream bos = new BitOutputStream(oos);
      long totalBytes = lz78CompressedData.length;
      long processedBytes = 0;

      // Initialize progress bar with 0% completion
      printProgressBar(processedBytes, totalBytes);

      for (byte b : lz78CompressedData) {
        String code = st[b & 0xFF];
        for (int i = 0; i < code.length(); i++) {
          bos.write(code.charAt(i) == '1');
        }

        // Update progress after each byte is processed
        processedBytes++;
        printProgressBar(processedBytes, totalBytes);
      }
      bos.close();

      // Final call to ensure the progress bar shows 100%
      printProgressBar(totalBytes, totalBytes);
      System.out.println(); // Print a new line after progress bar
    }
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
    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFile));
    int[] freq = (int[]) ois.readObject();
    Node root = buildTree(freq);

    BitInputStream bis = new BitInputStream(ois);
    ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

    long totalBytes = 0;
    long processedBytes = 0;

    // Calculate the total bytes to be processed
    for (int f : freq) {
        totalBytes += f;
    }

    // Huffman decompression
    while (true) {
        try {
            Node x = root;
            while (!isLeaf(x)) {
                boolean bit = bis.read();
                if (bit) x = x.right;
                else x = x.left;
            }
            byteOutput.write(x.ch);

            // Update progress after each byte is processed
            processedBytes++;
            printProgressBar(processedBytes, totalBytes);
        } catch (EOFException e) {
            break;
        }
    }
    bis.close();
    byte[] lz77CompressedData = byteOutput.toByteArray();

    // LZ78 decompression
    List<LZ78.Token> lz77Tokens = LZ78.deserializeLZ78Tokens(lz77CompressedData);
    String lz77DecompressedData = LZ78.decompress(lz77Tokens);
    Files.write(Paths.get(outputFile), lz77DecompressedData.getBytes());

    // Final call to ensure the progress bar shows 100%
    printProgressBar(totalBytes, totalBytes);
    System.out.println();
    System.out.println("Decompression complete"+ "\n");
    System.out.println("Run command: 'diff + filname + filname' to check for differences");
    System.out.println("If there is no output, the files are identical");
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
    /**
     * Closes the stream.
     */

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
    /**
     * Constructs a new BitInputStream from the given ObjectInputStream.
     * 
     * @param ois The ObjectInputStream to read from.
     * @throws IOException if an I/O error occurs.
     */

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
    /**
     * Closes the stream.
     */

    @Override
    public void close() throws IOException {
      ois.close();
    }
  }




  /**
   * Utility method to print a progress bar with color.
   *
   * @param completed The number of bytes processed.
   * @param total     The total number of bytes.
   */
  public static void printProgressBar(long completed, long total) {
    int barLength = 50; // Length of the progress bar
    int filledLength = (int) (barLength * completed / total);

    StringBuilder bar = new StringBuilder("\r[");
    for (int i = 0; i < barLength; i++) {
      if (i < filledLength) {
        bar.append("\u001B[42m \u001B[0m"); // Green background for completed portion
      } else {
        bar.append("\u001B[41m \u001B[0m"); // Red background for incomplete portion
      }
    }
    bar.append("] ").append(100 * completed / total).append("%");
    System.out.print(bar);
  }

  
  /**
   * The main method to test the compression and decompression.
   *
   * @param args The command line arguments.
   * args[0] = "compress" or "decompress" or "c" or "d"
   * args[1] = input file
   * args[2] = output file (will be overwritten)
   */
  public static void main(String[] args) {

    
     if (args.length != 3) {
      System.out.println("Usage: java Compress.java <action> <input file> <output file>");
      return;
    }
    
    String action = args[0];
    String inputFile = args[1];
    String outputFile = args[2];

    try {
      if ("compress".equals(action) || "c".equals(action)) {

        long inputFileSize = Files.size(Paths.get(inputFile));
        System.out.println("Original file size: " + inputFileSize + " bytes");
        compress(inputFile, outputFile);
        long outputFileSize = Files.size(Paths.get(outputFile));
        System.out.println("Compressed file size: " + outputFileSize + " bytes");
        System.out.printf("Compression ratio: %.2f%%", (double) outputFileSize / inputFileSize * 100);
      } else if ("decompress".equals(action) || "d".equals(action)) {
        decompress(inputFile, outputFile);
      } else {
        System.out.println("Unknown action");
      }


    } catch (IOException | ClassNotFoundException | IllegalArgumentException e) {
      e.printStackTrace();
    }
  }
  /**
   * The LZ78 class provides methods for compressing and decompressing text using LZ78.
   * The compress method returns a list of LZ78 tokens, and the decompress method
   * takes a list of LZ78 tokens and returns the original text.
   */

public class LZ78 {
    static class Token {
        final int index;
        final char nextChar;

        /**
         * Constructs a new LZ78 token.
         * 
         * @param index The index of the token.
         * @param nextChar The next character of the token.
         */

        Token(int index, char nextChar) {
            this.index = index;
            this.nextChar = nextChar;
        }

        /**
         * Returns a string representation of the token.
         */

        @Override
        public String toString() {
            return "(" + index + ", '" + nextChar + "')";
        }
    }

    /**
     * Serializes a list of LZ78 tokens to a byte array.
     * 
     * @param tokens The list of tokens to serialize.
     * @return The serialized byte array.
     */
    public static byte[] serializeLZ78Tokens(List<LZ78.Token> tokens) {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    DataOutputStream dataStream = new DataOutputStream(byteStream);

    try {
        for (LZ78.Token token : tokens) {
            dataStream.writeInt(token.index);
            dataStream.writeChar(token.nextChar);
        }
    } catch (IOException e) {
        throw new RuntimeException("Error during LZ78 token serialization", e);
    }

    return byteStream.toByteArray();
}
/**
 * Deserializes a list of LZ78 tokens from a byte array.
 * 
 * @param data The byte array to deserialize.
 * @return The list of tokens.
 */
public static List<LZ78.Token> deserializeLZ78Tokens(byte[] data) {
  ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
  DataInputStream dataStream = new DataInputStream(byteStream);
  List<LZ78.Token> tokens = new ArrayList<>();

  try {
      while (dataStream.available() > 0) {
          int index = dataStream.readInt();
          char nextChar = dataStream.readChar();
          tokens.add(new LZ78.Token(index, nextChar));
      }
  } catch (IOException e) {
      throw new RuntimeException("Error during LZ78 token deserialization", e);
  }

  return tokens;
}




    /**
     * Compresses a string using LZ78.
     * 
     * @param input The string to compress.
     * @return A list of LZ78 tokens.
     */

     public static List<Token> compress(String input) {
      List<Token> tokens = new ArrayList<>();
      List<String> dict = new ArrayList<>();
      String current = "";
  
      for (char c : input.toCharArray()) {
          String extended = current + c;
          int index = dict.indexOf(extended);
  
          // If index == -1, the substring is not in the dictionary
          if (index == -1) {
              int currentIndex = dict.indexOf(current);
              tokens.add(new Token(currentIndex == -1 ? 0 : currentIndex + 1, c));
              dict.add(extended);
              current = "";
          } else {
              current = extended;
          }
      }
      // Handling the last substring (if any)
      if (!current.isEmpty()) {
          int currentIndex = dict.indexOf(current);
          tokens.add(new Token(currentIndex == -1 ? 0 : currentIndex + 1, '\0'));
      }
      return tokens;
  }
    /**
     * Decompresses a list of LZ78 tokens.
     * 
     * @param tokens The list of tokens to decompress.
     * @return The decompressed string.
     */
    public static String decompress(List<Token> tokens) {
        List<String> dict = new ArrayList<>();
        dict.add("");  // Placeholder for index 0

        StringBuilder output = new StringBuilder();
        for (Token token : tokens) {
            String previous = dict.get(token.index);
            String current = previous + token.nextChar;
            dict.add(current);
            output.append(current);
        }
        return output.toString();
    }
  }
}
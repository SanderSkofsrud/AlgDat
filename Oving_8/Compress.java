import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * The Compress class provides methods for compressing and decompressing files
 * using a combination of Huffman coding and LZ77 (if implemented).
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
    // LZ77 compression
    String inputData = new String(Files.readAllBytes(Paths.get(inputFile)));
    List<LZ77.Token> lz77Tokens = LZ77.compress(inputData);
    byte[] lz77CompressesData = LZ77.getCompressedBytes(lz77Tokens);

    // Frequency calculation for Huffman compression on LZ77 bytes
    int[] freq = new int[256];
    for (byte b : lz77CompressesData) {
      freq[b & 0xFF]++;
    }
    Node root = buildTree(freq);
    String[] st = new String[256];
    StringBuilder prefix = new StringBuilder();
    writeCodes(root, prefix, st);

    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile));
    oos.writeObject(freq);

    // Huffman compression of LZ77 bytes
    BitOutputStream bos = new BitOutputStream(oos);
    for (byte b : lz77CompressesData) {
      String code = st[b & 0xFF];
      for (int i = 0; i < code.length(); i++) {
        bos.write(code.charAt(i) == '1');
      }
    }
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
    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFile));
    int[] freq = (int[]) ois.readObject();
    Node root = buildTree(freq);

    BitInputStream bis = new BitInputStream(ois);
    ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

    // Huffman decompression
    while (true) {
      try {
        Node x = root;
        while (!isLeaf(x)) {
          boolean bit = bis.read();
          if (bit) x = x.right;
          else     x = x.left;
        }
        byteOutput.write(x.ch);
      } catch (EOFException e) {
        break;
      }
    }
    bis.close();
    byte[] lz77CompressedData = byteOutput.toByteArray();

    // LZ77 decompression
    List<LZ77.Token> lz77Tokens = LZ77.getTokensFromBytes(lz77CompressedData);
    String lz77DecompressedData = LZ77.decompress(lz77Tokens);
    Files.write(Paths.get(outputFile), lz77DecompressedData.getBytes());

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
  /**
   * The LZ77 class provides methods for compressing and decompressing text using LZ77.
   */
  public class LZ77 {
    // Might want to tweak the values below
    private static final int WINDOW_SIZE = 4096;
    private static final int MAX_MATCH_LENGTH = 15;
    private static final int MIN_MATCH_LENGTH = 3;

    /**
     * Compresses the given text using LZ77.
     * 
     * @param text The text to compress.
     * @return A list of tokens representing the compressed text.
     */

    public static List<Token> compress(String text) {
        List<Token> tokens = new ArrayList<>();
        int index = 0;
        while (index < text.length()) {
            int bestMatchDistance = -1;
            int bestMatchLength = -1;
            int maxLookback = Math.min(index, WINDOW_SIZE);

            for (int beginIndex = index - maxLookback; beginIndex < index; beginIndex++) {
                int matchLength = 0;
                while (matchLength < MAX_MATCH_LENGTH &&
                       index + matchLength < text.length() &&
                       text.charAt(beginIndex + matchLength) == text.charAt(index + matchLength)) {
                    matchLength++;
                }

                if (matchLength > bestMatchLength) {
                    bestMatchLength = matchLength;
                    bestMatchDistance = index - beginIndex;
                }
            }

            if (bestMatchLength >= MIN_MATCH_LENGTH) {
              Token token = new Token(bestMatchDistance, bestMatchLength, text.charAt(index + bestMatchLength));
              System.out.println("Token: " + token);
              tokens.add(token);
              index += bestMatchLength + 1;
            } else {
              Token token = new Token(0, 0, text.charAt(index));
              System.out.println("Token: " + token);
              tokens.add(token);
              index++;
            }
        }
        return tokens;
    }
    /**
     * Converts the given list of tokens to a byte array.
     * 
     * @param tokens The tokens to convert.
     * @return A byte array representing the tokens.
     */
    public static byte[] getCompressedBytes(List<Token> tokens) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  
      for (Token token : tokens) {
          int highByte = ((token.matchDistance << 4) & 0xFF00) | ((token.matchLength >> 0) & 0x0F);
          int lowByte = ((token.matchLength << 4) & 0xF0) | ((token.nextChar >> 8) & 0x0F);
          outputStream.write(highByte);
          outputStream.write(lowByte);
          outputStream.write(token.nextChar & 0xFF);
      }
  
      return outputStream.toByteArray();
  }
  
    /**
     * Converts the given byte array to a list of tokens.
     * 
     * @param data The byte array to convert.
     * @return A list of tokens.
     */
    public static List<Token> getTokensFromBytes(byte[] bytes) {
      List<Token> tokens = new ArrayList<>();
  
      for (int i = 0; i < bytes.length; i += 3) {
          int highByte = bytes[i] & 0xFF;
          int lowByte = bytes[i + 1] & 0xFF;
          int thirdByte = bytes[i + 2] & 0xFF;
  
          int matchDistance = (highByte >> 4) & 0xFF;
          int matchLength = ((highByte & 0x0F) << 4) | ((lowByte >> 4) & 0x0F);
          char nextChar = (char) (((lowByte & 0x0F) << 8) | thirdByte);
  
          tokens.add(new Token(matchDistance, matchLength, nextChar));
      }
  
      return tokens;
  }
  
  
    
    /**
     * Decompresses the given tokens using LZ77.
     * 
     * @param tokens The tokens to decompress.
     * @return The decompressed text.
     */
    public static String decompress(List<Token> tokens) {
        StringBuilder decompressed = new StringBuilder();
        for (Token token : tokens) {
            if (token.matchLength > 0) {
                int startIndex = decompressed.length() - token.matchDistance;
                if (startIndex < 0 || startIndex >= decompressed.length() || startIndex + token.matchLength > decompressed.length()) {
                  System.out.println("Decompressed Length: " + decompressed.length());
                  System.out.println("Start Index: " + startIndex);
                  System.out.println("Token Match Length: " + token.matchLength);
                  System.out.println("Token: " + token);     
                  throw new IllegalArgumentException("Invalid token: " + token);
                }
                for (int i = 0; i < token.matchLength; i++) {
                    decompressed.append(decompressed.charAt(startIndex + i));
                }
            }
            decompressed.append(token.nextChar);
        }
        return decompressed.toString();
    }
    
    /**
     * The Token class represents a token in the compressed text.
     * Each token has a match distance, match length and next character.
     */
    static class Token {
        final int matchDistance;
        final int matchLength;
        final char nextChar;

        /**
         * Constructs a new token with the given match distance, match length and next character.
         * 
         * @param matchDistance The distance to the start of the matching string.
         * @param matchLength The length of the matching string.
         * @param nextChar The next character in the text.
         */
        Token(int matchDistance, int matchLength, char nextChar) {
            this.matchDistance = matchDistance;
            this.matchLength = matchLength;
            this.nextChar = nextChar;
        }
        @Override
        public String toString() {
          return "Token [matchDistance=" + matchDistance + ", matchLength=" + matchLength + ", nextChar=" + nextChar + "]";
    }
    }
}
}

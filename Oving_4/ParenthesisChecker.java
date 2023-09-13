import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

/**
 * The ParenthesisChecker class provides a method to check if a given source code file
 * has the correct nesting and pairing of parentheses, brackets, and braces.
 *
 * Save the code in a file named ParenthesisChecker.java.
 * Compile the code with the command javac ParenthesisChecker.java.
 * Run the program with the command java ParenthesisChecker <filename>, where <filename> is the name of the source code you want to check.
 */
public class ParenthesisChecker {

  /**
   * The main entry point for the application. It expects a filename as an argument.
   * Time complexity: O(n), where n is the number of characters in the file.
   *
   * @param args Command line arguments. The first argument should be the filename.
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Please provide a file to check.");
      return;
    }

    String filename = args[0];
    try {
      checkParenthesis(filename);
    } catch (IOException e) {
      System.out.println("An error occurred while reading the file: " + e.getMessage());
    }
  }

  /**
   * Checks if the provided file has the correct nesting and pairing of parentheses.
   * Any mismatch or incorrect nesting is printed to the console.
   * Time complexity: O(n), where n is the number of characters in the file.
   *
   * @param filename The name of the file to check.
   * @throws IOException If there's an error reading the file.
   */
  public static void checkParenthesis(String filename) throws IOException {
    Stack<Character> stack = new Stack<>();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      int lineNum = 0;
      String line;
      while ((line = br.readLine()) != null) {
        lineNum++;
        for (char ch : line.toCharArray()) {
          // If the character is an opening parenthesis, push to stack
          if (ch == '(' || ch == '[' || ch == '{') {
            stack.push(ch);
          }
          // If the character is a closing parenthesis, check for its matching pair
          else if (ch == ')' || ch == ']' || ch == '}') {
            if (stack.isEmpty()) {
              System.out.println("Error on line " + lineNum + ": Extra " + ch);
              return;
            }

            char top = stack.pop();
            if (!isMatchingPair(top, ch)) {
              System.out.println("Error on line " + lineNum + ": Mismatch between " + top + " and " + ch);
              return;
            }
          }
        }
      }

      // If there are unmatched opening parentheses left
      if (!stack.isEmpty()) {
        System.out.println("Error: Missing closing parenthesis for " + stack.pop());
        return;
      }
    }

    System.out.println("No errors found in the file.");
  }

  /**
   * Checks if the provided characters form a matching pair of parentheses.
   * Time complexity: O(1).
   *
   * @param open The opening parenthesis character.
   * @param close The closing parenthesis character.
   * @return true if they form a matching pair, false otherwise.
   */
  public static boolean isMatchingPair(char open, char close) {
    return (open == '(' && close == ')') ||
            (open == '[' && close == ']') ||
            (open == '{' && close == '}');
  }
}

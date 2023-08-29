/**
 * This class provides two recursive methods for multiplication.
 */
public class RecursiveMultiplication {

    /**
     * Multiplies a number using recursive summation.
     * Time Complexity: Θ(n)
     *
     * @param n The number of times x should be added to itself.
     * @param x The number to be added recursively.
     * @return The product of n and x.
     */
    public static double recursiveMultiplication1(int n, double x) {
        if (n == 1) {
            return x;
        }
        return x + recursiveMultiplication1(n - 1, x);
    }

    /**
     * Multiplies a number using a more efficient recursive method for even n values.
     * Time Complexity: Θ(log n) for even n and Θ(n) for odd n.
     *
     * @param n The number of times x should be multiplied.
     * @param x The number to be multiplied recursively.
     * @return The product of n and x.
     */
    public static double recursiveMultiplication2(int n, double x) {
        if (n == 1) {
            return x;
        }
        if ((n & 1) == 0) { // Even n
            return recursiveMultiplication2(n / 2, x + x);
        } else { // Odd n
            return x + recursiveMultiplication2(n - 1, x);
        }
    }

    /**
     * The main method that tests the multiplication methods.
     *
     * @param args The command line arguments (not used).
     */
    public static void main(String[] args) {
        double x = Math.random() * 10000;

        // Test cases
        testMultiplication(5, x);
        testMultiplication(10, x);
        testMultiplication(50, x);
        testMultiplication(100, x);
        testMultiplication(500, x);
        testMultiplication(2500, x);
    }

    /**
     * Tests and prints the results of the multiplication methods, 
     * as well as the time taken by each method.
     *
     * @param n The number of times x should be multiplied.
     * @param x The number to be multiplied.
     */
    public static void testMultiplication(int n, double x) {
        long startTime, endTime;

        // Method 1
        startTime = System.nanoTime();
        double result1 = recursiveMultiplication1(n, x);
        endTime = System.nanoTime();
        System.out.println("Method 1: " + n + " * " + x + " = " + result1);
        System.out.println("Time taken by method 1: " + (endTime - startTime) / 1_000_000.0 + " milliseconds");
        System.out.println("");

        // Method 2
        startTime = System.nanoTime();
        double result2 = recursiveMultiplication2(n, x);
        endTime = System.nanoTime();
        System.out.println("Method 2: " + n + " * " + x + " = " + result2);
        System.out.println("Time taken by method 2: " + (endTime - startTime) / 1_000_000.0 + " milliseconds");
        System.out.println("");

    }
}

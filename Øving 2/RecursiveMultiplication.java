public class RecursiveMultiplication {

    public static double recursiveMultiplication1(int n, double x) {
        if (n == 1) {
            return x;
        }
        return x + recursiveMultiplication1(n - 1, x);
    }

    public static double recursiveMultiplication2(int n, double x) {
        if (n == 1) {
            return x;
        }
        if ((n & 1) == 0) { // Even
            return recursiveMultiplication2(n / 2, x + x);
        } else { // Odd
            return (x / (n - 1)) + recursiveMultiplication2((n - 1) / 2, x + x) + recursiveMultiplication2((n - 1) / 2, x + x);
        }
    }

    public static void main(String[] args) {
        // Test cases
        testMultiplication(13, 2.5);
        testMultiplication(14, 10.1);
        
        // Time measurement
        long startTime, endTime;
        
        // Metode 1
        startTime = System.nanoTime();
        recursiveMultiplication1(4000, 2.5);
        endTime = System.nanoTime();
        System.out.println("Time taken by method 1: " + (endTime - startTime) + " nanoseconds");
        
        // Metode 2
        startTime = System.nanoTime();
        recursiveMultiplication2(4000, 2.5);
        endTime = System.nanoTime();
        System.out.println("Time taken by method 2: " + (endTime - startTime) + " nanoseconds");
    }

    public static void testMultiplication(int n, double x) {
        double result1 = recursiveMultiplication1(n, x);
        double result2 = recursiveMultiplication2(n, x);
        
        System.out.println("Method 1: " + n + " * " + x + " = " + result1);
        System.out.println("Method 2: " + n + " * " + x + " = " + result2);
    }
}

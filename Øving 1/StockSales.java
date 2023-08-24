import java.util.Random;

public class StockSales {

    private static Random random = new Random();

    /*
     * Method that takes an array of stock prices and returns the best day to buy and sell for maximum profit.
     * The method returns a string with the following format:
     * "Buy on day X and sell on day Y for a profit of $Z"
     * 
     * @param stockPrices An array of stock prices
     * @return A string with the best day to buy and sell for maximum profit
     */

    private static String maxProfit(int[] stockPrices) {
        if (stockPrices.length == 0) {
            return "No prices provided";
        }

        // Assume the first price is the minimum price
        int minPrice = stockPrices[0];
        int minPriceDay = 0;
        int maxProfit = Integer.MIN_VALUE;
        int buyDay = 0;
        int sellDay = 0;

        // Loop through the prices starting at the second price
        for (int i = 1; i < stockPrices.length; i++) {
            int currentPrice = stockPrices[i];

            // If the difference between the current price and the minimum price is greater
            // than the current maximum profit, update the maximum profit and the buy and sell days
            if (currentPrice - minPrice > maxProfit) {
                maxProfit = currentPrice - minPrice;
                buyDay = minPriceDay;
                sellDay = i;
            }

            // If the current price is less than the minimum price, update the minimum price
            // and the day it occurred
            if (currentPrice < minPrice) {
                minPrice = currentPrice;
                minPriceDay = i;
            }
        }

        return "Buy on day " + buyDay + " and sell on day " + sellDay + " for a profit of $" + maxProfit;
    }

    /*
     * Method that generates an array of random integers representing the price changes of a stock.
     * The array will have the specified amount of elements.
     * 
     * @param amount The amount of elements in the array
     * @return An array of random integers
     */
    private static int[] generateRandomPriceChanges(int amount) {
        int[] priceChanges = new int[amount];
        for (int i = 0; i < amount; i++) {
            priceChanges[i] = random.nextInt(19) - 9;  // Random numbers between -9 and 9 inclusive.
        }
        return priceChanges;
    }

    /*
     * Method that generates an array of stock prices based on the price changes.
     * 
     * @param priceChanges An array of price changes
     * @return An array of stock prices
     */
    private static int[] generateStockPrices(int[] priceChanges) {
        int[] stockPrices = new int[priceChanges.length];
        int cumulativePrice = 0;
        for (int i = 0; i < priceChanges.length; i++) {
            cumulativePrice += priceChanges[i];
            stockPrices[i] = cumulativePrice;
        }
        return stockPrices;
    }

    /*
     * Main method that tests the performance of the maxProfit method.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        testPerformance(100000);
        testPerformance(1000000);
        testPerformance(10000000);
        testPerformance(100000000);

    }

    /*
     * Method that tests the performance of the maxProfit method.
     * Prints the amount of days and the time it took to run the method.
     * 
     * @param days The amount of days to test
     * @return An array of stock prices
     */
    private static void testPerformance(int days) {
        int[] priceChanges = generateRandomPriceChanges(days);
        int[] stockPrices = generateStockPrices(priceChanges);

        System.out.println(days + " days:");
        System.out.println(maxProfit(stockPrices));

        long startTime = System.nanoTime();
        maxProfit(stockPrices);
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000.0;  // Convert to milliseconds

        System.out.printf("Time: %.2fms%n", duration);  // Print with 2 decimal places
    }
}

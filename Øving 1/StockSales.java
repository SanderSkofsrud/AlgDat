import java.util.Random;

public class StockSales {

    private static Random random = new Random();

    private static String maxProfit(int[] stockPrices) {
        if (stockPrices.length == 0) {
            return "No prices provided";
        }

        int minPrice = stockPrices[0];
        int minPriceDay = 0;
        int maxProfit = Integer.MIN_VALUE;
        int buyDay = 0;
        int sellDay = 0;

        for (int i = 1; i < stockPrices.length; i++) {
            int currentPrice = stockPrices[i];

            if (currentPrice - minPrice > maxProfit) {
                maxProfit = currentPrice - minPrice;
                buyDay = minPriceDay;
                sellDay = i;
            }

            if (currentPrice < minPrice) {
                minPrice = currentPrice;
                minPriceDay = i;
            }
        }

        return "Buy on day " + buyDay + " and sell on day " + sellDay + " for a profit of $" + maxProfit;
    }

    private static int[] generateRandomPriceChanges(int amount) {
        int[] priceChanges = new int[amount];
        for (int i = 0; i < amount; i++) {
            priceChanges[i] = random.nextInt(19) - 9;  // Random numbers between -9 and 9 inclusive.
        }
        return priceChanges;
    }

    private static int[] generateStockPrices(int[] priceChanges) {
        int[] stockPrices = new int[priceChanges.length];
        int cumulativePrice = 0;
        for (int i = 0; i < priceChanges.length; i++) {
            cumulativePrice += priceChanges[i];
            stockPrices[i] = cumulativePrice;
        }
        return stockPrices;
    }

    public static void main(String[] args) {
        testPerformance(100000);
        testPerformance(1000000);
        testPerformance(10000000);
    }

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

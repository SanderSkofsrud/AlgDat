import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StockSales {
  /*
   * Map of day and price change
   */
  static Map<Integer, Integer> priceChange = new HashMap<>();
  /*
   * Random number generator
   */
  static Random random = new Random();

  /*
   * Finds the maximum profit of a stock sale
   * @param priceChange Map of day and price change
   * @return String with buy day, sell day and profit
   */
  private static String maxProfit(Map<Integer, Integer> priceChange) {
    if (priceChange.size() == 0) {
      return "No prices provided";
    }

    int minPrice = priceChange.get(0);
    int minPriceDay = 0;
    int maxProfit = Integer.MIN_VALUE;
    int buyDay = 0;
    int sellDay = 0;

    for (int i = 1; i < priceChange.size(); i++) {
      int currentPrice = priceChange.get(i);

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

  /*
   * Main method
   * Creates a map of 100 days with random price changes
   * Prints the maximum profit
   * Prints the time it takes to run the method
   * @param args Command line arguments
   * @return void
   */
  public static void main(String[] args) {
    for (int i = 0; i < 100; i++) {
      int randomNumber = random.nextInt(19) - 9;
      priceChange.put(i, randomNumber);
    }

    System.out.println(maxProfit(priceChange));

    long startTime = System.nanoTime();
    maxProfit(priceChange);
    long endTime = System.nanoTime();
    long duration = (endTime - startTime)/1000;
    System.out.println("Time: " + duration + "ms");

    long startTime2 = System.nanoTime();
    maxProfit(priceChange);
    long endTime2 = System.nanoTime();
    long duration2 = (endTime2 - startTime2)/1000;
    System.out.println("Time: " + duration2 + "ms");
  }
}

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StockSales {
  static Map<Integer, Integer> priceChange = new HashMap<>();
  static Random random = new Random();

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

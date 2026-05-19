class Solution {
    public int maxProfit(int[] prices) {
        if(prices.length == 0)
            return 0;
        int min = prices[0];
        int profit = 0;

        for(int price: prices) {
            min = Math.min(min, price);
            int currentProfit = price- min;
            profit = Math.max(profit, currentProfit);
        }
        return profit;
        
    }

    ye prefic summ arry se bhi kr sakte hai

    
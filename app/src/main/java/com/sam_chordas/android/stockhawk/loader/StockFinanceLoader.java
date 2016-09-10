package com.sam_chordas.android.stockhawk.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.sam_chordas.android.stockhawk.pojo.HistoryStock;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

/**
 * Created by DELL I7 on 9/9/2016.
 */
public class StockFinanceLoader extends AsyncTaskLoader<ArrayList<HistoryStock>> {

    String symbol = "";
    List<HistoricalQuote> historicalQuotes;
    ArrayList<HistoryStock> historyStockArrayList;

    public StockFinanceLoader(Context context, String symbol) {
        super(context);
        this.symbol = symbol;

    }

    @Override
    public ArrayList<HistoryStock> loadInBackground() {
        historyStockArrayList = new ArrayList<>();

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -1); // from 1 year
        Stock stock = null;
        try {
            stock = YahooFinance.get(symbol, from, to, Interval.MONTHLY);
            historicalQuotes = stock.getHistory();
            int quotesSize = historicalQuotes.size();
            for (int i = 0; i < quotesSize; i++) {
                HistoricalQuote quote = historicalQuotes.get(i);

                //get low price of stock during month
                BigDecimal stockPriceLow = quote.getLow();

                //get the month of the quote
                Calendar calendar = quote.getDate();
                String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
                historyStockArrayList.add(new HistoryStock(stockPriceLow, month));

            }

            Log.i("google", "onRunTask: " + stock.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return historyStockArrayList;
    }
}

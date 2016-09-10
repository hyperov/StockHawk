package com.sam_chordas.android.stockhawk.pojo;

import java.math.BigDecimal;

/**
 * Created by Ahmed on 9/9/2016.
 */
public class HistoryStock {

    private BigDecimal stockPriceLow;
    private String month;

    public BigDecimal getStockPriceLow() {
        return stockPriceLow;
    }

    public String getMonth() {
        return month;
    }

    public HistoryStock(BigDecimal stockPriceLow, String month) {
        super();
        this.month = month;
        this.stockPriceLow = stockPriceLow;

    }
}

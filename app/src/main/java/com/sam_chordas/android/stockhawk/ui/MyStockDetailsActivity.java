package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.loader.StockFinanceLoader;
import com.sam_chordas.android.stockhawk.pojo.HistoryStock;

import java.util.ArrayList;

public class MyStockDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<HistoryStock>> {

    LineChartView lineGraph;
    LineSet set;
    Point entry;
    Animation animation;
    String symbol;

    private static final int STOCK_HISTORY_LOADER_ID = 0;

    Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        lineGraph = (LineChartView) findViewById(R.id.linechart);

        // send stock name to loader
        symbol = getIntent().getExtras().getString("symbol");
        bundle = new Bundle();
        bundle.putString("symbol", symbol);

        //init loader


        animation = new Animation(7000);
//      animation.setEasing(new linearEase());
        animation.setEasing(new BounceEase());


        lineGraph.setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setXLabels(AxisController.LabelPosition.OUTSIDE);
//        lineGraph.show();


        getLoaderManager().initLoader(STOCK_HISTORY_LOADER_ID, bundle, this).forceLoad();


    }


//    @Nullable
//    @Override
//    public Intent getParentActivityIntent() {
//        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//    }

    @Override
    protected void onStop() {
        super.onStop();
//        lineGraph.dismiss(animation);
    }

    @Override
    public Loader<ArrayList<HistoryStock>> onCreateLoader(int id, Bundle args) {

        return new StockFinanceLoader(this, args.getString("symbol"));
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<HistoryStock>> loader, ArrayList<HistoryStock> data) {


        if (data.size() == 0) {
            Log.e("arraylist", "onLoadFinished: " + "size is 0");
            return;
        }

        set = new LineSet();
        Float biggest = 0f;
        Float smallest = data.get(0).getStockPriceLow().floatValue();
        int step = 3;
        for (int k = data.size(); k > 0; k--) {
            HistoryStock stock = data.get(k - 1);
            String month = stock.getMonth();
            Float priceLow = stock.getStockPriceLow().floatValue();

            //get highest value of points to draw chart
            if (priceLow >= biggest)
                biggest = priceLow;

            //get lowest value of points to draw chart
            if (priceLow <= smallest)
                smallest = priceLow;


            set.addPoint(month, priceLow);
        }
        int big = biggest.intValue();
        int small = smallest.intValue();

        //get proper step value
        do {
            step++;
        } while ((((big + 20) - (small - 20)) % step) != 0);

        //graph x,y Axis begin and end of y axis, and space between values
        lineGraph.setAxisBorderValues(small - 20, big + 20, step);

        //dots
//        set.setDotsStrokeColor(getResources().getColor(R.color.material_red_700));

        //set

        set.setSmooth(false);
        set.setColor(getResources().getColor(R.color.md_material_blue_800));
        set.setDotsStrokeThickness(2);

        lineGraph.addData(set);
        lineGraph.show(animation);

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<HistoryStock>> loader) {
//        lineGraph.dismiss();
    }
}

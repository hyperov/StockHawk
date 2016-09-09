package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.sam_chordas.android.stockhawk.R;

public class MyStockDetailsActivity extends AppCompatActivity {

    LineChartView lineGraph;
    LineSet set;
    Point entry;
    Animation animation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        lineGraph = (LineChartView) findViewById(R.id.linechart);

        animation = new Animation(7000);
//        animation.setEasing(new linearEase());
        animation.setEasing(new BounceEase());
        set = new LineSet();

        set.addPoint("ff", 5);
        set.addPoint("fd", 6);
        set.addPoint("fgf", 8);
        set.addPoint("fdfgf", 10);
        set.addPoint("ffkj", 20);
        set.addPoint("ffs", 30);
        set.addPoint("ffg", 2);
        set.addPoint("ffza", 5);
        set.addPoint("ffqq", 7);
        set.setDotsStrokeThickness(20);
//        set.setDotsColor(getResources().getColor(R.color.));
        set.setColor(getResources().getColor(R.color.md_material_blue_800));
        set.setDotsStrokeColor(getResources().getColor(R.color.material_red_700));
//        set.setFill(getResources().getColor(R.color.material_green_700));
        set.setSmooth(true);
//        set.setDashed();

        lineGraph.addData(set);
        lineGraph.setStep(3);
        lineGraph.setAxisBorderValues(0, 50, 2);
        lineGraph.setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setXLabels(AxisController.LabelPosition.OUTSIDE);


//        basic api calling to get stock history and show it
//        in lineChartView
        /*

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -5); // from 5 years ago
        Stock google = null;
        try {
            google = YahooFinance.get("GOOG", from, to, Interval.MONTHLY);
            Calendar c = google.getHistory().get(0).getDate();
            String month = String.valueOf(c.get(Calendar.MONTH) + 1);
            Date d = c.getTime();
            Log.i("google", "onRunTask: " + google.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
         */
        lineGraph.show(animation);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}

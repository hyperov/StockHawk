package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;

public class MyStockDetailsActivity extends AppCompatActivity {

    LineChartView lineGraph;
    LineSet set;
    Point entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        lineGraph= (LineChartView) findViewById(R.id.linechart);
        set=new LineSet();

        set.addPoint("ff",5);
        set.addPoint("fd",6);
        set.addPoint("fgf",8);
        set.addPoint("fdfgf",1);
        set.addPoint("ffkj",54);
        set.addPoint("ffs",89);
        set.addPoint("ffg",2);
        set.addPoint("ffza",0);
        set.addPoint("ffqq",-5);
        set.beginAt(0);
        lineGraph.addData(set);

        lineGraph.show();


    }
}

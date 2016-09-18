package com.sam_chordas.android.stockhawk.service;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by Ahmed Nabil on 9/17/2016.
 */
public class WidgetService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, null, null, null, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.list_item_quote);

                String symbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
                String bidPrice = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
                String change = data.getString(data.getColumnIndex(QuoteColumns.CHANGE));


                views.setTextViewText(R.id.stock_symbol, symbol);
                views.setTextViewText(R.id.bid_price, bidPrice);
                views.setTextViewText(R.id.change, change);

                views.setContentDescription(R.id.stock_symbol, getString(R.string.stock_name) + symbol);
                views.setContentDescription(R.id.bid_price, getString(R.string.bid_price) + bidPrice);
                views.setContentDescription(R.id.change, getString(R.string.change_stock) + change);


                final Intent fillInIntent = new Intent();

                fillInIntent.putExtra("symbol", symbol);
                views.setOnClickFillInIntent(R.id.item, fillInIntent);
                return views;
            }


            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(data.getColumnIndex(QuoteColumns._ID));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

package com.sam_chordas.android.stockhawk.service;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {
    private String LOG_TAG = StockTaskService.class.getSimpleName();
    public static final String ACTION_DATA_UPDATED="com.sam_chorads.android.stockhawk.ACTION_DATA_UPDATED";

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;

    public StockTaskService() {
    }

    public StockTaskService(Context context) {
        mContext = context;
    }

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    public int onRunTask(TaskParams params) {

        ResultReceiver resultReceiver=null;
        Cursor initQueryCursor;
        if (mContext == null) {
            mContext = this;
        }
        //reset server status
        Utils.setServerStatus(mContext, Utils.SERVER_UNKNOWN);

        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                    + "in (", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (params.getTag().equals("init") || params.getTag().equals("periodic")) {
            isUpdate = true;
            initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                    null, null);
            if (initQueryCursor == null || initQueryCursor.getCount() == 0) {
                // Init task. Populates DB with quotes for the symbols seen below
                try {
                    urlStringBuilder.append(
                            URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else if (initQueryCursor != null && initQueryCursor.getCount() != 0) {
                DatabaseUtils.dumpCursor(initQueryCursor);
                initQueryCursor.moveToFirst();

                for (int i = 0; i < initQueryCursor.getCount(); i++) {
                    mStoredSymbols.append("\"" +
                            initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")) + "\",");
                    initQueryCursor.moveToNext();
                }

                mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");

                try {
                    urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
            if (initQueryCursor != null)
                initQueryCursor.close();

        } else if (params.getTag().equals("add")) {
            //receiver
           resultReceiver= params.getExtras().getParcelable("receiver");

            isUpdate = false;
            // get symbol from params.getExtra and build query
            String stockInput = params.getExtras().getString("symbol");
            try {
                urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        // finalize the URL for the API query.
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        String urlString;
        String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;

//        if (urlStringBuilder != null) {
        urlString = urlStringBuilder.toString();
        try {
            getResponse = fetchData(urlString);
            result = GcmNetworkManager.RESULT_SUCCESS;
            try {
                ContentValues contentValues = new ContentValues();
                // update ISCURRENT to 0 (false) so new data is current
                if (isUpdate) {
                    contentValues.put(QuoteColumns.ISCURRENT, 0);
                    mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                            null, null);
                }

                ArrayList<ContentProviderOperation> contentProviderOperations =
                        Utils.quoteJsonToContentVals(getResponse, mContext);

                if (contentProviderOperations.size() != 0) {
                    mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                            contentProviderOperations);
                    Utils.setServerStatus(mContext, Utils.SERVER_OK);
                    //broad cast for widget
                    Intent updateData=new Intent(ACTION_DATA_UPDATED)
                            .setPackage(mContext.getPackageName());
                    mContext.sendBroadcast(updateData);
                    //receiver :tag=add
                    if(resultReceiver!=null){
                        Bundle bundle=new Bundle();
                        bundle.putString("status","ok");
                        resultReceiver.send(0,bundle);
                    }
                } else {
                    Utils.setServerStatus(mContext, Utils.SERVER_INVALID);
                    // receiver:tag=add
                    if(resultReceiver!=null){
                        Bundle bundle=new Bundle();
                        bundle.putString("status","invalid");
                        resultReceiver.send(0,bundle);
                    }
                }
            } catch (RemoteException | OperationApplicationException e) {
                Log.e(LOG_TAG, "Error applying batch insert", e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "onRunTask: " + e.getMessage(), e);
            Utils.setServerStatus(mContext, Utils.SERVER_DOWN);
        }

        return result;
    }

}

package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

    private  int result;
    private boolean isAdd = false;

    public StockIntentService() {
        super(StockIntentService.class.getName());
    }

    public StockIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        isAdd = false;
        Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
        StockTaskService stockTaskService = new StockTaskService(this);
        Bundle args = new Bundle();
        if (intent.getStringExtra("tag").equals("add")) {
            args.putString("symbol", intent.getStringExtra("symbol"));
            isAdd = true;
        }
        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        result = stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
        if (result == GcmNetworkManager.RESULT_SUCCESS) {
            Utils.setServerStatus(StockIntentService.this, Utils.SERVER_OK);
        }
//        if (isAdd) {
//            if (result == GcmNetworkManager.RESULT_FAILURE) {
//                Utils.setServerStatus(StockIntentService.this, Utils.SERVER_DOWN);
//            } else if (result == GcmNetworkManager.RESULT_SUCCESS) {
//            }
//        }
    }
}


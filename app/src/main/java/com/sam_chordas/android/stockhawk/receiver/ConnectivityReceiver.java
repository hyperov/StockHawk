package com.sam_chordas.android.stockhawk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

/**
 * Created by Ahmed Nabil on 9/15/2016.
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //maybe get savedinstance state from intent
        if (Utils.isConnected(context)) {

            Utils.setServerStatus(context, Utils.SERVER_UNKNOWN);

            Intent mServiceIntent = new Intent(context, StockIntentService.class);
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");
            context.startService(mServiceIntent);

        //////////////////////////////////////////////////////////////////////////////////
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
//            PeriodicTask periodicTask = new PeriodicTask.Builder()
//                    .setService(StockTaskService.class)
//                    .setPeriod(period)
//                    .setFlex(flex)
//                    .setTag(periodicTag)
//                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
//                    .setRequiresCharging(false)
//                    .build();
//            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
//            // are updated.
//            GcmNetworkManager.getInstance(context).schedule(periodicTask);
        }else{
            Toast.makeText(context,context.getString(R.string.network_toast),Toast.LENGTH_LONG).show();
        }
    }
}

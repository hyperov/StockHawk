package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.receiver.ConnectivityReceiver;
import com.sam_chordas.android.stockhawk.receiver.MyResultReceiver;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, MyResultReceiver.Receiver {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;

    public MyResultReceiver mReceiver;
    public ConnectivityReceiver connectivityReceiver;

    RecyclerView recyclerView;
    FloatingActionButton fab;
    TextView networkError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.activity_my_stocks);

        //receiver to receive data from intent to activity
        mReceiver = new MyResultReceiver(new Handler());

        //receiver to check connectivity change
        connectivityReceiver = new ConnectivityReceiver();
//        registerReceiver(connectivityReceiver,
//                new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        fab = (FloatingActionButton) findViewById(R.id.fab);

        //empty view for network error
        networkError = (TextView) findViewById(R.id.network_error);

        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(this, StockIntentService.class);
        if (savedInstanceState == null) {

            Utils.setServerStatus(this, Utils.SERVER_UNKNOWN);

            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");

            if (Utils.isConnected(this))
                startService(mServiceIntent);

//            else {
//                updateEmptyView(networkError);
//                networkToast();
//                //set text for no network
//            }
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(this, null);
//        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
//                new RecyclerViewItemClickListener.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View v, int position) {
//                        //TODO:
//                        // do something on item click
//                    }
//                }));
        recyclerView.setAdapter(mCursorAdapter);


        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(recyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isConnected(MyStocksActivity.this)) {
                    new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                            .content(R.string.content_test)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    if (!String.valueOf(input).isEmpty() && !String.valueOf(input).contains(" ")) {
                                        // On FAB click, receive user input. Make sure the stock doesn't already exist
                                        // in the DB and proceed accordingly
                                        Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                                new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                                new String[]{input.toString().toUpperCase()}, null);

                                        if (c != null) {
                                            if (c.getCount() != 0) {
                                                Toast toast =
                                                        Toast.makeText(MyStocksActivity.this, getString(R.string.stock_inserted_found),
                                                                Toast.LENGTH_LONG);
                                                toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                                toast.show();

                                                c.close();

                                            } else {

                                                // Add the stock to DB
                                                mServiceIntent.putExtra("tag", "add");
                                                mServiceIntent.putExtra("symbol", input.toString().toUpperCase());
                                                //send receiver to intent
                                                mServiceIntent.putExtra("receiver", mReceiver);
                                                startService(mServiceIntent);

                                                //close cursor
                                                c.close();

                                            }
                                        }

                                    } else {
                                        Toast.makeText(MyStocksActivity.this,
                                                R.string.stock_name_added_is_null_or_contains_spaces, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .backgroundColor(getResources().getColor(R.color.material_green_700)).show();
                } else {
                    networkToast();
                }

            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        mTitle = getTitle();
        if (Utils.isConnected(this)) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
    }


    /**
     * Updates the empty list view with contextually relevant information that the user can
     * use to determine why they aren't seeing weather.
     */
    private void updateEmptyView(TextView tv) {

        // if cursor is empty, why? do we have an invalid location
        int message = R.string.empty_stock_list;
        @Utils.ServerStatus int serverStatus = Utils.getServerStatus(this);
        switch (serverStatus) {
            case Utils.SERVER_DOWN:
                message = R.string.empty_stock_list_server_down;
                break;
            case Utils.SERVER_INVALID:
                message = R.string.empty_stock_list_server_invalid;
                break;

            default:
                if (!Utils.isConnected(this)) {
                    message = R.string.empty_stock_list_no_network;
                }
        }
        tv.setText(message);
    }


    @Override
    public void onResume() {
        super.onResume();
//        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mReceiver.setReceiver(this);
        registerReceiver(connectivityReceiver,
                new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mReceiver.setReceiver(null);
        unregisterReceiver(connectivityReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void networkToast() {
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_LONG).show();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_change_units) {
            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        mCursor = data;

        if (data.getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
            networkError.setVisibility(View.VISIBLE);
            updateEmptyView(networkError);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
            networkError.setVisibility(View.GONE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        String status;
        if (resultCode == 0 && resultData != null) {
            Toast toast;
            status = resultData.getString("status");
            if (status != null && status.equals("ok")) { //valid stock
                toast =
                        Toast.makeText(this,
                                getString(R.string.stock_inserted_added),
                                Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                toast.show();

            } else if (status != null &&status.equals("invalid")) { //invalid stock
                toast =
                        Toast.makeText(this,
                                getString(R.string.stock_inserted_not_found),
                                Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                toast.show();
            }


        }
    }
}

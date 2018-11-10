package com.xendev.xeon.cryptowatch;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.xendev.xeon.cryptowatch.Classes.Coin;
import com.xendev.xeon.cryptowatch.Data.CoinAdapter;
import com.xendev.xeon.cryptowatch.Data.CoinContract;
import com.xendev.xeon.cryptowatch.Data.CoinDbHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.xendev.xeon.cryptowatch.DisplayRatesActivity.API_URL;
import static com.xendev.xeon.cryptowatch.DisplayRatesActivity.loadingDialog;
import static com.xendev.xeon.cryptowatch.LandingActivity.textToast;

public class CoinListActivity extends Fragment {

    View coinListView;
    private CoinAdapter mAdapter;
    private SQLiteDatabase mDb;
    private EditText mNewCoinText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        coinListView = inflater.inflate(R.layout.activity_edit_watch,container,false);

        RecyclerView waitlistRecyclerView;

        // Set Add button
        Button btnAddCoin = (Button) coinListView.findViewById(R.id.add_to_coinlist_button);

        btnAddCoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToWatchlist(view);
            }
        });

        // Set local attributes to corresponding views
        waitlistRecyclerView = (RecyclerView) coinListView.findViewById(R.id.all_coin_list_view);
        mNewCoinText = (EditText) coinListView.findViewById(R.id.coin_name_edit_text);

        // Set layout for the RecyclerView, because it's a list we are using the linear layout
        waitlistRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        // Create a DB helper (this will create the DB if run for the first time)
        CoinDbHelper dbHelper = new CoinDbHelper(getActivity());

        // Keep a reference to the mDb until paused or killed. Get a writable database
        // because you will be adding restaurant customers
        mDb = dbHelper.getWritableDatabase();

        // Get all guest info from the database and save in a cursor
        Cursor cursor = getAllCoins(null);

        // Create an adapter for that cursor to display the data
        mAdapter = new CoinAdapter(getActivity(), cursor);

        // Link the adapter to the RecyclerView
        waitlistRecyclerView.setAdapter(mAdapter);

        // Create an item touch helper to handle swiping items off the list
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            // COMPLETED (4) Override onMove and simply return false inside
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //do nothing, we only care about swiping
                return false;
            }

            // COMPLETED (5) Override onSwiped
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //get the id of the item being swiped
                long id = (long) viewHolder.itemView.getTag();
                //remove from DB
                removeCoin(id);
                // Inform User
                textToast(getActivity(),"Removed");
                //update the list
                mAdapter.swapCursor(getAllCoins(null));
            }

            //COMPLETED (11) attach the ItemTouchHelper to the waitlistRecyclerView
        }).attachToRecyclerView(waitlistRecyclerView);

        return coinListView;
    }

    /**
     * This method is called when user clicks on the Add to waitlist button
     *
     * @param view The calling view (button)
     */
    public void addToWatchlist(View view) {
        if (mNewCoinText.getText().length() == 0) {
            return;
        }

        loadingDialog = new ProgressDialog(getActivity());
        loadingDialog.setMessage("Adding...");
        loadingDialog.show();

        final String coinName = mNewCoinText.getText().toString().toUpperCase();

        final RequestQueue queue = Volley.newRequestQueue(getActivity());

        // Request a Json response from the provided URL.
        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.GET, API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject objCoin = response.getJSONObject("Data");
                            if (objCoin.has(coinName)) {
                                JSONObject objData = objCoin.getJSONObject(coinName);
                                Integer coinID = objData.getInt("Id");

                                // Add guest info to mDb
                                addNewWatch(mNewCoinText.getText().toString().toUpperCase());

                                // Update the cursor in the adapter to trigger UI to display the new list
                                mAdapter.swapCursor(getAllCoins(null));

                                //clear UI text fields
                                mNewCoinText.clearFocus();
                                mNewCoinText.getText().clear();
                                loadingDialog.dismiss();


                            } else {
                                loadingDialog.dismiss();
                                textToast(getActivity(),"Failed! Invalid Name.");
                            }

                        } catch (JSONException e) {
                            Log.e("CryptoWatch", "Unexpected JSON Exception", e);
                            loadingDialog.dismiss();
                            textToast(getActivity(),"Unexpected JSON Exception " + e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CryptoWatch", "unexpected JSON exception", error);
                loadingDialog.dismiss();
                textToast(getActivity(),error.toString());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(JsonRequest);
    }



    /**
     * Query the mDb and get all guests from the waitlist table
     *
     * @return Cursor containing the list of guests
     */
    public Cursor getAllCoins(ArrayList<Coin> coins) {
        Cursor cursor =
                mDb.query(
                CoinContract.CoinEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                CoinContract.CoinEntry.COLUMN_COIN_TIMESTAMP);
        return cursor;
    }

    /**
     * Adds a new guest to the mDb including the party count and the current timestamp
     *
     * @param name  Coin Name
     * @return id of new record added
     */
    private long addNewWatch(String name) {
        ContentValues cv = new ContentValues();
        cv.put(CoinContract.CoinEntry.COLUMN_COIN_NAME, name);
        return mDb.insert(CoinContract.CoinEntry.TABLE_NAME, null, cv);
    }


    // COMPLETED (1) Create a new function called removeGuest that takes long id as input and returns a boolean
    /**
     * Removes the record with the specified id
     *
     * @param id the DB id to be removed
     * @return True: if removed successfully, False: if failed
     */
    private boolean removeCoin(long id) {
        // COMPLETED (2) Inside, call mDb.delete to pass in the TABLE_NAME and the condition that WaitlistEntry._ID equals id
        return mDb.delete(CoinContract.CoinEntry.TABLE_NAME, CoinContract.CoinEntry._ID + "=" + id, null) > 0;
    }
}

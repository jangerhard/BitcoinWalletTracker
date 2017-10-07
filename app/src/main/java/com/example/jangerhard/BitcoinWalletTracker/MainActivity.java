package com.example.jangerhard.BitcoinWalletTracker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.widget.PullRefreshLayout;
import com.example.jangerhard.BitcoinWalletTracker.qrStuff.barcode.BarcodeCaptureActivity;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int BARCODE_READER_REQUEST_CODE = 1337;
    private static final String LOG_TAG = "MainActivity";
    private RequestQueue mRequestQueue;
    String url = "https://blockchain.info/";
    Activity mActivity;
    AccountAdapter adapter;
    TextView tvTotalBalance;
    BitcoinUtils utils;
    PullRefreshLayout allAccountsView;

    private int numRefreshed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        tvTotalBalance = (TextView) findViewById(R.id.totalBalance);

        // listen refresh event
        allAccountsView = (PullRefreshLayout) findViewById(R.id.allAccountsView);
        allAccountsView.setRefreshStyle(PullRefreshLayout.STYLE_SMARTISAN);
        allAccountsView.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                refreshData();
            }
        });

        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);

        utils = new BitcoinUtils(sharedPref, getString(R.string.bitcoinaddresses));

        adapter = new AccountAdapter(this, utils);
        adapter.notifyDataSetChanged();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        Button bAddAccount = (Button) findViewById(R.id.bAddAccount);
        bAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
            }
        });


        Button bGetAccount = (Button) findViewById(R.id.bGetAccount);
        bGetAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData();
            }
        });

        refreshData();
    }

    private void updateUI() {

        allAccountsView.setRefreshing(false);
        tvTotalBalance.setText(String.format("Total balance: %s", utils.totalBalance()));
    }

    private void refreshData() {

        if (utils.getAddresses().isEmpty()) {
            allAccountsView.setRefreshing(false);
            Toast.makeText(getBaseContext(), "No accounts yet!", Toast.LENGTH_SHORT).show();
            return;
        }
        numRefreshed = 0;
        getWalletInfo(utils.getAddresses());
    }

    private void getWalletInfo(List<String> addresses) {

//        adapter.setPending();

        for (String address : addresses) {

            // Request a string response from the provided URL.
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET,
                            url + "rawaddr/" + address,
                            null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            handleRefreshedAccount(new Gson().fromJson(response.toString(), BitcoinAccount.class));
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("MainActivity", error.toString());
                            Toast.makeText(getBaseContext(),
                                    "That didn't work!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            // Add the request to the RequestQueue.
            getVolleyRequestQueue().add(jsObjRequest);
        }

        adapter.notifyDataSetChanged();

    }

    private void handleRefreshedAccount(BitcoinAccount acc) {

        numRefreshed++;
        utils.updateAccount(acc);
        adapter.notifyDataSetChanged();

        if (numRefreshed == utils.getNumberOfAccounts())
            updateUI();

    }

    private void addBarcode(Barcode barcode) {

        Log.i(LOG_TAG, "Address: " + barcode.displayValue);

        if (utils.hasAddress(barcode.displayValue)) {
            Toast.makeText(getBaseContext(), "That address is already added!", Toast.LENGTH_SHORT).show();
        } else if (BitcoinUtils.verifyAddress(barcode.displayValue)) {
            showBitcoinAddressDialog(barcode.displayValue);
        } else
            Toast.makeText(getBaseContext(), "That is not a bitcoin address!", Toast.LENGTH_SHORT).show();
    }

    private void showBitcoinAddressDialog(final String address) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("New address scanned!");
        builder.setMessage("Is this the correct address? \n\n" + address);

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        utils.addAddress(address);
                        refreshData();
                    }
                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // negative button logic
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    addBarcode((Barcode) data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject));
                } else
                    Toast.makeText(getBaseContext(), R.string.no_barcode_captured, Toast.LENGTH_SHORT).show();
            } else Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format),
                    CommonStatusCodes.getStatusCodeString(resultCode)));
        } else super.onActivityResult(requestCode, resultCode, data);
    }

    public RequestQueue getVolleyRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        }

        return mRequestQueue;
    }
}

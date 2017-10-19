package io.github.jangerhard.BitcoinWalletTracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.widget.PullRefreshLayout;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.github.jangerhard.BitcoinWalletTracker.qrStuff.barcode.BarcodeCaptureActivity;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class MainActivity extends AppCompatActivity {

    private static final int BARCODE_READER_REQUEST_CODE = 1337;
    private static final String LOG_TAG = "MainActivity";
    private RequestQueue mRequestQueue;
    String url_blockchain = "https://blockchain.info/";
    String url_exchange = "https://min-api.cryptocompare.com/data/";
    Activity mActivity;
    AccountAdapter adapter;
    TextView tvTotalBalance;
    BitcoinUtils utils;
    PullRefreshLayout allAccountsView;
    RecyclerView recyclerView;

    private int numRefreshed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        recyclerView = findViewById(R.id.recycler_view);

        tvTotalBalance = findViewById(R.id.totalBalance);

        // listen refresh event
        allAccountsView = findViewById(R.id.allAccountsView);
        allAccountsView.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                refreshData();
            }
        });

        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);

        utils = new BitcoinUtils(sharedPref, getString(R.string.bitcoinaddresses));
        utils.setup();

        adapter = new AccountAdapter(this, utils);
        adapter.notifyDataSetChanged();

        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setAdapter(adapter);

        Button bAddAccount = findViewById(R.id.bAddAccount);
        bAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
            }
        });


        Button bGetAccount = findViewById(R.id.bGetAccount);
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
        getCurrentPrice(utils.getCurrencyPair());
    }

    private void getSingleWalletInfo(String address, final boolean firstTime) {

        // Request a response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET,
                        url_blockchain + "rawaddr/" + address + "?limit=5",
                        null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        BitcoinAccount newAcc = new Gson().fromJson(response.toString(), BitcoinAccount.class);
                        if (firstTime)
                            handleAddedAccount(newAcc);
                        else
                            handleRefreshedAccount(newAcc);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        String message = "Something went wrong!";

                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            message = "No internet connection.";
                        } else if (error instanceof ServerError) {
                            message = "Error on the server.";
                        } else if (error instanceof NetworkError) {
                            message = "There's a problem with the network.";
                        } else if (error instanceof ParseError) {
                            message = "There was an error handling the data.";
                        }

                        Log.e(LOG_TAG, message);
                        Toast.makeText(getBaseContext(),
                                message,
                                Toast.LENGTH_SHORT).show();
                        allAccountsView.setRefreshing(false);
                    }
                });

        // Add the request to the RequestQueue.
        getVolleyRequestQueue().add(jsObjRequest);

    }

    private void getAllWalletsInfo(List<String> addresses) {

        for (String address : addresses) {
            getSingleWalletInfo(address, false);
        }
    }

    private void getCurrentPrice(String currencyPair) {

        // Request a response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET,
                        url_exchange + "price?fsym=BTC&tsyms=" + currencyPair,
                        null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        handleRefreshedCurrency(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        String message = "Something went wrong!";

                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            message = "No internet connection.";
                        } else if (error instanceof ServerError) {
                            message = "Error reaching server while getting exchange-rate.";
                        } else if (error instanceof NetworkError) {
                            message = "There's a problem with the network.";
                        } else if (error instanceof ParseError) {
                            message = "There was an error handling the data.";
                        }

                        Log.e(LOG_TAG, message);
                        Toast.makeText(getBaseContext(),
                                message,
                                Toast.LENGTH_SHORT).show();
                        allAccountsView.setRefreshing(false);
                    }
                });

        getVolleyRequestQueue().add(jsObjRequest);
    }

    private void handleRefreshedCurrency(JSONObject response) {

        Double price = 0.0;

        try {
            price = response.getDouble("NOK");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing price information");
        }

        Log.i(LOG_TAG, "Got price: " + price);
        //Toast.makeText(mActivity, "Got price: " + price, Toast.LENGTH_SHORT).show();
        getAllWalletsInfo(utils.getAddresses());
        utils.updateCurrency(price);

    }

    private void handleAddedAccount(BitcoinAccount newAcc) {
        utils.addNewAccount(newAcc);

        recyclerView.smoothScrollToPosition(adapter.getItemCount());
        adapter.notifyItemInserted(adapter.getItemCount() - 1);
    }

    private void handleRefreshedAccount(BitcoinAccount acc) {

        numRefreshed++;
        int index = utils.updateAccount(acc);
        adapter.notifyItemChanged(index);

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

        new MaterialStyledDialog.Builder(this)
                .setTitle("New address scanned!")
                .setIcon(R.drawable.bitcoinlogo)
                .withDialogAnimation(true)
                .setDescription("Is this the correct address? \n\n" + address)
                .setPositiveText(getString(android.R.string.yes))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        utils.addAddress(address);
                        getSingleWalletInfo(address, true);
                    }
                })
                .setNegativeText(getString(android.R.string.cancel))
                .show();
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

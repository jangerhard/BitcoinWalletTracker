package com.example.jangerhard.BitcoinWalletTracker;

import android.Manifest;
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
import com.example.jangerhard.BitcoinWalletTracker.qrStuff.barcode.BarcodeCaptureActivity;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONObject;

import java.math.BigInteger;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int BARCODE_READER_REQUEST_CODE = 1337;
    private static final String LOG_TAG = "MainActivity";
    private RequestQueue mRequestQueue;
    String url = "https://blockchain.info/";
    Activity mActivity;
    AccountAdapter adapter;
    int numRefreshed = 0;
    TextView tvTotalBalance;
    BitcoinUtils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        tvTotalBalance = (TextView) findViewById(R.id.totalBalance);

        utils = new BitcoinUtils();
        prepareAccounts();
        adapter = new AccountAdapter(this, utils.getAccounts());

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

        refreshData();

        Button bGetAccount = (Button) findViewById(R.id.bGetAccount);
        bGetAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData();
            }
        });

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

    private void addBarcode(Barcode barcode) {

        Log.i(LOG_TAG, "Address: " + barcode.displayValue);

        if (BitcoinUtils.verifyAddress(barcode.displayValue)) {
            showBitcoinAddressDialog(barcode.displayValue);
        } else
            Toast.makeText(getBaseContext(), "That is not a bitcoin address!", Toast.LENGTH_SHORT).show();
    }

    private void addBitcoinAddress(String address) {
        utils.addAddress(address);
        refreshData();
    }

    private void prepareAccounts() {

        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);

        if (!utils.addAddressesFromPrefs(sharedPref, getString(R.string.bitcoinaddresses))) {
            tvTotalBalance.setText("No accounts.");
        }

//        addresses.add("1FfmbHfnpaZjKFvyi1okTjJJusN455paPH");
//        addresses.add("1AJbsFZ64EpEfS5UAjAfcUG8pH8Jn3rn1F");
//        addresses.add("1A8JiWcwvpY7tAopUkSnGuEYHmzGYfZPiq");
//
//        saveData();
    }

    private void refreshData() {

        if (utils.noAddresses())
            return;

        utils.clearAccounts();

        Dexter.withActivity(mActivity)
                .withPermission(Manifest.permission.INTERNET)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        getWalletInfo(utils.getAddresses());
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(
                                getBaseContext(),
                                "You need to activate Internet permission!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    private void getWalletInfo(List<String> addresses) {

        for (String address : addresses) {

            // Create blank account
            BitcoinAccount b = new BitcoinAccount();
            b.setFinal_balance(BigInteger.valueOf(0L));
            utils.addAccount(b);

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

        acc.setNickName("TestAccount " + numRefreshed);
        utils.removeAccount(numRefreshed);
        utils.addAccount(numRefreshed, acc);
        numRefreshed++;

        if (numRefreshed >= utils.numAddresses()) {
            adapter.notifyDataSetChanged();
            numRefreshed = 0;
            tvTotalBalance.setText(String.format("Total balance: %s", utils.totalBalance()));
        }
    }

    public RequestQueue getVolleyRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        }

        return mRequestQueue;
    }

    private void saveData() {
        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.bitcoinaddresses),
                BitcoinUtils.createAddressString(utils.getAddresses()));
        editor.apply();
    }

    private void showBitcoinAddressDialog(final String address) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("New address added!");
        builder.setMessage("Is this the correct address? \n\n" + address);

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addBitcoinAddress(address);
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
}

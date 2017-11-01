package io.github.jangerhard.BitcoinWalletTracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.wajahatkarim3.easyflipview.EasyFlipView;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

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
    String url_exchange = "https://api.coinbase.com/v2/prices/";
    Activity mActivity;
    AccountAdapter adapter;
    TextView tvTotalBalance, tvTotalValue,
            tvTotalInvestment, tvTotalInvestmentSettings, tvExchangeRate;
    BitcoinUtils utils;
    PullRefreshLayout allAccountsView;
    RecyclerView recyclerView;
    EasyFlipView mFlipView;

    private int numRefreshed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
        utils = new BitcoinUtils(sharedPref, getString(R.string.bitcoinaddresses));
        utils.setup();

        // Overview
        setupOverviewUI();

        // Settings
        setupSettingsUI();

        // Accounts
        setupAccountsUI();

        handleIncomingData();
        refreshData();
    }

    private void setupOverviewUI() {
        tvTotalBalance = findViewById(R.id.tvCalculatedBalance);
        tvTotalValue = findViewById(R.id.tvInvestmentPercentage);

        ImageButton bAddAccount = findViewById(R.id.bAddAccount);
        bAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mActivity, "Launching camera", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
            }
        });

        mFlipView = findViewById(R.id.flipview_layout);

        ImageButton bOpenSettings = findViewById(R.id.bSettings);
        ImageView bCloseSettings = findViewById(R.id.bSettingsClose);
        View.OnClickListener clFlip = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFlipView.flipTheView();
            }
        };
        bOpenSettings.setOnClickListener(clFlip);
        bCloseSettings.setOnClickListener(clFlip);
    }

    private void setupSettingsUI() {
        tvTotalInvestment = findViewById(R.id.tv_total_investment);
        tvTotalInvestmentSettings = findViewById(R.id.tv_total_investment_settings);
        tvExchangeRate = findViewById(R.id.tv_exchange_rate);

        Button bChangeCurrency = findViewById(R.id.bChangeCurrency);
        bChangeCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] items = getResources().getStringArray(R.array.currencyNames);
                new LovelyChoiceDialog(mActivity)
                        .setTopColorRes(R.color.dialog_edit)
                        .setTitle("Change currency")
                        .setIcon(R.drawable.ic_language_white_48dp)
                        .setItems(items, new LovelyChoiceDialog.OnItemSelectedListener<String>() {
                            @Override
                            public void onItemSelected(int position, String item) {
                                String pair = getResources().getStringArray(R.array.currencies)[position];
                                utils.setCurrencyPair(pair);
                                refreshData();
                            }
                        })
                        .show();
            }
        });

        Button bChangeInvestment = findViewById(R.id.bAddInvestment);
        bChangeInvestment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LovelyTextInputDialog(mActivity)
                        .setTopColorRes(R.color.dialog_edit)
                        .setTitle("Change total investment")
                        .setIcon(R.drawable.ic_attach_money_white_48dp)
                        .setHint("Total amount invested")
                        .setInitialInput("" + utils.getTotalInvestment())
                        .setInputFilter("You have to enter a number!", new LovelyTextInputDialog.TextFilter() {
                            @Override
                            public boolean check(String text) {
                                return text.matches("\\d+");
                            }
                        })
                        .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                            @Override
                            public void onTextInputConfirmed(String text) {
                                utils.saveInvestment(Long.parseLong(text));
                                updateUI();
                            }
                        })
                        .show();
            }
        });
    }

    private void setupAccountsUI() {

        recyclerView = findViewById(R.id.recycler_view);

        // listen refresh event
        allAccountsView = findViewById(R.id.allAccountsView);
        allAccountsView.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                refreshData();
            }
        });

        adapter = new AccountAdapter(this, utils);
//        adapter.notifyDataSetChanged();

        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setAdapter(adapter);
    }

    private void updateUI() {

        allAccountsView.setRefreshing(false);
        tvExchangeRate.setText(utils.getExchangeRate());
        tvTotalBalance.setText(utils.getTotalBalance());
        tvTotalValue.setText(utils.getTotalValue());
        tvTotalInvestment.setText(utils.getTotalInvestmentPercentage());
        tvTotalInvestmentSettings.setText(utils.getTotalInvestmentFormated());
    }

    private void handleIncomingData() {
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    addBarcode(sharedText);
                }
            }
        }
    }

    private void refreshData() {

        if (utils.getAddresses().isEmpty()) {
            updateUI();
            Toast.makeText(getBaseContext(), R.string.no_accounts_initial, Toast.LENGTH_SHORT).show();
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

                        String message = getString(R.string.error_generic);

                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            message = getString(R.string.error_no_internet);
                        } else if (error instanceof ServerError) {
                            message = getString(R.string.error_server);
                        } else if (error instanceof NetworkError) {
                            message = getString(R.string.Error_network);
                        } else if (error instanceof ParseError) {
                            message = getString(R.string.error_parsing);
                        }

                        Log.e(LOG_TAG, message);
                        Toast.makeText(getBaseContext(),
                                message,
                                Toast.LENGTH_SHORT).show();
                        updateUI();
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
                        url_exchange + "BTC-" + currencyPair + "/spot",
                        null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        handleRefreshedCurrency(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        String message = getString(R.string.error_generic);

                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            message = getString(R.string.error_no_internet);
                        } else if (error instanceof ServerError) {
                            message = getString(R.string.error_server);
                        } else if (error instanceof NetworkError) {
                            message = getString(R.string.Error_network);
                        } else if (error instanceof ParseError) {
                            message = getString(R.string.error_parsing);
                        }

                        Log.e(LOG_TAG, message);
                        Toast.makeText(getBaseContext(),
                                message,
                                Toast.LENGTH_SHORT).show();
                        updateUI();
                    }
                });

        getVolleyRequestQueue().add(jsObjRequest);
    }

    private void handleRefreshedCurrency(JSONObject response) {

        Double price = 0.0;

        try {
            response = response.getJSONObject("data");
            price = response.getDouble("amount");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing price information");
        }

        Log.i(LOG_TAG, "Got price: " + price);
        //Toast.makeText(mActivity, "Got price: " + price, Toast.LENGTH_SHORT).show();
        utils.updateCurrency(price);
        getAllWalletsInfo(utils.getAddresses());

    }

    private void handleAddedAccount(BitcoinAccount newAcc) {
        utils.addNewAccount(newAcc);

        recyclerView.smoothScrollToPosition(adapter.getItemCount());
        adapter.notifyItemInserted(adapter.getItemCount() - 1);
        updateUI();
    }

    private void handleRefreshedAccount(BitcoinAccount acc) {

        numRefreshed++;
        int index = utils.updateAccount(acc);
        adapter.notifyItemChanged(index);

        if (numRefreshed == utils.getNumberOfAccounts())
            updateUI();

    }

    private void addBarcode(String address) {

        Log.i(LOG_TAG, "Address: " + address);

        if (utils.hasAddress(address)) {
            Toast.makeText(getBaseContext(), R.string.account_already_added, Toast.LENGTH_SHORT).show();
        } else if (BitcoinUtils.verifyAddress(address)) {
            showBitcoinAddressDialog(address);
        } else
            Toast.makeText(getBaseContext(), R.string.invalid_address, Toast.LENGTH_SHORT).show();
    }

    private void showBitcoinAddressDialog(final String address) {

        new LovelyStandardDialog(this)
                .setTopColorRes(R.color.dialog_info)
                .setTitle(R.string.new_address)
                .setIcon(R.drawable.bitcoin_128)
                .setMessage(getString(R.string.question_correct_address) + "\n\n" + address)
                .setPositiveButton(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        utils.addAddress(address);
                        getSingleWalletInfo(address, true);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode b = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    addBarcode(b.displayValue);
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

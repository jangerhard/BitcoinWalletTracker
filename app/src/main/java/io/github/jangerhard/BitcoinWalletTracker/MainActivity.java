package io.github.jangerhard.BitcoinWalletTracker;

import java.util.List;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.widget.PullRefreshLayout;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.wajahatkarim3.easyflipview.EasyFlipView;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;
import io.github.jangerhard.BitcoinWalletTracker.qrStuff.barcode.BarcodeCaptureActivity;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final int BARCODE_READER_REQUEST_CODE = 1337;
    private static final String DARK_THEME_SELECTED = "dark_theme_selected";
    private static final String REFRESHING_THEME = "refreshing_theme";
    private static final String SHOW_GAIN_PERCENTAGE = "show_gain_percentage";
    private static final String LOG_TAG = "MainActivity";

    private RequestQueue mRequestQueue;
    String url_blockchain = "https://blockchain.info/";
    String url_exchange = "https://api.coinbase.com/v2/prices/";
    Activity mActivity;
    AccountAdapter adapter;
    TextView tvTotalBalance, tvTotalValue,
            tvInvestmentGain, tvTotalInvestmentSettings, tvExchangeRate;
    BitcoinUtils utils;
    PullRefreshLayout allAccountsView;
    CardView cv_no_accounts;
    RecyclerView recyclerView;
    EasyFlipView mFlipView;
    SharedPreferences sharedPref;
    Boolean selectedDarkTheme, showGainPercentage, noAccounts;

    private int numRefreshed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        selectedDarkTheme = sharedPref.getBoolean(DARK_THEME_SELECTED, true);
        showGainPercentage = sharedPref.getBoolean(SHOW_GAIN_PERCENTAGE, true);
        setTheme(selectedDarkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);

        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_main);

        utils = new BitcoinUtils(sharedPref, getString(R.string.bitcoinaddresses));
        utils.setup();

        cv_no_accounts = findViewById(R.id.no_accounts_view);
        Button bNoAccAdd = findViewById(R.id.bNoAccountsAdd);
        bNoAccAdd.setOnClickListener(view -> showAddAccountDialog());

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
        tvTotalValue = findViewById(R.id.tvTotalInvestment);

        ImageButton bAddAccount = findViewById(R.id.bAddAccount);
        bAddAccount.setOnClickListener(v -> showAddAccountDialog());

        mFlipView = findViewById(R.id.flipview_layout);

        if (sharedPref.getBoolean(REFRESHING_THEME, false)) {
            mFlipView.flipTheView(false);
            sharedPref.edit().putBoolean(REFRESHING_THEME, false).apply();
        }

        ImageButton bOpenSettings = findViewById(R.id.bSettings);
        ImageView bCloseSettings = findViewById(R.id.bSettingsClose);
        View.OnClickListener clFlip = view -> mFlipView.flipTheView();
        bOpenSettings.setOnClickListener(clFlip);
        bCloseSettings.setOnClickListener(clFlip);
    }

    private void setupSettingsUI() {
        tvInvestmentGain = findViewById(R.id.tvInvestmentGain);
        tvTotalInvestmentSettings = findViewById(R.id.tv_total_investment_settings);
        tvExchangeRate = findViewById(R.id.tv_exchange_rate);

        ImageButton bChangeCurrency = findViewById(R.id.bChangeCurrency);
        bChangeCurrency.setOnClickListener(view -> {
            String[] items = getResources().getStringArray(R.array.currencyNames);
            new LovelyChoiceDialog(mActivity)
                    .setTopColorRes(R.color.dialog_currencies)
                    .setTitle(R.string.change_currency_dialog_title)
                    .setIcon(R.drawable.ic_language_white_48dp)
                    .setItems(items, (position, item) -> {
                        String pair = getResources().getStringArray(R.array.currencies)[position];
                        utils.setCurrencyPair(pair);
                        refreshData();
                    })
                    .show();
        });

        ImageButton bChangeInvestment = findViewById(R.id.bAddInvestment);
        bChangeInvestment.setOnClickListener(view -> new LovelyTextInputDialog(mActivity)
                .setTopColorRes(R.color.dialog_investment)
                .setTitle(R.string.change_investment)
                .setIcon(R.drawable.ic_attach_money_white_48dp)
                .setHint(R.string.total_amount_invested)
                .setInitialInput("" + utils.getTotalInvestment())
                .setInputFilter(R.string.error_investment_input,
                        text -> text.trim().length() == 0 || text.trim().matches("\\d+"))
                .setConfirmButton(android.R.string.ok, text -> {
                    if (text.trim().length() == 0)
                        utils.saveInvestment(Long.parseLong("0"));
                    else utils.saveInvestment(Long.parseLong(text.trim()));
                    updateUI();
                })
                .show());

        CheckBox cbTheme = findViewById(R.id.checkbox_darktheme);
        cbTheme.setChecked(selectedDarkTheme);
        cbTheme.setOnCheckedChangeListener((compoundButton, b) -> {
            Toast.makeText(mActivity, R.string.toast_message_changing_theme, Toast.LENGTH_SHORT).show();

            sharedPref.edit().putBoolean(DARK_THEME_SELECTED, b).apply();
            sharedPref.edit().putBoolean(REFRESHING_THEME, true).apply();

            Intent intent = new Intent(mActivity, mActivity.getClass());
            startActivity(intent);
            finish();
        });
        CheckBox cbInvestmentGain = findViewById(R.id.checkbox_investment_gain);
        cbInvestmentGain.setChecked(showGainPercentage);
        cbInvestmentGain.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                Toast.makeText(mActivity, R.string.toast_message_show_gain_percentage, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mActivity, R.string.toast_message_show_gain, Toast.LENGTH_SHORT).show();

            sharedPref.edit().putBoolean(SHOW_GAIN_PERCENTAGE, b).apply();
            showGainPercentage = b;

            updateUI();
        });

        Button bAbout = findViewById(R.id.about_page);
        bAbout.setOnClickListener(view -> {

            Element donationElement = new Element();
            donationElement.setIconDrawable(R.drawable.ic_attach_money);
            donationElement.setTitle("Donation");
            donationElement.setOnClickListener(view1 -> {
                final String donationAddress = "1MArRnVPrMf6FR4FqtEThAa8piUbgfYDQ3";
                new LovelyStandardDialog(mActivity)
                        .setTopColorRes(R.color.dialog_qr)
                        .setIcon(utils.getBigQRThumbnail(donationAddress))
                        .setTitle(donationAddress)
                        .setNegativeButton("Copy", view11 -> {
                            ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("qrCode", donationAddress);
                            if (clipboard != null) {
                                Toast.makeText(mActivity, "Address copied to clipboard", Toast.LENGTH_SHORT).show();
                                clipboard.setPrimaryClip(clip);
                            }
                        })
                        .setPositiveButton(R.string.share, v -> {
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, donationAddress);
                            sendIntent.setType("text/plain");
                            mActivity.startActivity(sendIntent);
                        })
                        .show();
            });

            View aboutPage = new AboutPage(mActivity)
                    .isRTL(false)
                    .setImage(R.drawable.avatar_android)
                    .setDescription("Thanks for downloading the app! " +
                            "Feel free to email me about any weird bugs or any other feedback. " +
                            "I'd love to hear from you!")
                    .addEmail("jgschoepp@gmail.com", "Contact me")
                    .addWebsite("https://janschoepp.com/", "Visit my website")
                    .addGitHub("jangerhard", "Check out my github")
                    .addItem(donationElement)
                    .create();

            new LovelyCustomDialog(mActivity)
                    .setView(aboutPage)
                    .setTopColorRes(R.color.cardview_dark_background)
                    .show();
        });
    }

    private void setupAccountsUI() {

        recyclerView = findViewById(R.id.recycler_view);

        // listen refresh event
        allAccountsView = findViewById(R.id.allAccountsView);

        // start refresh
        allAccountsView.setOnRefreshListener(this::refreshData);

        adapter = new AccountAdapter(this, utils);
//        adapter.notifyDataSetChanged();

        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setAdapter(adapter);
    }

    private void updateUI() {

        if (noAccounts)
            cv_no_accounts.setVisibility(View.VISIBLE);
        else
            cv_no_accounts.setVisibility(View.GONE);

        allAccountsView.setRefreshing(false);
        tvExchangeRate.setText(utils.getExchangeRate());
        tvTotalBalance.setText(utils.getTotalBalance());
        tvTotalValue.setText(utils.getTotalValue());
        if (showGainPercentage)
            tvInvestmentGain.setText(utils.getTotalInvestmentPercentage());
        else
            tvInvestmentGain.setText(utils.getTotalInvestmentGain());
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
            Toast.makeText(getBaseContext(), R.string.no_accounts_initial, Toast.LENGTH_SHORT).show();
            noAccounts = true;
            updateUI();
            return;
        }
        noAccounts = false;
        numRefreshed = 0;
        getCurrentPrice(utils.getCurrencyPair());
    }

    private void getSingleWalletInfo(String address, final boolean firstTime) {

        // Request a response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET,
                        url_blockchain + "rawaddr/" + address + "?limit=5",
                        null, response -> {
                    BitcoinAccount newAcc = new Gson().fromJson(response.toString(), BitcoinAccount.class);
                    if (firstTime)
                        handleAddedAccount(newAcc);
                    else
                        handleRefreshedAccount(newAcc);
                }, error -> {

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
                        null,
                        this::handleRefreshedCurrency,
                        error -> {

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
        if (adapter.getItemCount() > 0)
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
        else adapter.notifyItemInserted(0);
        refreshData();
    }

    private void handleRefreshedAccount(BitcoinAccount acc) {

        numRefreshed++;
        int index = utils.updateAccount(acc);
        adapter.notifyItemChanged(index);

        if (numRefreshed == utils.getNumberOfAccounts())
            updateUI();

    }

    private void addBarcode(String address) {

        if (address.contains(":"))
            address = address.substring(address.indexOf(":") + 1);
        if (address.contains("?"))
            address = address.substring(0, address.indexOf("?"));

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
                .setPositiveButton(android.R.string.yes, v -> {
                    utils.addAddress(address);
                    getSingleWalletInfo(address, true);
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void showAddAccountDialog() {

        new LovelyTextInputDialog(mActivity)
                .setTopColorRes(R.color.dialog_info)
                .setTitle("Add address")
                .setIcon(R.drawable.bitcoin_128)
                .setHint("1FfmbHfnpaZjKFvyi1okTjJJusN455paPH")
                .setInputFilter("That is not a valid address!", text -> BitcoinUtils.verifyAddress(text))
                .setConfirmButton(android.R.string.ok, text -> {
                    utils.addAddress(text);
                    getSingleWalletInfo(text, true);
                })
                .setNegativeButton("Scan", view -> {
                    Toast.makeText(mActivity, "Launching camera", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                    startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
                })
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

    @Override
    public void onBackPressed() {

        if (mFlipView.isBackSide())
            mFlipView.flipTheView();
        else {
            new LovelyStandardDialog(this)
                    .setPositiveButton("Quit", view -> finish())
                    .setTitle("Quit the app?")
                    .setTopColor(getResources().getColor(R.color.about_instagram_color))
                    .show();
        }
    }
}

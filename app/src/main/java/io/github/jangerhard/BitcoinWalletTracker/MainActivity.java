package io.github.jangerhard.BitcoinWalletTracker;

import java.util.List;

import android.app.Activity;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.baoyz.widget.PullRefreshLayout;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.wajahatkarim3.easyflipview.EasyFlipView;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import io.github.jangerhard.BitcoinWalletTracker.adapter.AccountAdapter;
import io.github.jangerhard.BitcoinWalletTracker.client.BlockExplorer;
import io.github.jangerhard.BitcoinWalletTracker.client.PriceFetcher;
import io.github.jangerhard.BitcoinWalletTracker.qrStuff.barcode.BarcodeCaptureActivity;
import io.github.jangerhard.BitcoinWalletTracker.utilities.BitcoinAccount;
import io.github.jangerhard.BitcoinWalletTracker.utilities.BitcoinUtils;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class MainActivity extends AppCompatActivity {

    public static final int BARCODE_READER_REQUEST_CODE = 1337;
    private static final String DARK_THEME_SELECTED = "dark_theme_selected";
    private static final String REFRESHING_THEME = "refreshing_theme";
    private static final String SHOW_GAIN_PERCENTAGE = "show_gain_percentage";
    private static final String LOG_TAG = "MainActivity";

    private Activity mActivity;
    private AccountAdapter adapter;
    private TextView tvTotalBalance, tvTotalValue,
            tvInvestmentGain, tvTotalInvestmentSettings, tvExchangeRate;
    private BitcoinUtils utils;
    private PullRefreshLayout allAccountsView;
    private CardView cv_no_accounts;
    private RecyclerView recyclerView;
    private EasyFlipView mFlipView;
    private SharedPreferences sharedPref;
    private Boolean selectedDarkTheme, showGainPercentage, noAccounts;

    private PriceFetcher priceFetcher;
    private BlockExplorer blockExplorer;
    private DialogMaker dialogMaker;

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

        RequestQueue queue = Volley.newRequestQueue(this);
        priceFetcher = new PriceFetcher(queue, this, utils);
        blockExplorer = new BlockExplorer(queue, this);

        dialogMaker = new DialogMaker(this, blockExplorer, utils);

        cv_no_accounts = findViewById(R.id.no_accounts_view);
        Button bNoAccAdd = findViewById(R.id.bNoAccountsAdd);
        bNoAccAdd.setOnClickListener(view -> dialogMaker.showAddAccountDialog());

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
        bAddAccount.setOnClickListener(v -> dialogMaker.showAddAccountDialog());

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
        bChangeCurrency.setOnClickListener(view -> dialogMaker.showCurrencySelectorDialog());

        ImageButton bChangeInvestment = findViewById(R.id.bAddInvestment);
        bChangeInvestment.setOnClickListener(view -> dialogMaker.showInvestmentChangeDialog());

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
            donationElement.setOnClickListener(view1 -> dialogMaker.showAccountShareDialog(getString(R.string.donationAddress)));

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

            dialogMaker.showCustomViewDialog(aboutPage);
        });
    }

    private void setupAccountsUI() {

        recyclerView = findViewById(R.id.recycler_view);

        // listen refresh event
        allAccountsView = findViewById(R.id.allAccountsView);

        // start refresh
        allAccountsView.setOnRefreshListener(this::refreshData);

        adapter = new AccountAdapter(this, utils, dialogMaker);
//        adapter.notifyDataSetChanged();

        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setAdapter(adapter);
    }

    public void updateUI() {

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

    public void refreshData() {

        if (utils.getAddresses().isEmpty()) {
            Toast.makeText(getBaseContext(), R.string.no_accounts_initial, Toast.LENGTH_SHORT).show();
            noAccounts = true;
            updateUI();
            return;
        }
        noAccounts = false;
        numRefreshed = 0;
        priceFetcher.getCurrentPrice();
    }

    public void getAllWalletsInfo(List<String> addresses) {
        for (String address : addresses) {
            blockExplorer.getSingleWalletInfo(address, false);
        }
    }

    public void handleAddedAccount(BitcoinAccount newAcc) {
        utils.addNewAccount(newAcc);

        recyclerView.smoothScrollToPosition(adapter.getItemCount());
        if (adapter.getItemCount() > 0)
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
        else adapter.notifyItemInserted(0);
        refreshData();
    }

    public void handleRefreshedAccount(BitcoinAccount acc) {

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
            dialogMaker.showBitcoinAddressDialog(address);
        } else
            Toast.makeText(getBaseContext(), R.string.invalid_address, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {

        if (mFlipView.isBackSide())
            mFlipView.flipTheView();
        else {
            new LovelyStandardDialog(this)
                    .setPositiveButton("Quit", view -> finish())
                    .setTitle("Quit the app?")
                    .setTopColor(ContextCompat.getColor(this, R.color.about_instagram_color))
                    .show();
        }
    }
}

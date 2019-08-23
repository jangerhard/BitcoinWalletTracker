package io.github.jangerhard.BitcoinWalletTracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import io.github.jangerhard.BitcoinWalletTracker.model.BlockinfoResponse;
import io.github.jangerhard.BitcoinWalletTracker.utilities.BitcoinUtils;
import io.github.jangerhard.BitcoinWalletTracker.utilities.SharedPreferencesHelper;
import io.github.jangerhard.BitcoinWalletTracker.utilities.TrackedWallet;
import io.vavr.control.Option;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class MainActivity extends AppCompatActivity {

    public static final int BARCODE_READER_REQUEST_CODE = 1337;
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
    private Boolean selectedDarkTheme, showGainPercentage;

    private SharedPreferencesHelper preferences;

    private PriceFetcher priceFetcher;
    private BlockExplorer blockExplorer;
    private DialogMaker dialogMaker;

    private int numRefreshed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        preferences = new SharedPreferencesHelper(this.getPreferences(Context.MODE_PRIVATE));

        selectedDarkTheme = preferences.isDarkTheme();
        showGainPercentage = preferences.shouldShowGainInPercentage();
        setTheme(selectedDarkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);

        super.onCreate(savedInstanceState);

        mActivity = this;
        setContentView(R.layout.activity_main);

        utils = new BitcoinUtils(preferences);

        RequestQueue queue = Volley.newRequestQueue(this);
        priceFetcher = new PriceFetcher(queue, this);
        blockExplorer = new BlockExplorer(queue, this);

        dialogMaker = new DialogMaker(this);

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

        if (preferences.isThemeRefreshing()) {
            mFlipView.flipTheView(false);
            preferences.stopThemeRefreshing();
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
        bChangeInvestment.setOnClickListener(view -> dialogMaker.showInvestmentChangeDialog(utils.getTotalInvestment()));

        CheckBox cbTheme = findViewById(R.id.checkbox_darktheme);
        cbTheme.setChecked(selectedDarkTheme);
        cbTheme.setOnCheckedChangeListener((compoundButton, b) -> {
            Toast.makeText(mActivity, R.string.toast_message_changing_theme, Toast.LENGTH_SHORT).show();

            preferences.toggleDarkThemeSelected(b);

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

            preferences.setShowGainInPercentage(b);
            showGainPercentage = b;

            updateUI();
        });

        Button bAbout = findViewById(R.id.about_page);
        bAbout.setOnClickListener(view -> {

            TrackedWallet donationWallet = new TrackedWallet(getString(R.string.donationAddress));

            Element donationElement = new Element();
            donationElement.setIconDrawable(R.drawable.ic_attach_money);
            donationElement.setTitle("Donation");
            donationElement.setOnClickListener(view1 -> dialogMaker.showAccountShareDialog(donationWallet));

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

        if (utils.getTrackedWallets().isEmpty())
            cv_no_accounts.setVisibility(View.VISIBLE);
        else
            cv_no_accounts.setVisibility(View.GONE);

        if (numRefreshed == utils.getTrackedWallets().length())
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
        numRefreshed = 0;
        priceFetcher.getCurrentPrice(utils.getCurrencyPair());
    }

    public void handleUpdatedPrice(Option<Double> maybePrice) {
        maybePrice
                .peek(newPrice -> {
                    utils.updateCurrency(newPrice);
                    updateAllTrackedWallets();
                });

        updateUI();
    }

    public void handleOpenCamera() {
        Toast.makeText(this, "Launching camera", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
    }

    public void handleOpenShare(String address) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, address);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void handleChangeCurrency(int position) {
        String pair = getResources().getStringArray(R.array.currencies)[position];
        utils.setCurrencyPair(pair);
        priceFetcher.getCurrentPrice(pair);
    }

    public void handleUpdateInvestment(String newInvestment) {
        if (newInvestment.trim().length() == 0)
            utils.saveInvestment(Long.parseLong("0"));
        else
            utils.saveInvestment(Long.parseLong(newInvestment.trim()));
        updateUI();
    }

    public void updateAllTrackedWallets() {
        utils.getTrackedWallets().map(TrackedWallet::getAddress)
                .forEach(address -> blockExplorer.getSingleWalletInfo(address));
    }

    public void handleAddedAccount(String newAcc) {

        utils.addTrackedWallet(newAcc);

        recyclerView.smoothScrollToPosition(adapter.getItemCount());
        if (adapter.getItemCount() > 0)
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
        else adapter.notifyItemInserted(0);

        blockExplorer.getSingleWalletInfo(newAcc);
        refreshData();
    }

    public void handleRefreshedAccount(BlockinfoResponse acc) {
        utils.handleUpdatedAccount(acc)
                .peek(it -> {
                    numRefreshed++;
                    adapter.notifyItemChanged(it);
                });

        updateUI();
    }

    public void handleUpdatedNickname(String address, String newNickname, int position) {
        utils.setNewNickname(address, newNickname);
        adapter.notifyItemChanged(position);
    }

    public void handleRemoveSelectedAccount(TrackedWallet trackedWallet, int position) {
        Toast.makeText(this,
                "Removed account " + utils.getNickname(trackedWallet.getAddress()),
                Toast.LENGTH_SHORT).show();
        utils.removeTrackedAccount(trackedWallet);
        adapter.handleRemoveSelectedAccount(position);
        updateUI();
    }

    private void addBarcode(String address) {

        if (address.contains(":"))
            address = address.substring(address.indexOf(":") + 1);
        if (address.contains("?"))
            address = address.substring(0, address.indexOf("?"));

        Log.i(LOG_TAG, "Address: " + address);

        if (utils.alreadyTrackingWallet(address)) {
            Toast.makeText(getBaseContext(), R.string.account_already_added, Toast.LENGTH_SHORT).show();
        } else if (BitcoinUtils.verifyAddress(address).isDefined()) {
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

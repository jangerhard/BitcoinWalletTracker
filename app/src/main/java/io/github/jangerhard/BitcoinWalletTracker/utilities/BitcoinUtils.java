package io.github.jangerhard.BitcoinWalletTracker.utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;

import android.util.Log;
import androidx.annotation.NonNull;
import com.samourai.wallet.util.FormatsUtilGeneric;
import io.github.jangerhard.BitcoinWalletTracker.model.BlockonomicsTransactionsResponse;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.bitcoinj.params.MainNetParams;

public class BitcoinUtils {

    public enum ACCOUNT_TYPE {
        BITCOIN, SEGWIT
    }

    private static final String LOG_TAG = "BitcoinUtilities";

    private static final int BITCOIN_FACTOR = 100000000;
    private static final int MICRO_BITCOIN_FACTOR = 100000;

    private List<TrackedWallet> trackedWallets;

    private SharedPreferencesHelper preferences;
    private Double currentPrice;
    private String currencyPair;
    private long totalBalance;

    public BitcoinUtils(SharedPreferencesHelper helper) {
        preferences = helper;

        trackedWallets = getAddressesFromPrefs();

        currencyPair = helper.getCurrency().getOrElse("USD");

        totalBalance = calculateTotalBalance(trackedWallets);
    }

    public List<TrackedWallet> getTrackedWallets() {
        return trackedWallets;
    }

    public boolean alreadyTrackingWallet(String address) {
        return trackedWallets.map(TrackedWallet::getAddress).contains(address);
    }

    public void addTrackedWallet(String address) {
        if (!alreadyTrackingWallet(address)) {
            trackedWallets = trackedWallets.append(new TrackedWallet(address));
            totalBalance = calculateTotalBalance(trackedWallets);
            preferences.saveTrackedWallets(trackedWallets);
        } else
            Log.d(LOG_TAG, "Already tracking " + address);
    }

    public List<TrackedWallet> getAddressesFromPrefs() {
        return preferences.getAddresses()
                .filter(it -> verifyAddress(it).isDefined())
                .map(TrackedWallet::new);
    }

    public String getNickname(String address) {
        return preferences.getNickname(address);
    }

    public void setNewNickname(String selectedAccount, String newNickname) {
        saveNicknameToPrefs(selectedAccount, newNickname);
    }

    public Option<Integer> handleUpdatedBalance(String address, long newBalance) {

        trackedWallets = trackedWallets.map(wallet -> {
            if (wallet.getAddress().equals(address))
                wallet.setFinal_balance(newBalance);

            return wallet;
        });

        totalBalance = calculateTotalBalance(trackedWallets);
        return Option.of(trackedWallets.indexWhere(trackedWallet ->
                trackedWallet.getAddress().equals(address))
        );
    }

    public Option<Integer> handleUpdatedTransactions(String address, List<BlockonomicsTransactionsResponse.Transaction> transactions) {

        trackedWallets = trackedWallets.map(wallet -> {
            if (wallet.getAddress().equals(address))
                wallet.setTransactions(transactions);

            return wallet;
        });

        return Option.of(trackedWallets.indexWhere(trackedWallet ->
                trackedWallet.getAddress().equals(address))
        );
    }

    public void removeTrackedAccount(TrackedWallet trackedWallet) {

        deleteNicknameFromPrefs(trackedWallet.getAddress());
        trackedWallets = trackedWallets.remove(trackedWallet);
        preferences.saveTrackedWallets(trackedWallets);
        totalBalance = calculateTotalBalance(trackedWallets);
    }

    public static long calculateTotalBalance(List<TrackedWallet> accounts) {
        return accounts.map(TrackedWallet::getFinal_balance).sum().longValue();
    }

    public String getTotalBalance() {
        return formatBitcoinBalanceToString(totalBalance);
    }

    public String getTotalValue() {
        return formatCurrency(convertBTCtoCurrency(totalBalance));
    }

    public String getTotalInvestmentPercentage() {

        double investment = getTotalInvestment();
        double totalVal = convertBTCtoCurrency(totalBalance);

        if (investment == 0 || totalVal == 0) {
            return "";
        }

        double result = (totalVal - investment) / investment * 100;

        String pre = "";

        if (result > 0)
            pre = "+";

        return "(" + pre + BigDecimal.valueOf(result)
                .setScale(3, RoundingMode.HALF_UP)
                .toEngineeringString() + "%)";

    }

    public String getTotalInvestmentGain() {

        double investment = getTotalInvestment();
        double totalVal = convertBTCtoCurrency(totalBalance);

        if (investment == 0 || totalVal == 0) {
            return "";
        }

        double result = totalVal - investment;

        String pre = "";

        if (result > 0)
            pre = "+";

        return "(" + pre + BigDecimal.valueOf(result)
                .setScale(2, RoundingMode.HALF_UP)
                .toEngineeringString() + ")";

    }

    public long getTotalInvestment() {
        return preferences.getInvestment();
    }

    public String getTotalInvestmentFormated() {

        return formatCurrency(getTotalInvestment());

    }

    public String formatBTCtoCurrency(long btc) {
        return formatCurrency(convertBTCtoCurrency(btc));
    }

    String formatCurrency(double val) {

        NumberFormat format = NumberFormat.getCurrencyInstance(getCurrentLocale(getCurrencyPair()));
        format.setCurrency(Currency.getInstance(getCurrencyPair()));
        return format.format(val);
    }

    double convertBTCtoCurrency(double bal) {
        Double price = getCurrentPrice();

        if (price == null)
            return 0;

        Double btc = formatBalance(bal, BITCOIN_FACTOR);
        BigDecimal result = new BigDecimal(btc * price);
        result = result.setScale(2, BigDecimal.ROUND_HALF_UP);

        return result.doubleValue();
    }

    @NonNull
    public static String formatBitcoinBalanceToString(long bal) {

        BigDecimal newBalance;
        String endTag;

        if (bal < 10000000 && bal > -10000000) {
            newBalance = new BigDecimal(formatBalance(bal, MICRO_BITCOIN_FACTOR));
            endTag = " mBTC";
        } else {
            newBalance = new BigDecimal(formatBalance(bal, BITCOIN_FACTOR));
            endTag = " BTC";
        }

        return newBalance.setScale(4, BigDecimal.ROUND_HALF_UP) + endTag;
    }

    private static double formatBalance(double bal, int factor) {

        BigDecimal a = new BigDecimal(bal);
        BigDecimal divider = new BigDecimal("" + factor);

        return a.divide(
                divider,
                7,
                BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

    public static Option<ACCOUNT_TYPE> verifyAddress(String qrString) {

        if (qrString == null) return Option.none();

        if (FormatsUtilGeneric.getInstance().isValidBitcoinAddress(qrString, MainNetParams.get()))
            return Option.some(ACCOUNT_TYPE.BITCOIN);

        if (FormatsUtilGeneric.getInstance().isValidXpub(qrString))
            return Option.some(ACCOUNT_TYPE.SEGWIT);

        return Option.none();
    }

    private void deleteNicknameFromPrefs(String address) {
        preferences.deleteNickname(address);
    }

    private void saveNicknameToPrefs(String address, String name) {
        preferences.saveNickname(address, name);
    }

    private void saveInvestmentToPrefs(long investment) {
        preferences.saveInvestment(investment);
    }

    public void saveInvestment(long investment) {
        saveInvestmentToPrefs(investment);
    }

    public void updateCurrency(Double price) {
        currentPrice = price;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String pair) {
        currencyPair = pair;
        preferences.saveCurrencyPair(pair);
    }

    public String getExchangeRate() {
        Double cPrice = getCurrentPrice();

        if (cPrice == null || cPrice == 0) return "";

        String formated = formatCurrency(cPrice);

        return "Rate: " + formated;
    }

    private static Locale getCurrentLocale(String currencyPair) {
        switch (currencyPair) {
            case "NOK":
                return new Locale("no", "NO");
            case "SEK":
                return new Locale("sv", "SV");
            case "DKK":
                return new Locale("da", "DA");
            default:
                return Locale.getDefault();
        }

    }

    private Double getCurrentPrice() {
        return currentPrice;
    }

    public static String getConvertedTimeStamp(Long time) {

        DateFormat formatter = DateFormat.getDateTimeInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time * 1000);

        return formatter.format(calendar.getTime());
    }
}

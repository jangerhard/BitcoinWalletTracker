package io.github.jangerhard.BitcoinWalletTracker;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.params.MainNetParams;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BitcoinUtils {

    private static final int BITCOIN_FACTOR = 100000000;
    private static final int MICRO_BITCOIN_FACTOR = 100000;
    static final int LOADING_ACCOUNT = -1;
    private final int BIG_QR_SIZE = 512;
    private final int REGULAR_QR_SIZE = 70;

    private List<BitcoinAccount> accountList;
    private List<String> addresses;
    private Map<String, Bitmap> bitmapList;
    private Map<String, Bitmap> bigBitmapList;

    private SharedPreferences sharedPref;
    private String prefsAccountsKey;
    private Double currentPrice;
    private String currencyPair;
    private long totalBalance;

    BitcoinUtils(SharedPreferences sharedPref, String key) {
        accountList = new ArrayList<>();
        addresses = new ArrayList<>();
        bitmapList = new HashMap<>();
        bigBitmapList = new HashMap<>();
        this.sharedPref = sharedPref;
        prefsAccountsKey = key;
    }

    void setup() {
        addAddressesFromPrefs();
        makeAccounts();
        createBitmaps();
        currencyPair = sharedPref.getString("currencyPair", "USD");
        totalBalance = calculateTotalBalance(accountList);
        bigBitmapList.put("1MArRnVPrMf6FR4FqtEThAa8piUbgfYDQ3", createQRThumbnail("1MArRnVPrMf6FR4FqtEThAa8piUbgfYDQ3", BIG_QR_SIZE));
    }

    private void createBitmaps() {
        for (String address : addresses) {
            bitmapList.put(address, createQRThumbnail(address, REGULAR_QR_SIZE));
            bigBitmapList.put(address, createQRThumbnail(address, BIG_QR_SIZE));
        }
    }

    List<BitcoinAccount> getAccounts() {
        return accountList;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    String getNickname(String address) {
        return sharedPref.getString(address, "Wallet");
    }

    void setNewNickname(String selectedAccount, String newNickname) {
        saveNicknameToPrefs(selectedAccount, newNickname);
    }

    void addNewAccount(BitcoinAccount newAcc) {
        totalBalance = calculateTotalBalance(accountList);
        accountList.add(newAcc);
    }

    /**
     * @param refreshedAccount A BitcoinAccount that is being refreshed
     * @return Given an account to update returns the index where it is updated
     */
    int updateAccount(BitcoinAccount refreshedAccount) {

        int index = getAccountIndex(refreshedAccount.getAddress());

        if (index != -1) {
            accountList.set(index, refreshedAccount);
        } else {
            index = 0;
            accountList.add(index, refreshedAccount);
        }
        totalBalance = calculateTotalBalance(accountList);
        return index;
    }

    void removeAccount(String selectedAccountTag) {

        deleteNicknameFromPrefs(selectedAccountTag);
        bitmapList.remove(getAccountIndex(selectedAccountTag));
        bigBitmapList.remove(getAccountIndex(selectedAccountTag));
        accountList.remove(getAccountIndex(selectedAccountTag));
        addresses.remove(selectedAccountTag);
        totalBalance = calculateTotalBalance(accountList);
        saveAddressesToPrefs();
    }

    void addAddress(String address) {
        if (!addresses.contains(address)) {
            addresses.add(address);
            bitmapList.put(address, createQRThumbnail(address, REGULAR_QR_SIZE));
            bigBitmapList.put(address, createQRThumbnail(address, BIG_QR_SIZE));
            saveAddressesToPrefs();
        }
    }

    boolean hasAddress(String displayValue) {
        return addresses.contains(displayValue);
    }

    int getNumberOfAccounts() {
        return accountList.size();
    }

    /**
     * @param accAddress Address of the account which index is needed.
     * @return -1 if account does not exist, or index of account if it exists.
     */
    private int getAccountIndex(String accAddress) {

        for (BitcoinAccount acc : accountList)
            if (acc.getAddress().equals(accAddress))
                return accountList.indexOf(acc);

        return -1;
    }

    static long calculateTotalBalance(List<BitcoinAccount> accounts) {
        if (accounts.size() == 0)
            return 0;

        long total = 0;

        for (BitcoinAccount acc : accounts) total += acc.getFinal_balance();

        return total;
    }

    String getTotalBalance() {
        return formatBitcoinBalanceToString(totalBalance);
    }

    String getTotalValue() {
        return formatCurrency(convertBTCtoCurrency(totalBalance));
    }

    String getTotalInvestmentPercentage() {

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

    String getTotalInvestmentGain() {

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

    long getTotalInvestment() {
        return getInvestmentFromPrefs();
    }

    String getTotalInvestmentFormated() {

        return formatCurrency(getTotalInvestment());

    }

    String formatBTCtoCurrency(long btc) {
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
    static String formatBitcoinBalanceToString(long bal) {

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

    static boolean verifyAddress(String qrString) {

        if (qrString == null)
            return false;

        try {
            Address.fromBase58(MainNetParams.get(), qrString);
        } catch (AddressFormatException e) {
            return false;
        }
        return true;

    }

    private void deleteNicknameFromPrefs(String address) {
        sharedPref.edit().remove(address).apply();
    }

    private void saveNicknameToPrefs(String address, String name) {
        sharedPref.edit().putString(address, name).apply();
    }

    private long getInvestmentFromPrefs() {
        return sharedPref.getLong("totalInvestment", 0L);
    }

    private void saveInvestmentToPrefs(long investment) {
        sharedPref.edit().putLong("totalInvestment", investment).apply();
    }

    void saveInvestment(long investment) {
        saveInvestmentToPrefs(investment);
    }

    public void updateCurrency(Double price) {
        currentPrice = price;
    }

    private void addAddressesFromPrefs() {

        String savedString = sharedPref.getString(prefsAccountsKey, "");

        if (savedString.length() != 0) {
            String[] items = new String[1];

            if (savedString.contains(","))
                items = savedString.split(",");
            else
                items[0] = savedString;

            Collections.addAll(addresses, items);
        }
    }

    private void makeAccounts() {
        if (accountList.size() != 0)
            return;

        BitcoinAccount acc;
        for (String address : addresses) {
            acc = new BitcoinAccount();
            acc.setAddress(address);
            accountList.add(acc);
        }
    }

    private void saveAddressesToPrefs() {
        StringBuilder addressString = new StringBuilder();
        for (String s : addresses) {
            addressString.append(s);
            addressString.append(",");
        }
        sharedPref.edit().putString(prefsAccountsKey, addressString.toString()).apply();
    }

    String getBalanceOfAccount(String selectedAccountAddress) {

        int index = getAccountIndex(selectedAccountAddress);

        if (index != -1)
            return formatBitcoinBalanceToString(accountList.get(index).getFinal_balance());
        else
            return "0 BTC";

    }

    private static Bitmap createQRThumbnail(String address, int size) {
        Bitmap bitmap = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2); /* default = 4 */
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(address, BarcodeFormat.QR_CODE, size, size, hints);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    Bitmap getBigQRThumbnail(String address) {
        return bigBitmapList.get(address);
    }

    Bitmap getQRThumbnail(String address) {
        return bitmapList.get(address);
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    void setCurrencyPair(String pair) {
        currencyPair = pair;
        sharedPref.edit().putString("currencyPair", pair).apply();
    }

    String getExchangeRate() {
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

    /**
     * Returns the value associated with the account address; Positive if received, and negative
     * if paid.
     *
     * @param t       - Transaction related to an account
     * @param address - The address of the account
     * @return BigInteger value, positive if received, and negative
     * if paid. If no transaction is associated, returns 0.
     */
    static long getTransactionValue(Transaction t, String address) {
        if (t.getOut() == null && t.getInputs() == null)
            return 0;

        // Paid
        for (TransactionInput i : t.getInputs()) {
            TransactionPrevOut p = i.getPrev_out();
            if (p != null && p.getAddr() != null && p.getAddr().equals(address)) {
                return -i.getPrev_out().getValue();
            }
        }
        // Received
        for (TransactionOut o : t.getOut()) {
            if (o.getAddr() != null && o.getAddr().equals(address))
                return o.getValue();
        }

        return 0;
    }

    static String getConvertedTimeStamp(Long time) {

        DateFormat formatter = DateFormat.getDateTimeInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time * 1000);

        return formatter.format(calendar.getTime());
    }
}

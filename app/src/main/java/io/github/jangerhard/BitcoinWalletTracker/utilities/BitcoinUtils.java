package io.github.jangerhard.BitcoinWalletTracker.utilities;

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

public class BitcoinUtils {

    private static final int BITCOIN_FACTOR = 100000000;
    private static final int MICRO_BITCOIN_FACTOR = 100000;
    public static final int LOADING_ACCOUNT = -1;
    private final int BIG_QR_SIZE = 512;
    private final int REGULAR_QR_SIZE = 70;

    private List<BitcoinAccount> accountList;
    private List<String> addresses;
    private Map<String, Bitmap> bitmapList;
    private Map<String, Bitmap> bigBitmapList;

    private List<TrackedWallet> trackedWallets;

    private SharedPreferencesHelper preferences;
    private Double currentPrice;
    private String currencyPair;
    private long totalBalance;

    public BitcoinUtils(SharedPreferencesHelper helper, String donationAddress) {
        accountList = new ArrayList<>();
        addresses = new ArrayList<>();
        bitmapList = new HashMap<>();
        bigBitmapList = new HashMap<>();
        preferences = helper;

        trackedWallets = getAddressesFromPrefs();

        addAddressesFromPrefs();
        makeAccounts();
        createBitmaps();
        currencyPair = helper.getCurrencyPair();
        totalBalance = calculateTotalBalance(accountList);
        bigBitmapList.put(donationAddress, createQRThumbnail(donationAddress, BIG_QR_SIZE));
    }

    public List<TrackedWallet> getAddressesFromPrefs() {
        return io.vavr.collection.List.of(preferences.getAccountsString().split(","))
                .map(TrackedWallet::new)
                .toJavaList();
    }

    private void createBitmaps() {
        for (String address : addresses) {
            bitmapList.put(address, createQRThumbnail(address, REGULAR_QR_SIZE));
            bigBitmapList.put(address, createQRThumbnail(address, BIG_QR_SIZE));
        }
    }

    public List<BitcoinAccount> getAccounts() {
        return accountList;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public String getNickname(String address) {
        return preferences.getNickname(address);
    }

    public void setNewNickname(String selectedAccount, String newNickname) {
        saveNicknameToPrefs(selectedAccount, newNickname);
    }

    public void addNewAccount(BitcoinAccount newAcc) {
        totalBalance = calculateTotalBalance(accountList);
        accountList.add(newAcc);
    }

    /**
     * @param refreshedAccount A BitcoinAccount that is being refreshed
     * @return Given an account to update returns the index where it is updated
     */
    public int updateAccount(BitcoinAccount refreshedAccount) {

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

    public void removeAccount(String selectedAccountTag) {

        deleteNicknameFromPrefs(selectedAccountTag);
        bitmapList.remove(getAccountIndex(selectedAccountTag));
        bigBitmapList.remove(getAccountIndex(selectedAccountTag));
        accountList.remove(getAccountIndex(selectedAccountTag));
        addresses.remove(selectedAccountTag);
        totalBalance = calculateTotalBalance(accountList);
        saveAddressesToPrefs();
    }

    public void addAddress(String address) {
        if (!addresses.contains(address)) {
            addresses.add(address);
            bitmapList.put(address, createQRThumbnail(address, REGULAR_QR_SIZE));
            bigBitmapList.put(address, createQRThumbnail(address, BIG_QR_SIZE));
            saveAddressesToPrefs();
        }
    }

    public boolean hasAddress(String displayValue) {
        return addresses.contains(displayValue);
    }

    public int getNumberOfAccounts() {
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

    public static long calculateTotalBalance(List<BitcoinAccount> accounts) {
        if (accounts.size() == 0)
            return 0;

        long total = 0;

        for (BitcoinAccount acc : accounts) total += acc.getFinal_balance();

        return total;
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
        return getInvestmentFromPrefs();
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

    public static boolean verifyAddress(String qrString) {

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
        preferences.deleteNickname(address);
    }

    private void saveNicknameToPrefs(String address, String name) {
        preferences.saveNickname(address, name);
    }

    private long getInvestmentFromPrefs() {
        return preferences.getInvestmentFromPrefs();
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

    private void addAddressesFromPrefs() {

        String savedString = preferences.getAccountsString();

        if (savedString != null && savedString.length() != 0) {
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
        preferences.saveAddresses(addresses);
    }

    public String getBalanceOfAccount(String selectedAccountAddress) {

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

    public Bitmap getBigQRThumbnail(String address) {
        return bigBitmapList.get(address);
    }

    public Bitmap getQRThumbnail(String address) {
        return bitmapList.get(address);
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

    /**
     * Returns the value associated with the account address; Positive if received, and negative
     * if paid.
     *
     * @param t       - Transaction related to an account
     * @param address - The address of the account
     * @return BigInteger value, positive if received, and negative
     * if paid. If no transaction is associated, returns 0.
     */
    public static long getTransactionValue(Transaction t, String address) {
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

    public static String getConvertedTimeStamp(Long time) {

        DateFormat formatter = DateFormat.getDateTimeInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time * 1000);

        return formatter.format(calendar.getTime());
    }
}

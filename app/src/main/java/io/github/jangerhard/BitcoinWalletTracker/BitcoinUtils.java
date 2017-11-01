package io.github.jangerhard.BitcoinWalletTracker;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class BitcoinUtils {

    private static final int BITCOIN_FACTOR = 100000000;
    private static final int MICRO_BITCOIN_FACTOR = 100000;
    private final int BIG_QR_SIZE = 512;
    private final int REGULAR_QR_SIZE = 128;

    private List<BitcoinAccount> accountList;
    private List<String> addresses;
    private SharedPreferences sharedPref;
    private String prefsAccountsKey;
    private Double currentPrice;
    private List<Bitmap> bitmapList;
    private List<Bitmap> bigBitmapList;

    BitcoinUtils(SharedPreferences sharedPref, String key) {
        accountList = new ArrayList<>();
        addresses = new ArrayList<>();
        bitmapList = new ArrayList<>();
        bigBitmapList = new ArrayList<>();
        this.sharedPref = sharedPref;
        prefsAccountsKey = key;
    }

    void setup() {
        addAddressesFromPrefs();
        makeAccounts();
        createBitmaps();
    }

    private void createBitmaps() {
        for (String address : addresses) {
            bitmapList.add(createQRThumbnail(address, REGULAR_QR_SIZE));
            bigBitmapList.add(createQRThumbnail(address, BIG_QR_SIZE));
        }
    }

    List<BitcoinAccount> getAccounts() {
        return accountList;
    }

    List<String> getAddresses() {
        return addresses;
    }

    String getNickname(String address) {
        return sharedPref.getString(address, "Wallet");
    }

    void setNewNickname(String selectedAccount, String newNickname) {
        saveNicknameToPrefs(selectedAccount, newNickname);
    }

    void addNewAccount(BitcoinAccount newAcc) {
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
        return index;
    }

    void removeAccount(String selectedAccountTag) {

        deleteNicknameFromPrefs(selectedAccountTag);
        bitmapList.remove(getAccountIndex(selectedAccountTag));
        accountList.remove(getAccountIndex(selectedAccountTag));
        addresses.remove(selectedAccountTag);
        saveAddressesToPrefs();
    }

    void addAddress(String address) {
        if (!addresses.contains(address)) {
            addresses.add(address);
            bitmapList.add(createQRThumbnail(address, REGULAR_QR_SIZE));
            bitmapList.add(createQRThumbnail(address, BIG_QR_SIZE));
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

    static BigInteger calculateTotalBalance(List<BitcoinAccount> accounts) {
        if (accounts.size() == 0)
            return new BigInteger("0");

        BigInteger total = new BigInteger("0");

        for (BitcoinAccount acc : accounts) {
            if (acc.getFinal_balance() != null)
                total = total.add(acc.getFinal_balance());
        }
        return total;
    }

    String getTotalBalance() {
        return formatBitcoinBalanceToString(calculateTotalBalance(accountList));
    }

    String getTotalValue() {
        return formatCurrency(convertBTCtoCurrency(calculateTotalBalance(accountList)));
    }

    String getTotalInvestmentPercentage() {

        double investment = getTotalInvestment();

        if (investment == 0)
            return "";

        double totalVal = convertBTCtoCurrency(calculateTotalBalance(accountList));

        double result = (totalVal - investment) / investment * 100;

        return "(" + BigDecimal.valueOf(result)
                .setScale(3, RoundingMode.HALF_UP)
                .toEngineeringString() + "%)";

    }

    long getTotalInvestment() {
        return getInvestmentFromPrefs();
    }

    String getTotalInvestmentFormated() {

        return formatCurrency(getTotalInvestment());

    }

    String formatBTCtoCurrency(BigInteger btc) {
        return formatCurrency(convertBTCtoCurrency(btc));
    }

    String formatCurrency(double val) {
        NumberFormat format = NumberFormat.getCurrencyInstance(getCurrentLocale());
        format.setCurrency(Currency.getInstance(getCurrencyPair()));
        return format.format(val);
    }

    double convertBTCtoCurrency(BigInteger bal) {

        if (bal == null || bal.intValue() == 0) {
            return 0L;
        }

        BigDecimal btc = new BigDecimal(formatBalance(bal.toString(), BITCOIN_FACTOR));
        BigDecimal price = new BigDecimal(getCurrentPrice().toString());
        BigDecimal result = btc.multiply(price, MathContext.DECIMAL32);
        result = result.setScale(2, BigDecimal.ROUND_HALF_UP);

        return result.doubleValue();
    }

    @NonNull
    static String formatBitcoinBalanceToString(BigInteger bal) {
        if (bal == null || bal.intValue() == 0)
            return "0.0000 BTC";

        String balance = bal.toString();
        BigDecimal newBalance;
        String tag;

        if (balance.length() < 8) {
            newBalance = new BigDecimal(formatBalance(balance, MICRO_BITCOIN_FACTOR));
            tag = " mBTC";
        } else {
            newBalance = new BigDecimal(formatBalance(balance, BITCOIN_FACTOR));
            tag = " BTC";
        }

        return newBalance.setScale(4, BigDecimal.ROUND_HALF_UP) + tag;
    }

    private static String formatBalance(String bal, int factor) {
        if (bal == null)
            return "";

        BigDecimal a = new BigDecimal(bal);
        BigDecimal divider = new BigDecimal("" + factor);

        return a.divide(
                divider,
                7,
                BigDecimal.ROUND_HALF_DOWN)
                .toEngineeringString();
    }

    static boolean verifyAddress(String qrString) {

        if (qrString == null)
            return false;

        /*
          A Bitcoin address is between 25 and 34 characters long;
          the address always starts with a 1;
          an address can contain all alphanumeric characters,
          with the exceptions of 0, O, I, and l.

         */

        return (qrString.length() <= 34 && qrString.length() >= 25) &&
                (qrString.substring(0, 1).equals("1") || (qrString.substring(0, 1).equals("3"))) &&
                (!qrString.contains("0")) && (!qrString.contains("O")) &&
                (!qrString.contains("I")) && (!qrString.contains("l"));

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

    void updateCurrency(Double price) {
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
        return bigBitmapList.get(addresses.indexOf(address));
    }

    Bitmap getQRThumbnail(String address) {
        return bitmapList.get(addresses.indexOf(address));
    }

    static String getCurrencyPair() {
        //TODO: fix
        return "NOK";
    }

    String getExchangeRate() {
        return "1 BTC = " + formatCurrency(getCurrentPrice());
    }

    private static Locale getCurrentLocale() {
        return new Locale("no", "NO");
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
    static BigInteger getTransactionValue(Transaction t, String address) {
        if (t.getOut() == null && t.getInputs() == null)
            return new BigInteger("0");

        // Paid
        for (TransactionInput i : t.getInputs()) {
            TransactionPrevOut p = i.getPrev_out();
            if (p != null && p.getAddr() != null && p.getAddr().equals(address))
                return i.getPrev_out().getValue().negate();
        }
        // Received
        for (TransactionOut o : t.getOut()) {
            if (o.getAddr() != null && o.getAddr().equals(address))
                return o.getValue();
        }

        return new BigInteger("0");
    }

    static String getConvertedTimeStamp(Long time) {

        DateFormat formatter = DateFormat.getDateTimeInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time * 1000);

        return formatter.format(calendar.getTime());
    }
}

package io.github.jangerhard.BitcoinWalletTracker;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BitcoinUtils {

    private static final String LOG_TAG = "BitcoinUtil";
    private List<BitcoinAccount> accountList;
    private List<String> addresses;
    private SharedPreferences sharedPref;
    private String prefsAccountsKey;
    private Double currentPrice;
    private List<Bitmap> bitmapMap;

    BitcoinUtils(SharedPreferences sharedPref, String key) {
        accountList = new ArrayList<>();
        addresses = new ArrayList<>();
        bitmapMap = new ArrayList<>();
        this.sharedPref = sharedPref;
        prefsAccountsKey = key;
        addAddressesFromPrefs();
        makeAccounts();
        createBitmaps();
    }

    private void createBitmaps() {
        for (String address : addresses)
            bitmapMap.add(createQRThumbnail(address));
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
        accountList.remove(getAccountIndex(selectedAccountTag));
        addresses.remove(selectedAccountTag);
        saveAddressesToPrefs();
    }

    void addAddress(String address) {
        if (!addresses.contains(address)) {
            addresses.add(address);
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

    String totalBalance() {

        if (accountList.isEmpty())
            return "0 mBTC";

        BigInteger total = new BigInteger("0");

        for (BitcoinAccount acc : this.getAccounts()) {
            total = total.add(acc.getFinal_balance());
        }
        return formatBitcoinBalanceToString(total);
    }

    String formatPriceToString(BigInteger bal) {
        if (bal == null)
            return "";
        BigDecimal btc = new BigDecimal(formatBitcoin(bal));
        BigDecimal result = btc.multiply(new BigDecimal("" + currentPrice));
        return result.setScale(2, BigDecimal.ROUND_HALF_UP) + getCurrencyPair();
    }

    static String formatBitcoinBalanceToString(BigInteger bal) {
        if (bal == null)
            return "";

        String balance = formatBitcoin(bal);

        if (balance.startsWith("0.0"))
            return balance.substring(2, balance.length()) + " mBTC";
        else
            return balance + " BTC";
    }

    private static String formatBitcoin(BigInteger bal) {
        if (bal == null)
            return "";

        BigDecimal a = new BigDecimal(bal);
        BigDecimal divider = new BigDecimal("100000000");

        return a.divide(
                divider,
                4,
                BigDecimal.ROUND_HALF_UP)
                .toEngineeringString();
    }

    static boolean verifyAddress(String qrString) {
        /*
          A Bitcoin address is between 25 and 34 characters long;
          the address always starts with a 1;
          an address can contain all alphanumeric characters,
          with the exceptions of 0, O, I, and l.

         */
        Log.d(LOG_TAG, "Address: " + qrString);
        if (!(qrString.length() <= 34 && qrString.length() >= 25))
            Log.d(LOG_TAG, "Wrong length");
        if (!(qrString.substring(0, 1).equals("1") || (qrString.substring(0, 1).equals("3"))))
            Log.d(LOG_TAG, "Doesn't start with 1 or 3");
        if (qrString.contains("0"))
            Log.d(LOG_TAG, "Contains 0");
        if (qrString.contains("O"))
            Log.d(LOG_TAG, "Contains O");
        if (qrString.contains("I"))
            Log.d(LOG_TAG, "Contains I");
        if (qrString.contains("l"))
            Log.d(LOG_TAG, "Contains l");


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

    void sortAccounts() {

        // TODO: Fix

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

    private Bitmap createQRThumbnail(String address) {
        Bitmap bitmap = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(address, BarcodeFormat.QR_CODE, 128, 128);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private Bitmap createQRBig(String address) {
        Bitmap bitmap = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(address, BarcodeFormat.QR_CODE, 800, 800);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    Bitmap getQRThumbnail(String address) {
        return bitmapMap.get(addresses.indexOf(address));
    }

    String getCurrencyPair() {
        //TODO: fix
        return "NOK";
    }
}

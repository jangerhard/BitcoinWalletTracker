package com.example.jangerhard.BitcoinWalletTracker;

import android.content.SharedPreferences;
import android.util.Log;

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

    public BitcoinUtils() {
        accountList = new ArrayList<>();
        addresses = new ArrayList<>();
    }

    List<BitcoinAccount> getAccounts() {
        return accountList;
    }

    List<String> getAddresses() {
        return addresses;
    }

    void updateAccount(BitcoinAccount refreshedAccount) {
        accountList.set(addresses.indexOf(refreshedAccount.getAddress()), refreshedAccount);
    }

    void setPending(String address) {
        accountList.get(addresses.indexOf(address)).setNickName("Pending ...");
    }

    void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    void setNewNickname(int selectedAccountTag, String name) {
        accountList.get(selectedAccountTag).setNickName(name);
        saveNickname(accountList.get(selectedAccountTag).getAddress(), name);
    }

    private void saveNickname(String address, String name) {
        sharedPref.edit().putString(address, name).apply();
    }

    void addAddress(String address) {
        addresses.add(address);
    }

    void addAccount(BitcoinAccount account) {
        accountList.add(account);
    }

    void addAccount(int location, BitcoinAccount account) {
        accountList.add(location, account);
    }

    void removeAccount(int number) {
        accountList.remove(number);
    }

    void removeAddress(String address) {
        addresses.remove(address);
    }

    boolean noAddresses() {
        return addresses.isEmpty();
    }

    void clearAccounts() {
        accountList.clear();
    }

    int numAddresses() {
        return addresses.size();
    }

    String totalBalance() {
        BigInteger total = new BigInteger("0");

        for (BitcoinAccount acc : this.getAccounts()) {
            total = total.add(acc.getFinal_balance());
        }
        return formatBitcoinBalanceToString(total);
    }

    static String formatBitcoinBalanceToString(BigInteger bal) {
        if (bal == null)
            return "";

        BigDecimal a = new BigDecimal(bal);
        BigDecimal divider;
        String endTag;

        if (bal.toString().length() < 7) {
            divider = new BigDecimal("10000");
            endTag = "mBTC";
        } else {
            divider = new BigDecimal("100000000");
            endTag = "BTC";
        }
        return a.divide(
                divider,
                3,
                BigDecimal.ROUND_HALF_UP)
                .toEngineeringString() + " " + endTag;

    }

    private void createAccountsList(SharedPreferences sharedPref) {

        for (String address : addresses) {
            BitcoinAccount b = new BitcoinAccount();
            b.setAddress(address);
            b.setNickName(sharedPref.getString(address, "Paper Wallet"));
            accountList.add(b);
        }

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

    boolean addAddressesFromPrefs(SharedPreferences sharedPref, String key) {
        this.sharedPref = sharedPref;
        String defaultAddress = "";
        List<String> addresses = BitcoinUtils
                .createAddressListFromPrefs(
                        sharedPref.getString(
                                key, defaultAddress));

        // Empty
        if (addresses.get(0).length() < 2) {
            addresses.clear();
            return false;
        }

        setAddresses(addresses);
        createAccountsList(sharedPref);
        return true;
    }

    static String createAddressStringToPrefs(List<String> addresses) {
        StringBuilder csvList = new StringBuilder();
        for (String s : addresses) {
            csvList.append(s);
            csvList.append(",");
        }
        return csvList.toString();
    }

    private static List<String> createAddressListFromPrefs(String addresses) {
        String[] items = new String[1];

        if (addresses.contains(","))
            items = addresses.split(",");
        else
            items[0] = addresses;

        List<String> list = new ArrayList<>();
        Collections.addAll(list, items);
        return list;
    }
}

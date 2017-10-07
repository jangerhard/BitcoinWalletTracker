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
    private String prefsAccountsKey;

    BitcoinUtils(SharedPreferences sharedPref, String key) {
        accountList = new ArrayList<>();
        addresses = new ArrayList<>();
        this.sharedPref = sharedPref;
        prefsAccountsKey = key;
        addAddressesFromPrefs();
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

        accountList.get(getAccountIndex(selectedAccount)).setNickname(newNickname);
        saveNicknameToPrefs(selectedAccount, newNickname);

    }

    void updateAccount(BitcoinAccount refreshedAccount) {

        int index = getAccountIndex(refreshedAccount.getAddress());

        if (index != -1)
            accountList.set(index, refreshedAccount);
        else
            accountList.add(refreshedAccount);
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

    private void saveAddressesToPrefs() {
        StringBuilder addressString = new StringBuilder();
        for (String s : addresses) {
            addressString.append(s);
            addressString.append(",");
        }
        sharedPref.edit().putString(prefsAccountsKey, addressString.toString()).apply();
    }
}

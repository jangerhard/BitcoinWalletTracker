package io.github.jangerhard.BitcoinWalletTracker.utilities;

import java.util.List;

import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private final String CURRENCY_PAIR = "currencyPair";
    private final String TOTAL_INVESTMENT = "totalInvestment";
    private final String DARK_THEME_SELECTED = "dark_theme_selected";
    private final String REFRESHING_THEME = "refreshing_theme";
    private final String SHOW_GAIN_PERCENTAGE = "show_gain_percentage";
    private final String PREFS_ACCOUNT_KEY = "BitcoinAddresses";

    private SharedPreferences preferences;

    public SharedPreferencesHelper(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void toggleDarkThemeSelected(boolean b) {
        preferences.edit().putBoolean(DARK_THEME_SELECTED, b).apply();
        preferences.edit().putBoolean(REFRESHING_THEME, true).apply();
    }

    public boolean isDarkTheme() {return preferences.getBoolean(DARK_THEME_SELECTED, true);}

    public boolean isThemeRefreshing() {return preferences.getBoolean(REFRESHING_THEME, false);}

    public void stopThemeRefreshing() { preferences.edit().putBoolean(REFRESHING_THEME, false).apply();}

    public String getCurrencyPair() {
        return preferences.getString(CURRENCY_PAIR, "USD");
    }

    public void saveCurrencyPair(String pair) {
        preferences.edit().putString(CURRENCY_PAIR, pair).apply();
    }

    public String getNickname(String address) {return preferences.getString(address, "Wallet");}

    public void deleteNickname(String address) { preferences.edit().remove(address).apply(); }

    public void saveNickname(String address, String name) {
        preferences.edit().putString(address, name).apply();
    }

    public long getInvestmentFromPrefs() {
        return preferences.getLong(TOTAL_INVESTMENT, 0L);
    }

    public void saveInvestment(long investment) {
        preferences.edit().putLong(TOTAL_INVESTMENT, investment).apply();
    }

    public void setRefreshingTheme(boolean b) {
        preferences.edit().putBoolean(REFRESHING_THEME, b).apply();
    }

    public boolean shouldShowGainInPercentage() {return preferences.getBoolean(DARK_THEME_SELECTED, true);}

    public void setShowGainInPercentage(boolean b) {
        preferences.edit().putBoolean(SHOW_GAIN_PERCENTAGE, b).apply();
    }

    public String getAccountsString() {
        return preferences.getString(PREFS_ACCOUNT_KEY, "");
    }

    @Deprecated
    public void saveAddresses(List<String> addresses) {
        StringBuilder addressString = new StringBuilder();
        for (String s : addresses) {
            addressString.append(s);
            addressString.append(",");
        }
        preferences.edit().putString(PREFS_ACCOUNT_KEY, addressString.toString()).apply();
    }

    public void saveTrackedWallets(io.vavr.collection.List<TrackedWallet> addresses) {
        String s = addresses
                .map(TrackedWallet::getAddress)
                .intersperse(",")
                .foldLeft(new StringBuilder(), StringBuilder::append)
                .toString();
        preferences.edit().putString(PREFS_ACCOUNT_KEY, s).apply();
    }
}

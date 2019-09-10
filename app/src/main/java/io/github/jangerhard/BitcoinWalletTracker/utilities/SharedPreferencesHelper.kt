package io.github.jangerhard.BitcoinWalletTracker.utilities

import android.content.SharedPreferences
import io.vavr.collection.List
import io.vavr.control.Option

class SharedPreferencesHelper(private val preferences: SharedPreferences) {

    private val CURRENCY_PAIR = "currencyPair"
    private val TOTAL_INVESTMENT = "totalInvestment"
    private val DARK_THEME_SELECTED = "dark_theme_selected"
    private val REFRESHING_THEME = "refreshing_theme"
    private val SHOW_GAIN_PERCENTAGE = "show_gain_percentage"
    private val PREFS_ACCOUNT_KEY = "BitcoinAddresses"

    fun isDarkTheme() = preferences.getBoolean(DARK_THEME_SELECTED, true)

    fun isThemeRefreshing() = preferences.getBoolean(REFRESHING_THEME, false)

    fun getCurrency(): Option<String> {

        val currency = preferences.getString(CURRENCY_PAIR, "USD")

        if (currency.isNullOrBlank()) return Option.some("USD")

        return Option.some(currency)
    }

    fun getInvestment() = preferences.getLong(TOTAL_INVESTMENT, 0L)

    fun getAddresses(): List<String> {

        val s = preferences.getString(PREFS_ACCOUNT_KEY, "")

        if (s.isNullOrBlank()) return List.empty()

        return List.ofAll(s.split(","))
    }

    fun toggleDarkThemeSelected(b: Boolean) {
        preferences.edit().putBoolean(DARK_THEME_SELECTED, b).apply()
        preferences.edit().putBoolean(REFRESHING_THEME, true).apply()
    }

    fun stopThemeRefreshing() {
        preferences.edit().putBoolean(REFRESHING_THEME, false).apply()
    }

    fun saveCurrencyPair(pair: String) {
        preferences.edit().putString(CURRENCY_PAIR, pair).apply()
    }

    fun getNickname(address: String): String? {
        return preferences.getString(address, "Wallet")
    }

    fun deleteNickname(address: String) {
        preferences.edit().remove(address).apply()
    }

    fun saveNickname(address: String, name: String) {
        preferences.edit().putString(address, name).apply()
    }

    fun saveInvestment(investment: Long) {
        preferences.edit().putLong(TOTAL_INVESTMENT, investment).apply()
    }

    fun shouldShowGainInPercentage(): Boolean =
            preferences.getBoolean(DARK_THEME_SELECTED, true)

    fun setShowGainInPercentage(b: Boolean) {
        preferences.edit().putBoolean(SHOW_GAIN_PERCENTAGE, b).apply()
    }

    fun saveTrackedWallets(addresses: List<TrackedWallet>) {
        preferences.edit().putString(PREFS_ACCOUNT_KEY,
                addresses.map { it.address }.mkString(",")).apply()
    }
}

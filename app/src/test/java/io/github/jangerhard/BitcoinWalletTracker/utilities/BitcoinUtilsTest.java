package io.github.jangerhard.BitcoinWalletTracker.utilities;

import io.vavr.control.Option;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class BitcoinUtilsTest {

    private SharedPreferencesHelper preferences = Mockito.mock(SharedPreferencesHelper.class);

    private BitcoinUtils bitcoinUtils;

    @Before
    public void setup() {
        when(preferences.getCurrencyPair()).thenReturn("USD");
        when(preferences.getAccountsString()).thenReturn("1LVuX2MLwerH6sFb25HnyCFS8Zcuxc2u1s,1H6a4TidysCEV91PDdQZmyEphpJD9M7VmN");
        bitcoinUtils = new BitcoinUtils(preferences);
    }

    @Test
    public void getting_wallets_from_prefs_given_existing_addresses_returns_list() {
        assertFalse(bitcoinUtils.getAddressesFromPrefs().isEmpty());
    }

    @Test
    public void getting_wallets_from_prefs_given_no_existing_addresses_returns_list() {
        when(preferences.getAccountsString()).thenReturn("");
        assertTrue(bitcoinUtils.getAddressesFromPrefs().isEmpty());
    }

    @Test
    public void testFormatingBalanceToString() {

        String bal = BitcoinUtils.formatBitcoinBalanceToString(2195820L);
        assertEquals("21.9582 mBTC", bal);

        bal = BitcoinUtils.formatBitcoinBalanceToString(21958200L);
        assertEquals("0.2196 BTC", bal);
        bal = BitcoinUtils.formatBitcoinBalanceToString(219582000L);
        assertEquals("2.1958 BTC", bal);

        bal = BitcoinUtils.formatBitcoinBalanceToString(0L);
        assertEquals("0.0000 mBTC", bal);

        bal = BitcoinUtils.formatBitcoinBalanceToString(3428272196L);
        assertEquals("34.2827 BTC", bal);

        bal = BitcoinUtils.formatBitcoinBalanceToString(541721794L);
        assertEquals("5.4172 BTC", bal);

        bal = BitcoinUtils.formatBitcoinBalanceToString(-3428272196L);
        assertEquals("-34.2827 BTC", bal);

        bal = BitcoinUtils.formatBitcoinBalanceToString(-541721794L);
        assertEquals("-5.4172 BTC", bal);
    }

    @Test
    public void given_correct_bitcoin_address_return_BITCOIN_address() {

        // Real address, should return true
        Option<BitcoinUtils.ACCOUNT_TYPE> maybeVerified =
                BitcoinUtils.verifyAddress("1LVuX2MLwerH6sFb25HnyCFS8Zcuxc2u1s");
        assertTrue(maybeVerified.isDefined());
        assertEquals(BitcoinUtils.ACCOUNT_TYPE.BITCOIN, maybeVerified.get());

        // Real address, should return true
        maybeVerified = BitcoinUtils.verifyAddress("1H6a4TidysCEV91PDdQZmyEphpJD9M7VmN");
        assertTrue(maybeVerified.isDefined());
        assertEquals(BitcoinUtils.ACCOUNT_TYPE.BITCOIN, maybeVerified.get());
    }

    @Test
    public void given_correct_segwit_address_using_xpub_return_SEGWIT_address() {
        // Real address, should return true
        Option<BitcoinUtils.ACCOUNT_TYPE> maybeVerified =
                BitcoinUtils.verifyAddress("xpub68KFnj3bqUx1s7mHejLDBPywCAKdJEu1b49uniEEn2WSbHmZ7xbLqFTjJbtx1LUcAt1DwhoqWHmo2s5WMJp6wi38CiF2hYD49qVViKVvAoi");
        assertTrue(maybeVerified.isDefined());
        assertEquals(BitcoinUtils.ACCOUNT_TYPE.SEGWIT, maybeVerified.get());
    }

    @Test
    public void given_correct_segwit_address_using_ypub_return_SEGWIT_address() {
        // Real address, should return true
        Option<BitcoinUtils.ACCOUNT_TYPE> maybeVerified =
                BitcoinUtils.verifyAddress("ypub6QqdH2c5z79681jUgdxjGJzGW9zpL4ryPCuhtZE4GpvrJoZqM823XQN6iSQeVbbbp2uCRQ9UgpeMcwiyV6qjvxTWVcxDn2XEAnioMUwsrQ5");
        assertTrue(maybeVerified.isDefined());
        assertEquals(BitcoinUtils.ACCOUNT_TYPE.SEGWIT, maybeVerified.get());
    }

    @Test
    public void given_wrong_address_return_none() {

        // Wrong address, should return false
        Option<BitcoinUtils.ACCOUNT_TYPE> maybeVerified = BitcoinUtils.verifyAddress("randobambo");
        assertFalse(maybeVerified.isDefined());
        // Wrong address, should return false
        maybeVerified = BitcoinUtils.verifyAddress("43ghcYT1EKNKUzgGaXnv8qty16mifc8iy2");
        assertFalse(maybeVerified.isDefined());

        maybeVerified = BitcoinUtils.verifyAddress("");
        assertFalse(maybeVerified.isDefined());

        maybeVerified = BitcoinUtils.verifyAddress(null);
        assertFalse(maybeVerified.isDefined());
    }

}

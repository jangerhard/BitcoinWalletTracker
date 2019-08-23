package io.github.jangerhard.BitcoinWalletTracker.utilities;

import java.util.ArrayList;

import io.github.jangerhard.BitcoinWalletTracker.client.BlockinfoResponse;
import io.vavr.collection.List;
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
    public void when_updating_tracked_wallet_with_updated_info_given_no_previous_info_return_new_wallet() {
        String someAddress = "someAddress";
        TrackedWallet wallet = new TrackedWallet(someAddress);
        BlockinfoResponse account = new BlockinfoResponse();
        account.setAddress(someAddress);
        account.setFinal_balance(2000);
        account.setN_tx(2);

        assertFalse(wallet.getAssosiatedAccount().isDefined());

        wallet = bitcoinUtils.updateAssociatedAccount(wallet, account);

        assertTrue(wallet.getAssosiatedAccount().isDefined());
        assertEquals(2000, wallet.getCurrentBalance().get().longValue());
        assertEquals(2, wallet.getNumberOfTransactions().get().longValue());
    }

    @Test
    public void when_updating_tracked_wallet_with_updated_info__given_old_info_return_new_wallet() {
        String someAddress = "someAddress";
        TrackedWallet wallet = new TrackedWallet(someAddress);
        wallet.setAssosiatedAccount(
                createBitcoinAccount(someAddress, 2000, 2));

        assertTrue(wallet.getAssosiatedAccount().isDefined());
        assertEquals(2000, wallet.getCurrentBalance().get().longValue());
        assertEquals(2, wallet.getNumberOfTransactions().get().longValue());

        wallet = bitcoinUtils.updateAssociatedAccount(wallet,
                createBitcoinAccount(someAddress, 3000, 3));

        assertTrue(wallet.getAssosiatedAccount().isDefined());
        assertEquals(3000, wallet.getCurrentBalance().get().longValue());
        assertEquals(3, wallet.getNumberOfTransactions().get().longValue());
    }

    @Test
    public void when_updating_tracked_wallet_with_outdated_info_return_same_wallet() {
        String someAddress = "someAddress";
        TrackedWallet wallet = new TrackedWallet(someAddress);
        wallet.setAssosiatedAccount(
                createBitcoinAccount(someAddress, 2000, 2));

        assertTrue(wallet.getAssosiatedAccount().isDefined());
        assertEquals(2000, wallet.getCurrentBalance().get().longValue());
        assertEquals(2, wallet.getNumberOfTransactions().get().longValue());

        wallet = bitcoinUtils.updateAssociatedAccount(wallet,
                createBitcoinAccount(someAddress, 0, 0));

        assertTrue(wallet.getAssosiatedAccount().isDefined());
        assertEquals(2000, wallet.getCurrentBalance().get().longValue());
        assertEquals(2, wallet.getNumberOfTransactions().get().longValue());
    }

    private BlockinfoResponse createBitcoinAccount(String address, long balance, long transactions) {
        BlockinfoResponse account = new BlockinfoResponse();
        account.setAddress(address);
        account.setFinal_balance(balance);
        account.setN_tx(transactions);
        return account;
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
    public void testCalculateTotalAmount() {

        long a = 12345678;
        TrackedWallet wallet = new TrackedWallet("Someaddress");
        BlockinfoResponse acc = new BlockinfoResponse();
        acc.setFinal_balance(a);
        wallet.setAssosiatedAccount(acc);
        List<TrackedWallet> accounts = List.of(
                wallet,
                wallet,
                wallet,
                wallet,
                wallet
        );

        assertEquals(61728390, BitcoinUtils.calculateTotalBalance(accounts));

        assertEquals(0, BitcoinUtils.calculateTotalBalance(List.empty()));

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

//    @Test
//    public void testCurrency() throws Exception {
//
//        MainActivity mActivity = new MainActivity();
//        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
//
//        BitcoinUtils utils = new BitcoinUtils(sharedPref, mActivity.getString(R.string.bitcoinaddresses));
//
//        // 1 btc to NOK
//        utils.updateCurrency(44783.44d);
//        // 0.223 btc
//        long balance = 2195820;
//
//        assertEquals("kr 983,36", utils.formatCurrency(utils.convertBTCtoCurrency(balance)));
//
//        // 1 btc to NOK
//        utils.updateCurrency(45006.70d);
//        // 0.223 btc
//        balance = 23300000;
//
//        assertEquals("kr 10 486,56", utils.formatCurrency(utils.convertBTCtoCurrency(balance)));
//
//        assertEquals("kr 0,00", utils.formatCurrency(utils.convertBTCtoCurrency(0)));
//
//    }

    @Test
    public void testGetTransactionValue() {

        String testAddress = "testAddr";

        Transaction t = new Transaction();
        TransactionInput i = new TransactionInput();
        TransactionPrevOut prevOut = new TransactionPrevOut();
        prevOut.setAddr("testAddr");
        prevOut.setValue(12345);
        i.setPrev_out(prevOut);
        ArrayList<TransactionInput> tOList = new ArrayList<>();
        tOList.add(i);
        t.setInputs(tOList);

        assertEquals(-12345, BitcoinUtils.getTransactionValue(t, testAddress));

        t = new Transaction();
        TransactionOut o = new TransactionOut();
        o.setAddr(testAddress);
        o.setValue(12345);
        ArrayList<TransactionOut> tL = new ArrayList<>();
        tL.add(o);
        t.setOut(tL);
        i = new TransactionInput();
        prevOut = new TransactionPrevOut();
        prevOut.setAddr("wrongAddr");
        i.setPrev_out(prevOut);
        tOList.clear();
        tOList.add(i);
        t.setInputs(tOList);
        assertEquals(12345, BitcoinUtils.getTransactionValue(t, testAddress));
    }

}

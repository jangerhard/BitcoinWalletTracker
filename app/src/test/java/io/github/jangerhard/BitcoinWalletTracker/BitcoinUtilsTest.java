package io.github.jangerhard.BitcoinWalletTracker;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class BitcoinUtilsTest {

    @Test
    public void testFormatingBalanceToString() throws Exception {

        String bal = BitcoinUtils.formatBitcoinBalanceToString(new BigInteger("2195820"));
        assertEquals("21.9582 mBTC", bal);

        bal = BitcoinUtils.formatBitcoinBalanceToString(new BigInteger("21958200"));
        assertEquals("0.2196 BTC", bal);
        bal = BitcoinUtils.formatBitcoinBalanceToString(new BigInteger("219582000"));
        assertEquals("2.1958 BTC", bal);

        bal = BitcoinUtils.formatBitcoinBalanceToString(null);
        assertEquals("0.0000 BTC", bal);

        bal = BitcoinUtils.formatBitcoinBalanceToString(new BigInteger("0"));
        assertEquals("0.0000 BTC", bal);
    }

    @Test
    public void testVerifyAddress() throws Exception {

        // Real address, should return true
        Boolean verified = BitcoinUtils.verifyAddress("1LVuX2MLwerH6sFb25HnyCFS8Zcuxc2u1s");
        assertEquals(true, verified);

        // Real address, should return true
        verified = BitcoinUtils.verifyAddress("1H6a4TidysCEV91PDdQZmyEphpJD9M7VmN");
        assertEquals(true, verified);

        verified = BitcoinUtils.verifyAddress("");
        assertEquals(false, verified);
        verified = BitcoinUtils.verifyAddress(null);
        assertEquals(false, verified);

    }

    @Test
    public void testCurrency() throws Exception {

        MainActivity mActivity = new MainActivity();
        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);

        BitcoinUtils utils = new BitcoinUtils(sharedPref, mActivity.getString(R.string.bitcoinaddresses));

        // 1 btc to NOK
        utils.updateCurrency(44783.44d);
        // 0.223 btc
        BigInteger balance = new BigInteger("2195820");

        Double price = 983.36d;

        assertEquals(price.toString() + "NOK", utils.formatPriceToString(balance));

        // 1 btc to NOK
        utils.updateCurrency(45006.70d);
        // 0.223 btc
        balance = new BigInteger("23300000");

        price = 10486.56d;

        assertEquals(price.toString() + "NOK", utils.formatPriceToString(balance));

        assertEquals("0.00NOK", utils.formatPriceToString(null));

        assertEquals("0.00NOK", utils.formatPriceToString(new BigInteger("0")));


    }


}
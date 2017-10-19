package io.github.jangerhard.BitcoinWalletTracker;

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


}
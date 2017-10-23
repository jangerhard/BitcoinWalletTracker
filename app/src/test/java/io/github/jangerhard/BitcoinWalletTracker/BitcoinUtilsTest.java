package io.github.jangerhard.BitcoinWalletTracker;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
    public void testCalculateTotalAmount() throws Exception {

        BigInteger a = new BigInteger("12345678");
        BitcoinAccount acc = new BitcoinAccount();
        acc.setFinal_balance(a);
        List<BitcoinAccount> accounts = new ArrayList<>();
        accounts.add(acc);
        accounts.add(acc);
        accounts.add(acc);
        accounts.add(acc);
        accounts.add(acc);

        BigInteger expectedTotal = new BigInteger("61728390");

        assertEquals(expectedTotal, BitcoinUtils.calculateTotalBalance(accounts));

        expectedTotal = new BigInteger("0");
        assertEquals(expectedTotal, BitcoinUtils.calculateTotalBalance(new ArrayList<BitcoinAccount>()));

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

        assertEquals("kr 983,36", utils.formatPriceToString(balance));

        // 1 btc to NOK
        utils.updateCurrency(45006.70d);
        // 0.223 btc
        balance = new BigInteger("23300000");

        assertEquals("kr 10 486,56", utils.formatPriceToString(balance));

        assertEquals("kr 0,00", utils.formatPriceToString(null));

        assertEquals("kr 0,00", utils.formatPriceToString(new BigInteger("0")));

    }

    @Test
    public void testGetTransactionValue() throws Exception {

        String testAddress = "testAddr";

        Transaction t = new Transaction();
        TransactionInput i = new TransactionInput();
        TransactionPrevOut prevOut = new TransactionPrevOut();
        prevOut.setAddr("testAddr");
        prevOut.setValue(new BigInteger("12345"));
        i.setPrev_out(prevOut);
        ArrayList<TransactionInput> tOList = new ArrayList<>();
        tOList.add(i);
        t.setInputs(tOList);

        assertEquals(new BigInteger("-12345"), BitcoinUtils.getTransactionValue(t, testAddress));

        t = new Transaction();
        TransactionOut o = new TransactionOut();
        o.setAddr(testAddress);
        o.setValue(new BigInteger("12345"));
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
        assertEquals(new BigInteger("12345"), BitcoinUtils.getTransactionValue(t, testAddress));
    }

}
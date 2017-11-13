package io.github.jangerhard.BitcoinWalletTracker;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BitcoinUtilsTest {

    @Test
    public void testFormatingBalanceToString() throws Exception {

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
    public void testCalculateTotalAmount() throws Exception {

        long a = 12345678;
        BitcoinAccount acc = new BitcoinAccount();
        acc.setFinal_balance(a);
        List<BitcoinAccount> accounts = new ArrayList<>();
        accounts.add(acc);
        accounts.add(acc);
        accounts.add(acc);
        accounts.add(acc);
        accounts.add(acc);

        long expectedTotal = 61728390;

        assertEquals(expectedTotal, BitcoinUtils.calculateTotalBalance(accounts));

        expectedTotal = 0;
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
        // Real address, should return true
        verified = BitcoinUtils.verifyAddress("3KHDFXJQC9eD4MEMi2bUFhyXaQ5DEpT7JG");
        assertEquals(true, verified);
        // Real address, should return true
        verified = BitcoinUtils.verifyAddress("3ARbVBdz6jfg7WUMSBsMe5cvaxghbHs6Ch");
        assertEquals(true, verified);
        // Real address, should return true
        verified = BitcoinUtils.verifyAddress("3J98t1WpEZ73CNmQviecrnyiWrnqRhWNLy");
        assertEquals(true, verified);
        // Real address, should return true
        verified = BitcoinUtils.verifyAddress("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2");
        assertEquals(true, verified);

        // Wrong address, should return false
        verified = BitcoinUtils.verifyAddress("33ghcYT1EKNKUzgGaXnv8qty16mifc8iy2");
        assertEquals(false, verified);
        // Wrong address, should return false
        verified = BitcoinUtils.verifyAddress("43ghcYT1EKNKUzgGaXnv8qty16mifc8iy2");
        assertEquals(false, verified);
        // Wrong address, should return false
        verified = BitcoinUtils.verifyAddress("133ghcYT1EKNKUzgGaXnv8qty16mifc8iy2");
        assertEquals(false, verified);
        // Wrong address, should return false
        verified = BitcoinUtils.verifyAddress("333ghcYT1EKNKUzgGaXnv8qty16mifc8iy2");
        assertEquals(false, verified);

        verified = BitcoinUtils.verifyAddress("");
        assertEquals(false, verified);
        verified = BitcoinUtils.verifyAddress(null);
        assertEquals(false, verified);

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
    public void testGetTransactionValue() throws Exception {

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
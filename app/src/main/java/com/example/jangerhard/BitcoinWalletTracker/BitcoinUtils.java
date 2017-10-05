package com.example.jangerhard.BitcoinWalletTracker;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

class BitcoinUtils {

    static String totalBalance(List<BitcoinAccount> accounts){
        BigInteger total = new BigInteger("0");

        for (BitcoinAccount acc: accounts) {
            total = total.add(acc.getFinal_balance());
        }
        return formatBitcoinBalanceToString(total);
    }

    static String formatBitcoinBalanceToString(BigInteger bal) {
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

}

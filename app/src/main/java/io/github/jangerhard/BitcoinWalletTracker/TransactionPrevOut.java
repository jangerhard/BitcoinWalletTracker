package io.github.jangerhard.BitcoinWalletTracker;

import java.math.BigInteger;

/**
 * Created by jangerhard on 23-Oct-17.
 */

class TransactionPrevOut {

    private boolean spent;
    private String addr;
    private BigInteger value;

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public boolean isSpent() {
        return spent;
    }

    public void setSpent(boolean spent) {
        this.spent = spent;
    }
}

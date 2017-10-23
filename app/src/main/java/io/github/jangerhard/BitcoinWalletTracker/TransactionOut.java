package io.github.jangerhard.BitcoinWalletTracker;

import java.math.BigInteger;

class TransactionOut {

    private boolean spent;
    private BigInteger value;
    private String addr;

    public boolean isSpent() {
        return spent;
    }

    public void setSpent(boolean spent) {
        this.spent = spent;
    }

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
}

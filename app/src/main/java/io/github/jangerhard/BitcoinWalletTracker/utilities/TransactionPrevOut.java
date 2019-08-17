package io.github.jangerhard.BitcoinWalletTracker.utilities;

/**
 * Created by jangerhard on 23-Oct-17.
 */

class TransactionPrevOut {

    private boolean spent;
    private String addr;
    private long value;

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
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

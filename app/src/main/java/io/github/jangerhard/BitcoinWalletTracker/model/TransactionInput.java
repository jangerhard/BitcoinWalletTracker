package io.github.jangerhard.BitcoinWalletTracker.model;

/**
 * Created by jangerhard on 22-Oct-17.
 */

public class TransactionInput {

    private TransactionPrevOut prev_out;

    public TransactionPrevOut getPrev_out() {
        return prev_out;
    }

    public void setPrev_out(TransactionPrevOut prev_out) {
        this.prev_out = prev_out;
    }
}

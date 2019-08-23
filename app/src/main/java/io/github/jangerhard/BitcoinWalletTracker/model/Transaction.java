package io.github.jangerhard.BitcoinWalletTracker.model;

import io.vavr.collection.List;

public class Transaction {

    private long result;
    private List<TransactionOut> out;
    private List<TransactionInput> inputs;
    private long time;

    public long getResult() {
        return result;
    }

    public void setResult(long result) {
        this.result = result;
    }

    public List<TransactionOut> getOut() {
        return out;
    }

    public void setOut(List<TransactionOut> out) {
        this.out = out;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<TransactionInput> inputs) {
        this.inputs = inputs;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

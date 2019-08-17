package io.github.jangerhard.BitcoinWalletTracker.utilities;

import java.util.ArrayList;

public class Transaction {

    private long result;
    private ArrayList<TransactionOut> out;
    private ArrayList<TransactionInput> inputs;
    private long time;

    public long getResult() {
        return result;
    }

    public void setResult(long result) {
        this.result = result;
    }

    public ArrayList<TransactionOut> getOut() {
        return out;
    }

    public void setOut(ArrayList<TransactionOut> out) {
        this.out = out;
    }

    public ArrayList<TransactionInput> getInputs() {
        return inputs;
    }

    public void setInputs(ArrayList<TransactionInput> inputs) {
        this.inputs = inputs;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

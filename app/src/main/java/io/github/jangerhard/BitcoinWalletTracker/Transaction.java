package io.github.jangerhard.BitcoinWalletTracker;

import java.math.BigInteger;
import java.util.ArrayList;

class Transaction {

    private BigInteger result;
    private ArrayList<TransactionOut> out;
    private ArrayList<TransactionInput> inputs;
    private BigInteger time;

    public BigInteger getResult() {
        return result;
    }

    public void setResult(BigInteger result) {
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

    public BigInteger getTime() {
        return time;
    }

    public void setTime(BigInteger time) {
        this.time = time;
    }
}

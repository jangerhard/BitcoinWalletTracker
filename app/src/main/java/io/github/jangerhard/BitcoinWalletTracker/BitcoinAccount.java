package io.github.jangerhard.BitcoinWalletTracker;

import java.util.ArrayList;

public class BitcoinAccount {

    private String address;
    private long n_tx = BitcoinUtils.LOADING_ACCOUNT;
    private long total_received;
    private long total_sent;
    private long final_balance;
    private ArrayList<Transaction> txs;

    String getAddress() {
        return address;
    }

    void setAddress(String address) {
        this.address = address;
    }

    long getN_tx() {
        return n_tx;
    }

    void setN_tx(long n_tx) {
        this.n_tx = n_tx;
    }

    long getTotal_received() {
        return total_received;
    }

    void setTotal_received(long total_received) {
        this.total_received = total_received;
    }

    long getTotal_sent() {
        return total_sent;
    }

    void setTotal_sent(long total_sent) {
        this.total_sent = total_sent;
    }

    long getFinal_balance() {
        return final_balance;
    }

    void setFinal_balance(long final_balance) {
        this.final_balance = final_balance;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BitcoinAccount acc = (BitcoinAccount) obj;

        return (address.equals(acc.getAddress()) &&
                n_tx == acc.n_tx &&
                total_received == acc.total_received &&
                total_sent == acc.total_sent &&
                final_balance == acc.final_balance);
    }

    @Override
    public String toString() {
        return address + " has a balance of " + getFinal_balance() + "btc.";
    }

    public ArrayList<Transaction> getTxs() {
        return txs;
    }

    public void setTxs(ArrayList<Transaction> txs) {
        this.txs = txs;
    }
}

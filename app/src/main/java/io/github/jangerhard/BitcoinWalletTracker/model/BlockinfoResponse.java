package io.github.jangerhard.BitcoinWalletTracker.model;

import io.vavr.collection.List;

public class BlockinfoResponse {

    private String address;
    private long n_tx;
    private long total_received;
    private long total_sent;
    private long final_balance;
    private List<Transaction> txs;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getN_tx() {
        return n_tx;
    }

    public void setN_tx(long n_tx) {
        this.n_tx = n_tx;
    }

    public long getTotal_received() {
        return total_received;
    }

    public void setTotal_received(long total_received) {
        this.total_received = total_received;
    }

    public long getTotal_sent() {
        return total_sent;
    }

    public void setTotal_sent(long total_sent) {
        this.total_sent = total_sent;
    }

    public long getFinal_balance() {
        return final_balance;
    }

    public void setFinal_balance(long final_balance) {
        this.final_balance = final_balance;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BlockinfoResponse acc = (BlockinfoResponse) obj;

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

    public List<Transaction> getTxs() {
        return txs;
    }

    public void setTxs(List<Transaction> txs) {
        this.txs = txs;
    }
}

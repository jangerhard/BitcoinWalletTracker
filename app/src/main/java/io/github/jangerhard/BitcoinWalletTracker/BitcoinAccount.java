package io.github.jangerhard.BitcoinWalletTracker;

import android.view.View;

import java.math.BigInteger;

class BitcoinAccount {

    private String hash160;
    private String address;
    private BigInteger n_tx;
    private BigInteger n_unredeemed;
    private BigInteger total_received;
    private BigInteger total_sent;
    private BigInteger final_balance;

    private View.OnClickListener removeAccountListener;

    private View.OnClickListener shareAccountListener;
    private View.OnClickListener foldAccountListener;

    String getHash160() {
        return hash160;
    }

    void setHash160(String hash160) {
        this.hash160 = hash160;
    }

    String getAddress() {
        return address;
    }

    void setAddress(String address) {
        this.address = address;
    }

    BigInteger getN_tx() {
        return n_tx;
    }

    void setN_tx(BigInteger n_tx) {
        this.n_tx = n_tx;
    }

    BigInteger getN_unredeemed() {
        return n_unredeemed;
    }

    void setN_unredeemed(BigInteger n_unredeemed) {
        this.n_unredeemed = n_unredeemed;
    }

    BigInteger getTotal_received() {
        return total_received;
    }

    void setTotal_received(BigInteger total_received) {
        this.total_received = total_received;
    }

    BigInteger getTotal_sent() {
        return total_sent;
    }

    void setTotal_sent(BigInteger total_sent) {
        this.total_sent = total_sent;
    }

    BigInteger getFinal_balance() {
        return final_balance;
    }

    void setFinal_balance(BigInteger final_balance) {
        this.final_balance = final_balance;
    }

    public View.OnClickListener getRemoveAccountListener() {
        return removeAccountListener;
    }

    public void setRemoveAccountListener(View.OnClickListener removeAccountListener) {
        this.removeAccountListener = removeAccountListener;
    }

    public View.OnClickListener getShareAccountListener() {
        return shareAccountListener;
    }

    public void setShareAccountListener(View.OnClickListener shareAccountListener) {
        this.shareAccountListener = shareAccountListener;
    }

    public View.OnClickListener getFoldAccountListener() {
        return foldAccountListener;
    }

    public void setFoldAccountListener(View.OnClickListener foldAccountListener) {
        this.foldAccountListener = foldAccountListener;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BitcoinAccount acc = (BitcoinAccount) obj;

        return (address.equals(acc.getAddress()) &&
                hash160.equals(acc.hash160) &&
                n_tx.equals(acc.n_tx) &&
                n_unredeemed.equals(acc.n_unredeemed) &&
                total_received.equals(acc.total_received) &&
                total_sent.equals(acc.total_sent) &&
                final_balance.equals(acc.final_balance));
    }

    @Override
    public String toString() {
        return address + " has a balance of " + getFinal_balance() + "btc.";
    }
}

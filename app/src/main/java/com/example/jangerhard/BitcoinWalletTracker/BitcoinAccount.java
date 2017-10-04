package com.example.jangerhard.BitcoinWalletTracker;

import java.math.BigInteger;

/**
 * Created by jangerhard on 03-Oct-17.
 */

public class BitcoinAccount {

    private String hash160;
    private String address;
    private BigInteger n_tx;
    private BigInteger n_unredeemed;
    private BigInteger total_received;
    private BigInteger total_sent;
    private BigInteger final_balance;

    private String nickName = "NewAccount";

    public String getHash160() {
        return hash160;
    }

    public void setHash160(String hash160) {
        this.hash160 = hash160;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        setNickName(address);
    }

    public BigInteger getN_tx() {
        return n_tx;
    }

    public void setN_tx(BigInteger n_tx) {
        this.n_tx = n_tx;
    }

    public BigInteger getN_unredeemed() {
        return n_unredeemed;
    }

    public void setN_unredeemed(BigInteger n_unredeemed) {
        this.n_unredeemed = n_unredeemed;
    }

    public BigInteger getTotal_received() {
        return total_received;
    }

    public void setTotal_received(BigInteger total_received) {
        this.total_received = total_received;
    }

    public BigInteger getTotal_sent() {
        return total_sent;
    }

    public void setTotal_sent(BigInteger total_sent) {
        this.total_sent = total_sent;
    }

    public BigInteger getFinal_balance() {
        return final_balance;
    }

    public void setFinal_balance(BigInteger final_balance) {
        this.final_balance = final_balance;
    }

    public void setNickName(String name) {
        nickName = name;
    }

    public String getNickName() {
        return nickName;
    }

    @Override
    public String toString() {
        return getNickName() + " has a balance of " + getFinal_balance() + "btc.";
    }

    public String getFormatedBalance() {
        String bal = getFinal_balance().toString();
//        14226287 = 0.142
        if (bal.length() < 9)
            return "0." + bal.substring(0, 3) + " BTC";
        else
            return bal.substring(0, bal.length() - 8) +
                    "." +
                    bal.substring(bal.length() - 7, bal.length()) + " BTC";
    }
}

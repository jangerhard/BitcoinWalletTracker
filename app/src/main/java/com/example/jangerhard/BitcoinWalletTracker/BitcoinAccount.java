package com.example.jangerhard.BitcoinWalletTracker;

import java.math.BigInteger;

class BitcoinAccount {

    private String hash160;
    private String address;
    private BigInteger n_tx;
    private BigInteger n_unredeemed;
    private BigInteger total_received;
    private BigInteger total_sent;
    private BigInteger final_balance;

    private String nickname = "Wallet";

    String getNickname() {
        return nickname;
    }

    void setNickname(String nickname) {
        this.nickname = nickname;
    }

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

    @Override
    public String toString() {
        return nickname + " has a balance of " + getFinal_balance() + "btc.";
    }
}

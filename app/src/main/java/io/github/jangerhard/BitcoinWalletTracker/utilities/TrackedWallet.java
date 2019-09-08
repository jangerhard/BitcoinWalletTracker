package io.github.jangerhard.BitcoinWalletTracker.utilities;

import java.util.Objects;

import android.graphics.Bitmap;
import io.github.jangerhard.BitcoinWalletTracker.model.BlockonomicsTransactionsResponse.Transaction;
import io.vavr.collection.List;

import static io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.QR_SIZE.BIG;
import static io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.QR_SIZE.REGULAR;
import static io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.createQRThumbnail;

public class TrackedWallet {

    private String address;
    private Bitmap bigQRImage;
    private Bitmap regularQRImage;

    private long final_balance;
    private List<Transaction> transactions;

    public TrackedWallet(String address) {
        this.address = address;
        final_balance = 0;
        transactions = List.empty();
    }

    public String getFormattedBalance() {
        return BitcoinUtils.formatBitcoinBalanceToString(final_balance);
    }

    public String getAddress() {
        return address;
    }

    public Bitmap getBigQRImage() {
        if (bigQRImage == null) bigQRImage = createQRThumbnail(address, BIG);
        return bigQRImage;
    }

    public Bitmap getRegularQRImage() {
        if (bigQRImage == null) regularQRImage = createQRThumbnail(address, REGULAR);
        return regularQRImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackedWallet that = (TrackedWallet) o;
        return address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    public long getFinal_balance() {
        return final_balance;
    }

    public void setFinal_balance(long final_balance) {
        this.final_balance = final_balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}

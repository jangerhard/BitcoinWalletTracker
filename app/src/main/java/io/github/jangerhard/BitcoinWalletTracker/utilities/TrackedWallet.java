package io.github.jangerhard.BitcoinWalletTracker.utilities;

import java.util.Objects;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import io.github.jangerhard.BitcoinWalletTracker.model.BlockonomicsTransactionsResponse.Transaction;
import io.vavr.collection.List;

import static io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.QR_SIZE.BIG;
import static io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.QR_SIZE.REGULAR;
import static io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.createQRThumbnail;

public class TrackedWallet implements Parcelable {

    private String address;
    private String nickname = "Wallet";

    private long final_balance = 0;

    private Bitmap bigQRImage;
    private Bitmap regularQRImage;

    private List<Transaction> transactions = List.empty();

    public TrackedWallet(String address) {
        this.address = address;
    }

    protected TrackedWallet(Parcel in) {
        address = in.readString();
        nickname = in.readString();
        final_balance = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(nickname);
        dest.writeLong(final_balance);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TrackedWallet> CREATOR = new Creator<TrackedWallet>() {
        @Override
        public TrackedWallet createFromParcel(Parcel in) {
            return new TrackedWallet(in);
        }

        @Override
        public TrackedWallet[] newArray(int size) {
            return new TrackedWallet[size];
        }
    };

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
        if (regularQRImage == null) regularQRImage = createQRThumbnail(address, REGULAR);
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}

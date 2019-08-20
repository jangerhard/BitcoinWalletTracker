package io.github.jangerhard.BitcoinWalletTracker.utilities;

import java.util.ArrayList;

import android.graphics.Bitmap;
import io.vavr.control.Option;

import static io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.QR_SIZE.BIG;
import static io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.QR_SIZE.REGULAR;
import static io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.createQRThumbnail;

public class TrackedWallet {

    private String address;
    private Bitmap bigQRImage;
    private Bitmap regularQRImage;

    private BitcoinAccount assosiatedAccount;

    public TrackedWallet(String address) {
        this.address = address;
        bigQRImage = createQRThumbnail(address, BIG);
        regularQRImage = createQRThumbnail(address, REGULAR);
    }

    public Option<Long> getCurrentBalance() {
        if (assosiatedAccount == null) return Option.none();

        return Option.of(assosiatedAccount.getFinal_balance());
    }

    public String getAddress() {
        return address;
    }

    public Bitmap getBigQRImage() {
        return bigQRImage;
    }

    public Bitmap getRegularQRImage() {
        return regularQRImage;
    }

    public void setAssosiatedAccount(BitcoinAccount assosiatedAccount) {
        this.assosiatedAccount = assosiatedAccount;
    }

    public Option<BitcoinAccount> getAssosiatedAccount() {return Option.of(assosiatedAccount);}
}

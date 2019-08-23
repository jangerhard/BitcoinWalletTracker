package io.github.jangerhard.BitcoinWalletTracker.utilities;

import java.util.Objects;

import android.graphics.Bitmap;
import io.github.jangerhard.BitcoinWalletTracker.model.BlockinfoResponse;
import io.vavr.control.Option;

import static io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.QR_SIZE.BIG;
import static io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.QR_SIZE.REGULAR;
import static io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.createQRThumbnail;

public class TrackedWallet {

    private String address;
    private Bitmap bigQRImage;
    private Bitmap regularQRImage;

    private BlockinfoResponse assosiatedAccount;

    public TrackedWallet(String address) {
        this.address = address;
    }

    public String getFormattedBalance() {
        return BitcoinUtils.formatBitcoinBalanceToString(
                getCurrentBalance().getOrElse(0L)
        );
    }

    public Option<Long> getCurrentBalance() {
        if (assosiatedAccount == null) return Option.none();

        return Option.of(assosiatedAccount.getFinal_balance());
    }

    public Option<Long> getNumberOfTransactions() {
        if (assosiatedAccount == null) return Option.none();

        return Option.of(assosiatedAccount.getN_tx());
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

    public void setAssosiatedAccount(BlockinfoResponse assosiatedAccount) {
        this.assosiatedAccount = assosiatedAccount;
    }

    public Option<BlockinfoResponse> getAssosiatedAccount() {return Option.of(assosiatedAccount);}

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
}

package io.github.jangerhard.BitcoinWalletTracker.utilities

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import io.github.jangerhard.BitcoinWalletTracker.model.BlockonomicsTransactionsResponse.Transaction
import io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.QR_SIZE.BIG
import io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.QR_SIZE.REGULAR
import io.github.jangerhard.BitcoinWalletTracker.qrStuff.BitmapCreator.createQRThumbnail
import io.vavr.collection.List

class TrackedWallet(
        var address: String
) : Parcelable {

    var nickname: String = "Wallet"

    var finalBalance: Long = 0

    private var bigQRImage: Bitmap? = null
    private var regularQRImage: Bitmap? = null

    var transactions: List<Transaction> = List.empty<Transaction>()

    fun getBalanceAsString() = BitcoinUtils.formatBitcoinBalanceToString(this.finalBalance)

    constructor(parcel: Parcel) : this(parcel.readString()) {
        nickname = parcel.readString()
        finalBalance = parcel.readLong()
        bigQRImage = parcel.readParcelable(Bitmap::class.java.classLoader)
        regularQRImage = parcel.readParcelable(Bitmap::class.java.classLoader)
    }

    fun getBigQRImage(): Bitmap? {
        if (bigQRImage == null) bigQRImage = createQRThumbnail(address, BIG)
        return bigQRImage
    }

    fun getRegularQRImage(): Bitmap? {
        if (regularQRImage == null) regularQRImage = createQRThumbnail(address, REGULAR)
        return regularQRImage
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrackedWallet

        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int = address.hashCode() ?: 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(address)
        parcel.writeString(nickname)
        parcel.writeLong(finalBalance)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TrackedWallet> {
        override fun createFromParcel(parcel: Parcel): TrackedWallet {
            return TrackedWallet(parcel)
        }

        override fun newArray(size: Int): Array<TrackedWallet?> {
            return arrayOfNulls(size)
        }
    }
}

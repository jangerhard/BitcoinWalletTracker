package io.github.jangerhard.BitcoinWalletTracker.qrStuff;

import java.util.EnumMap;
import java.util.Map;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class BitmapCreator {
    public enum QR_SIZE {REGULAR, BIG}

    private static final int BIG_QR_SIZE = 512;
    private static final int REGULAR_QR_SIZE = 70;

    public static Bitmap createQRThumbnail(String address, QR_SIZE size) {
        switch (size) {
            case REGULAR:
                return createQRThumbnail(address, REGULAR_QR_SIZE);
            case BIG:
                return createQRThumbnail(address, BIG_QR_SIZE);
            default:
                throw new IllegalStateException("Unexpected value: " + size);
        }
    }

    private static Bitmap createQRThumbnail(String address, int size) {
        Bitmap bitmap = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2); /* default = 4 */
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(address, BarcodeFormat.QR_CODE, size, size, hints);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}

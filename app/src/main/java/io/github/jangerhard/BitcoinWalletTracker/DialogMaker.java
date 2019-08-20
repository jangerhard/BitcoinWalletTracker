package io.github.jangerhard.BitcoinWalletTracker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;
import io.github.jangerhard.BitcoinWalletTracker.client.BlockExplorer;
import io.github.jangerhard.BitcoinWalletTracker.qrStuff.barcode.BarcodeCaptureActivity;
import io.github.jangerhard.BitcoinWalletTracker.utilities.BitcoinUtils;
import io.github.jangerhard.BitcoinWalletTracker.utilities.TrackedWallet;

import static io.github.jangerhard.BitcoinWalletTracker.MainActivity.BARCODE_READER_REQUEST_CODE;

public class DialogMaker {
    private MainActivity activity;
    private BlockExplorer blockExplorer;
    private BitcoinUtils utils;

    public DialogMaker(MainActivity activity, BlockExplorer blockExplorer, BitcoinUtils utils) {
        this.activity = activity;
        this.blockExplorer = blockExplorer;
        this.utils = utils;
    }

    public void showBitcoinAddressDialog(final String address) {

        new LovelyStandardDialog(activity)
                .setTopColorRes(R.color.dialog_info)
                .setTitle(R.string.new_address)
                .setIcon(R.drawable.bitcoin_128)
                .setMessage(activity.getString(R.string.question_correct_address) + "\n\n" + address)
                .setPositiveButton(android.R.string.yes, view -> {
                    activity.handleAddedAccount(address);
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    public void showAddAccountDialog() {
        new LovelyTextInputDialog(activity)
                .setTopColorRes(R.color.dialog_info)
                .setTitle("Add address")
                .setIcon(R.drawable.bitcoin_128)
                .setHint("1FfmbHfnpaZjKFvyi1okTjJJusN455paPH")
                .setInputFilter("That is not a valid address!", BitcoinUtils::verifyAddress)
                .setConfirmButton(android.R.string.ok, text -> {
                    utils.addTrackedWallet(text);
                    blockExplorer.getSingleWalletInfo(text);
                })
                .setNegativeButton("Scan", view -> {
                    Toast.makeText(activity, "Launching camera", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity, BarcodeCaptureActivity.class);
                    activity.startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
                })
                .show();
    }

    public void showAccountShareDialog(TrackedWallet trackedWallet) {
        new LovelyStandardDialog(activity)
                .setTopColorRes(R.color.dialog_qr)
                .setIcon(trackedWallet.getBigQRImage())
                .setTitle(trackedWallet.getAddress())
                .setNegativeButton("Copy", view -> {
                    ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("qrCode", trackedWallet.getAddress());
                    if (clipboard != null) {
                        Toast.makeText(activity, "Address copied to clipboard", Toast.LENGTH_SHORT).show();
                        clipboard.setPrimaryClip(clip);
                    }
                })
                .setPositiveButton(R.string.share, v -> {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, trackedWallet.getAddress());
                    sendIntent.setType("text/plain");
                    activity.startActivity(sendIntent);
                })
                .show();
    }

    public void showCurrencySelectorDialog() {
        String[] items = activity.getResources().getStringArray(R.array.currencyNames);
        new LovelyChoiceDialog(activity)
                .setTopColorRes(R.color.dialog_currencies)
                .setTitle(R.string.change_currency_dialog_title)
                .setIcon(R.drawable.ic_language_white_48dp)
                .setItems(items, (position, item) -> {
                    String pair = activity.getResources().getStringArray(R.array.currencies)[position];
                    utils.setCurrencyPair(pair);
                    activity.refreshData();
                })
                .show();
    }

    public void showInvestmentChangeDialog() {
        new LovelyTextInputDialog(activity)
                .setTopColorRes(R.color.dialog_investment)
                .setTitle(R.string.change_investment)
                .setIcon(R.drawable.ic_attach_money_white_48dp)
                .setHint(R.string.total_amount_invested)
                .setInitialInput("" + utils.getTotalInvestment())
                .setInputFilter(R.string.error_investment_input,
                        text -> text.trim().length() == 0 || text.trim().matches("\\d+"))
                .setConfirmButton(android.R.string.ok, text -> {
                    if (text.trim().length() == 0)
                        utils.saveInvestment(Long.parseLong("0"));
                    else utils.saveInvestment(Long.parseLong(text.trim()));
                    activity.updateUI();
                })
                .show();
    }

    public void showCustomViewDialog(View view) {
        new LovelyCustomDialog(activity)
                .setView(view)
                .setTopColorRes(R.color.cardview_dark_background)
                .show();
    }
}

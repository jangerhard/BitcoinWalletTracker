package io.github.jangerhard.BitcoinWalletTracker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.Toast;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;
import io.github.jangerhard.BitcoinWalletTracker.utilities.BitcoinUtils;
import io.github.jangerhard.BitcoinWalletTracker.utilities.TrackedWallet;

public class DialogMaker {
    private MainActivity activity;

    public DialogMaker(MainActivity activity) { this.activity = activity; }

    public void showBitcoinAddressDialog(final String address) {

        new LovelyStandardDialog(activity)
                .setTopColorRes(R.color.dialog_info)
                .setTitle(R.string.new_address)
                .setIcon(R.drawable.bitcoin_128)
                .setMessage(activity.getString(R.string.question_correct_address) + "\n\n" + address)
                .setPositiveButton(android.R.string.yes, view -> activity.handleAddedAccount(address))
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
                .setConfirmButton(android.R.string.ok, text -> activity.handleAddedAccount(text))
                .setNegativeButton("Scan", view -> activity.handleOpenCamera())
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
                .setPositiveButton(R.string.share, v -> activity.handleOpenShare(trackedWallet.getAddress()))
                .show();
    }

    public void showCurrencySelectorDialog() {
        String[] items = activity.getResources().getStringArray(R.array.currencyNames);
        new LovelyChoiceDialog(activity)
                .setTopColorRes(R.color.dialog_currencies)
                .setTitle(R.string.change_currency_dialog_title)
                .setIcon(R.drawable.ic_language_white_48dp)
                .setItems(items, (position, item) -> activity.handleChangeCurrency(position))
                .show();
    }

    public void showInvestmentChangeDialog(long currentInvestment) {
        new LovelyTextInputDialog(activity)
                .setTopColorRes(R.color.dialog_investment)
                .setTitle(R.string.change_investment)
                .setIcon(R.drawable.ic_attach_money_white_48dp)
                .setHint(R.string.total_amount_invested)
                .setInitialInput("" + currentInvestment)
                .setInputFilter(R.string.error_investment_input,
                        text -> text.trim().length() == 0 || text.trim().matches("\\d+"))
                .setConfirmButton(android.R.string.ok, newPrice -> activity.handleUpdateInvestment(newPrice))
                .show();
    }

    public void showCustomViewDialog(View view) {
        new LovelyCustomDialog(activity)
                .setView(view)
                .setTopColorRes(R.color.cardview_dark_background)
                .show();
    }

    public void changeNicknameOnSelected(TrackedWallet wallet, String nickname, int position) {

        new LovelyTextInputDialog(activity)
                .setTopColorRes(R.color.dialog_edit)
                .setTitle(R.string.edit_nickname)
                .setIcon(R.drawable.ic_mode_edit_white_48dp)
                .setHint(R.string.savings)
                .setInitialInput(nickname)
                .setInputFilter(R.string.text_input_error_message, text -> {
                    if (text.length() > 30)
                        return false;
                    Pattern p = Pattern.compile("\\w+");
                    Matcher m = p.matcher(text);
                    return m.find();
                })
                .setConfirmButton(android.R.string.ok, newNickname ->
                        activity.handleUpdatedNickname(wallet.getAddress(), newNickname, position))
                .show();
    }

    public void showRemoveConfirmDialog(TrackedWallet wallet, String nickname, int position) {
        new LovelyStandardDialog(activity)
                .setTopColorRes(R.color.dialog_warning)
                .setIcon(R.drawable.ic_delete_forever_white_48dp)
                .setTitle(activity.getString(R.string.stop_tracking) + " " + nickname + "?")
                .setMessage(activity.getString(R.string.it_has_a_balance_of) + " " + wallet.getFormattedBalance())
                .setPositiveButton(android.R.string.yes, v -> activity.handleRemoveSelectedAccount(wallet, position))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}

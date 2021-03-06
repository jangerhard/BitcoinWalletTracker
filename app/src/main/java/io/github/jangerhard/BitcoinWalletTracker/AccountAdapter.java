package io.github.jangerhard.BitcoinWalletTracker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ramotion.foldingcell.FoldingCell;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.MyViewHolder> {

    private HashSet<Integer> unfoldedIndexes = new HashSet<>();

    private Context mContext;
    private int selectedAccountPosition;
    private String selectedAccountAddress;
    private String selectedAccountNickname;
    private BitcoinUtils utils;

    class MyViewHolder extends RecyclerView.ViewHolder {

        // Folded
        TextView accNickNameFolded, accBalance, accRate;
        private ImageView overflow, overflow2, qrCode;
        int position;

        // Unfolded
        TextView accAddress, accNickNameUnfolded, tvAccNumTxs, tvAccTotReceived, tvAccFinalBalance,
                tvRecentTransaction;
        RecyclerView transactionList;

        MyViewHolder(View view) {
            super(view);

            // Folded
            accNickNameFolded = view.findViewById(R.id.tvAccountNameFolded);
            accBalance = view.findViewById(R.id.tvAccountBalanceFolded);
            accRate = view.findViewById(R.id.tvAccountRateFolded);
            overflow = view.findViewById(R.id.im_OverflowFolded);
            qrCode = view.findViewById(R.id.im_thumbnailFolded);

            // Unfolded
            accAddress = view.findViewById(R.id.tv_unfolded_address);
            accNickNameUnfolded = view.findViewById(R.id.tv_unfolded_nickname);
            overflow2 = view.findViewById(R.id.im_OverflowFolded2);
            transactionList = view.findViewById(R.id.transactionList);
            tvAccNumTxs = view.findViewById(R.id.tv_account_number_transactions);
            tvAccTotReceived = view.findViewById(R.id.tv_account_total_received);
            tvAccFinalBalance = view.findViewById(R.id.tv_account_final_balance);
            tvRecentTransaction = view.findViewById(R.id.tv_unfolded_last_transactions);
        }
    }

    AccountAdapter(Context mContext, BitcoinUtils utils) {
        this.mContext = mContext;
        this.utils = utils;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FoldingCell itemView = (FoldingCell) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final BitcoinAccount account = utils.getAccounts().get(position);

        if (account.getN_tx() == BitcoinUtils.LOADING_ACCOUNT)
            return;

        String nickname = utils.getNickname(account.getAddress());
        holder.accNickNameFolded.setText(nickname);
        holder.accBalance.setText(
                BitcoinUtils.formatBitcoinBalanceToString(
                        account.getFinal_balance())
        );
        holder.accRate.setText(
                utils.formatBTCtoCurrency(account.getFinal_balance()));
        holder.position = holder.getAdapterPosition();
        holder.qrCode.setImageBitmap(
                utils.getQRThumbnail(account.getAddress()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((FoldingCell) view).toggle(false);
                // register in adapter that state for selected cell is toggled
                registerToggle(holder.position);

            }
        });

        holder.qrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupQRCode(account.getAddress());
            }
        });

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedAccountPosition = holder.getAdapterPosition();
                showPopupMenu(holder.overflow);
            }
        });

        holder.overflow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedAccountPosition = holder.getAdapterPosition();
                showPopupMenu(holder.overflow);
            }
        });

        // Unfolded
        holder.accAddress.setText(account.getAddress());
        holder.accNickNameUnfolded.setText(nickname);

        holder.tvAccNumTxs.setText(
                String.format(Locale.ENGLISH, "%d", account.getN_tx()));
        holder.tvAccTotReceived.setText(
                BitcoinUtils.formatBitcoinBalanceToString(account.getTotal_received()));
        holder.tvAccFinalBalance.setText(
                BitcoinUtils.formatBitcoinBalanceToString(account.getFinal_balance()));

        if (account.getN_tx() == 0)
            holder.tvRecentTransaction.setText(mContext.getResources().getString(R.string.no_activity_on_this_address));
        else
            holder.tvRecentTransaction.setText(mContext.getResources().getString(R.string.latest_transactions));

        holder.transactionList.setLayoutManager(
                new LinearLayoutManager(
                        mContext, LinearLayoutManager.VERTICAL, false));

        TransactionAdapter transactionAdapter =
                new TransactionAdapter(mContext, account.getTxs(), account.getAddress());

        holder.transactionList.setAdapter(transactionAdapter);
    }

    private void showPopupQRCode(final String address) {

        new LovelyStandardDialog(mContext)
                .setTopColorRes(R.color.dialog_qr)
                .setIcon(utils.getBigQRThumbnail(address))
                .setTitle(address)
                .setNegativeButton("Copy", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("qrCode", address);
                        if (clipboard != null) {
                            Toast.makeText(mContext, "Address copied to clipboard", Toast.LENGTH_SHORT).show();
                            clipboard.setPrimaryClip(clip);
                        }
                    }
                })
                .setPositiveButton(R.string.share, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareAddress(address);
                    }
                })
                .show();
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view) {
        // inflate menu
        selectedAccountAddress = utils.getAccounts().get(selectedAccountPosition).getAddress();
        selectedAccountNickname = utils.getNickname(selectedAccountAddress);
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_account, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_change_nickname:
                    changeNicknameOnSelected();
                    return true;
                case R.id.action_remove_account:
                    showRemoveConfirmDialog();
                    return true;
                default:
            }
            return false;
        }
    }

    private void removeSelectedAccount() {
        Toast.makeText(mContext,
                "Removed account " + utils.getNickname(selectedAccountAddress),
                Toast.LENGTH_SHORT).show();
        utils.removeAccount(selectedAccountAddress);
        notifyItemRemoved(selectedAccountPosition);
    }

    private void changeNicknameOnSelected() {

        new LovelyTextInputDialog(mContext)
                .setTopColorRes(R.color.dialog_edit)
                .setTitle(R.string.edit_nickname)
                .setIcon(R.drawable.ic_mode_edit_white_48dp)
                .setHint(R.string.savings)
                .setInitialInput(utils.getNickname(utils.getAddresses().get(selectedAccountPosition)))
                .setInputFilter(R.string.text_input_error_message, new LovelyTextInputDialog.TextFilter() {
                    @Override
                    public boolean check(String text) {
                        if (text.length() > 30)
                            return false;
                        Pattern p = Pattern.compile("\\w+");
                        Matcher m = p.matcher(text);
                        return m.find();
                    }
                })
                .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                    @Override
                    public void onTextInputConfirmed(String text) {
                        utils.setNewNickname(selectedAccountAddress, text);
                        notifyItemChanged(selectedAccountPosition);
                    }
                })
                .show();
    }

    private void showRemoveConfirmDialog() {

        new LovelyStandardDialog(mContext)
                .setTopColorRes(R.color.dialog_warning)
                .setIcon(R.drawable.ic_delete_forever_white_48dp)
                .setTitle(mContext.getString(R.string.stop_tracking) + " " + selectedAccountNickname + "?")
                .setMessage(mContext.getString(R.string.it_has_a_balance_of) + " " +
                        utils.getBalanceOfAccount(selectedAccountAddress))
                .setPositiveButton(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeSelectedAccount();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void shareAddress(String address) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, address);
        sendIntent.setType("text/plain");
        mContext.startActivity(sendIntent);
    }

    @Override
    public int getItemCount() {
        return utils.getNumberOfAccounts();
    }

    // simple methods for register cell state changes
    private void registerToggle(int position) {
        if (unfoldedIndexes.contains(position))
            registerFold(position);
        else
            registerUnfold(position);
    }

    private void registerFold(int position) {
        unfoldedIndexes.remove(position);
    }

    private void registerUnfold(int position) {
        unfoldedIndexes.add(position);
    }

}

package io.github.jangerhard.BitcoinWalletTracker.adapter;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ramotion.foldingcell.FoldingCell;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;
import io.github.jangerhard.BitcoinWalletTracker.DialogMaker;
import io.github.jangerhard.BitcoinWalletTracker.R;
import io.github.jangerhard.BitcoinWalletTracker.utilities.BitcoinUtils;
import io.github.jangerhard.BitcoinWalletTracker.utilities.TrackedWallet;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.MyViewHolder> {

    private HashSet<Integer> unfoldedIndexes = new HashSet<>();

    private Context mContext;
    private int selectedAccountPosition;
    private String selectedAccountAddress;
    private String selectedAccountNickname;
    private BitcoinUtils utils;
    private DialogMaker dialogMaker;

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

    public AccountAdapter(Context mContext, BitcoinUtils utils, DialogMaker dialogMaker) {
        this.mContext = mContext;
        this.utils = utils;
        this.dialogMaker = dialogMaker;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FoldingCell itemView = (FoldingCell) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final TrackedWallet trackedWallet = utils.getTrackedWallets().get(position);


        String nickname = utils.getNickname(trackedWallet.getAddress());
        holder.accNickNameFolded.setText(nickname);


        holder.position = holder.getAdapterPosition();
        holder.qrCode.setImageBitmap(trackedWallet.getRegularQRImage());

        holder.itemView.setOnClickListener(view -> {

            ((FoldingCell) view).toggle(false);
            // register in adapter that state for selected cell is toggled
            registerToggle(holder.position);

        });

        holder.qrCode.setOnClickListener(view -> dialogMaker.showAccountShareDialog(trackedWallet));

        holder.overflow.setOnClickListener(view -> {
            selectedAccountPosition = holder.getAdapterPosition();
            showPopupMenu(holder.overflow);
        });

        holder.overflow2.setOnClickListener(view -> {
            selectedAccountPosition = holder.getAdapterPosition();
            showPopupMenu(holder.overflow);
        });

        // Unfolded
        holder.accAddress.setText(trackedWallet.getAddress());
        holder.accNickNameUnfolded.setText(nickname);

        holder.transactionList.setLayoutManager(
                new LinearLayoutManager(
                        mContext, RecyclerView.VERTICAL, false));

        trackedWallet.getAssosiatedAccount()
                .onEmpty(() -> {
                    holder.accBalance.setText(mContext.getResources().getString(R.string.account_error_balance));
                    holder.accRate.setText(mContext.getResources().getString(R.string.account_error_balance));
                })
                .peek(account -> {

                    holder.accBalance.setText(
                            BitcoinUtils.formatBitcoinBalanceToString(
                                    account.getFinal_balance())
                    );
                    holder.accRate.setText(
                            utils.formatBTCtoCurrency(account.getFinal_balance()));


                    // unfolded
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

                    TransactionAdapter transactionAdapter =
                            new TransactionAdapter(mContext, account.getTxs(), account.getAddress());

                    holder.transactionList.setAdapter(transactionAdapter);
                });
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
                .setInputFilter(R.string.text_input_error_message, text -> {
                    if (text.length() > 30)
                        return false;
                    Pattern p = Pattern.compile("\\w+");
                    Matcher m = p.matcher(text);
                    return m.find();
                })
                .setConfirmButton(android.R.string.ok, text -> {
                    utils.setNewNickname(selectedAccountAddress, text);
                    notifyItemChanged(selectedAccountPosition);
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
                .setPositiveButton(android.R.string.yes, v -> removeSelectedAccount())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
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

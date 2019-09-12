package io.github.jangerhard.BitcoinWalletTracker.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import io.github.jangerhard.BitcoinWalletTracker.Activity.AccountDetailActivity;
import io.github.jangerhard.BitcoinWalletTracker.DialogMaker;
import io.github.jangerhard.BitcoinWalletTracker.R;
import io.github.jangerhard.BitcoinWalletTracker.utilities.BitcoinUtils;
import io.github.jangerhard.BitcoinWalletTracker.utilities.TrackedWallet;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.MyViewHolder> {

    private Context mContext;
    private int selectedAccountPosition;
    private BitcoinUtils utils;
    private DialogMaker dialogMaker;

    class MyViewHolder extends RecyclerView.ViewHolder {

        // Folded
        TextView accNickNameFolded, accBalance, accRate;
        private ImageView overflow2, qrCode;
        int position;

        // Unfolded
//        TextView accAddress, accNickNameUnfolded, tvAccNumTxs, tvAccTotReceived, tvAccFinalBalance,
//                tvRecentTransaction;
//        RecyclerView transactionList;

        MyViewHolder(View view) {
            super(view);

            // Folded
            accNickNameFolded = view.findViewById(R.id.tvAccountNameFolded);
            accBalance = view.findViewById(R.id.tvAccountBalanceFolded);
            accRate = view.findViewById(R.id.tvAccountRateFolded);
            qrCode = view.findViewById(R.id.im_thumbnailFolded);

//            // Unfolded
//            accAddress = view.findViewById(R.id.tv_unfolded_address);
//            accNickNameUnfolded = view.findViewById(R.id.tv_unfolded_nickname);
//            overflow2 = view.findViewById(R.id.im_OverflowFolded2);
//            transactionList = view.findViewById(R.id.transactionList);
//            tvAccNumTxs = view.findViewById(R.id.tv_account_number_transactions);
//            tvAccTotReceived = view.findViewById(R.id.tv_account_total_received);
//            tvAccFinalBalance = view.findViewById(R.id.tv_account_final_balance);
//            tvRecentTransaction = view.findViewById(R.id.tv_unfolded_last_transactions);
        }
    }

    public AccountAdapter(Context mContext, BitcoinUtils utils, DialogMaker dialogMaker) {
        this.mContext = mContext;
        this.utils = utils;
        this.dialogMaker = dialogMaker;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView itemView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final TrackedWallet trackedWallet = utils.getTrackedWallets().get(position);

        holder.accNickNameFolded.setText(trackedWallet.getNickname());

        holder.position = holder.getAdapterPosition();
        holder.qrCode.setImageBitmap(trackedWallet.getRegularQRImage());

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), AccountDetailActivity.class);
            intent.putExtra(AccountDetailActivity.SELECTED_WALLET, trackedWallet);
            view.getContext().startActivity(intent);
        });

        holder.qrCode.setOnClickListener(view -> dialogMaker.showAccountShareDialog(trackedWallet));

        // Unfolded
//        holder.accAddress.setText(trackedWallet.getAddress());
//        holder.accNickNameUnfolded.setText(nickname);
//
//        holder.transactionList.setLayoutManager(
//                new LinearLayoutManager(
//                        mContext, RecyclerView.VERTICAL, false));

        holder.accBalance.setText(trackedWallet.getFormattedBalance());
        holder.accRate.setText(
                utils.formatBTCtoCurrency(trackedWallet.getFinal_balance()));

        // unfolded
//        holder.tvAccNumTxs.setText(
//                String.format(Locale.ENGLISH, "%d", account.getN_tx()));
//        holder.tvAccTotReceived.setText(
//                BitcoinUtils.formatBitcoinBalanceToString(account.getTotal_received()));
//        holder.tvAccFinalBalance.setText(
//                BitcoinUtils.formatBitcoinBalanceToString(account.getFinal_balance()));

//        if (trackedWallet.getTransactions().size() >= 0)
//            holder.tvRecentTransaction.setText(mContext.getResources().getString(R.string.no_activity_on_this_address));
//        else
//            holder.tvRecentTransaction.setText(mContext.getResources().getString(R.string.latest_transactions));
//
//        TransactionAdapter transactionAdapter =
//                new TransactionAdapter(mContext, trackedWallet.getTransactions(), trackedWallet.getAddress());
//
//        holder.transactionList.setAdapter(transactionAdapter);
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view) {
        // inflate menu
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

        MyMenuItemClickListener() {}

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            TrackedWallet wallet = utils.getTrackedWallets().get(selectedAccountPosition);
            String nickname = wallet.getNickname();

            switch (menuItem.getItemId()) {
                case R.id.action_change_nickname:
                    dialogMaker.changeNicknameOnSelected(wallet, nickname, selectedAccountPosition);
                    return true;
                case R.id.action_remove_account:
                    dialogMaker.showRemoveConfirmDialog(wallet, nickname, selectedAccountPosition);
                    return true;
                default:
            }
            return false;
        }
    }

    public void handleRemoveSelectedAccount(int position) {
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return utils.getTrackedWallets().length();
    }

}

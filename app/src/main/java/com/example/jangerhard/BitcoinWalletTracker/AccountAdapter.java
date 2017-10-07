package com.example.jangerhard.BitcoinWalletTracker;

import android.content.Context;
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

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.MyViewHolder> {

    private Context mContext;
    private String selectedAccountAddress;
    private BitcoinUtils utils;

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView accName, accAddress, accBalance;
        private ImageView overflow, qrCode;

        MyViewHolder(View view) {
            super(view);
            accName = (TextView) view.findViewById(R.id.accountName);
            accAddress = (TextView) view.findViewById(R.id.accountAddress);
            accBalance = (TextView) view.findViewById(R.id.accountBalance);
            overflow = (ImageView) view.findViewById(R.id.overflow);
        }
    }

    AccountAdapter(Context mContext, BitcoinUtils utils) {
        this.mContext = mContext;
        this.utils = utils;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        BitcoinAccount account = utils.getAccounts().get(position);
        holder.accName.setText(utils.getNickname(account.getAddress()));
        holder.accBalance.setText(
                BitcoinUtils.formatBitcoinBalanceToString(
                        account.getFinal_balance())
        );
        holder.accAddress.setText(account.getAddress());

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow, String.valueOf(holder.accAddress.getText()));
            }
        });
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view, String address) {
        // inflate menu
        selectedAccountAddress = address;
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
                    utils.setNewNickname(selectedAccountAddress, "Nick" + Math.random());
                    notifyDataSetChanged();
                    return true;
                case R.id.action_remove_account:
                    Toast.makeText(mContext,
                            "Removed account " + selectedAccountAddress,
                            Toast.LENGTH_SHORT).show();
                    utils.removeAccount(selectedAccountAddress);
                    notifyDataSetChanged();
                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return utils.getNumberOfAccounts();
    }
}

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

import java.util.List;

/**
 * Created by jangerhard on 04-Oct-17.
 */

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.MyViewHolder> {

    private Context mContext;
    private List<BitcoinAccount> accountsList;
    private int selectedAccountTag;
    private BitcoinUtils utils;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView accName, accAddress, accBalance;
        private ImageView overflow, qrCode;

        public MyViewHolder(View view) {
            super(view);
            accName = (TextView) view.findViewById(R.id.accountName);
            accAddress = (TextView) view.findViewById(R.id.accountAddress);
            accBalance = (TextView) view.findViewById(R.id.accountBalance);
            overflow = (ImageView) view.findViewById(R.id.overflow);
        }
    }

    public AccountAdapter(Context mContext, List<BitcoinAccount> accountList, BitcoinUtils utils) {
        this.mContext = mContext;
        this.accountsList = accountList;
        this.utils = utils;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        BitcoinAccount account = accountsList.get(position);
        holder.accName.setText(account.getNickName());
        holder.accBalance.setText(
                BitcoinUtils.formatBitcoinBalanceToString(
                        account.getFinal_balance())
        );
        holder.accAddress.setText(account.getAddress());

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow, position);
            }
        });
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view, int position) {
        // inflate menu
        selectedAccountTag = position;
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
                    utils.setNewNickname(selectedAccountTag, "NewNickname");
                    notifyDataSetChanged();
                    return true;
                case R.id.action_remove_account:
                    Toast.makeText(mContext,
                            "Removed account " + accountsList.get(selectedAccountTag).getAddress(),
                            Toast.LENGTH_SHORT).show();
                    utils.getAccounts().remove(selectedAccountTag);
                    notifyDataSetChanged();
                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return accountsList.size();
    }
}

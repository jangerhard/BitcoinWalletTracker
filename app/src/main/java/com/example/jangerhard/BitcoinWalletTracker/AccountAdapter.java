package com.example.jangerhard.BitcoinWalletTracker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by jangerhard on 04-Oct-17.
 */

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.MyViewHolder> {

    private Context mContext;
    private List<BitcoinAccount> accountsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView accName, accBalance;

        public MyViewHolder(View view) {
            super(view);
            accName = (TextView) view.findViewById(R.id.accountName);
            accBalance = (TextView) view.findViewById(R.id.accountBalance);
        }
    }

    public AccountAdapter(Context mContext, List<BitcoinAccount> albumList) {
        this.mContext = mContext;
        this.accountsList = albumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        BitcoinAccount account = accountsList.get(position);
        holder.accName.setText(account.getNickName());
        holder.accBalance.setText(account.getFormatedBalance());
    }

    @Override
    public int getItemCount() {
        return accountsList.size();
    }
}

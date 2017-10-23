package io.github.jangerhard.BitcoinWalletTracker;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> {

    private Context mContext;
    private List<Transaction> transactionList;
    private String address;

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvFromAddress, tvResult;

        MyViewHolder(View view) {
            super(view);
            tvFromAddress = view.findViewById(R.id.transactionAddress);
            tvResult = view.findViewById(R.id.transactionResult);
        }
    }

    TransactionAdapter(Context mContext, List<Transaction> transactionList, String address) {
        this.mContext = mContext;
        this.transactionList = transactionList;
        this.address = address;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        Transaction t = transactionList.get(position);

        BigInteger transactionValue = BitcoinUtils.getTransactionValue(t, address);

        // Received BTC
        if (transactionValue.intValue() > 0)
            holder.tvResult.setTextColor(Color.GREEN);
            // Paid BTC
        else
            holder.tvResult.setTextColor(Color.RED);


        holder.tvResult.setText(
                BitcoinUtils.formatBitcoinBalanceToString(transactionValue)
        );
//        holder.tvResult.setText("Test " + position);
//        holder.tvFromAddress.setText("Test " + position);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }
}

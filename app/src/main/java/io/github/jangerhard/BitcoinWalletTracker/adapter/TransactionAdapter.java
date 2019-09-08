package io.github.jangerhard.BitcoinWalletTracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import io.github.jangerhard.BitcoinWalletTracker.R;
import io.github.jangerhard.BitcoinWalletTracker.model.BlockonomicsTransactionsResponse.Transaction;
import io.github.jangerhard.BitcoinWalletTracker.utilities.BitcoinUtils;
import io.vavr.collection.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> {

    private Context mContext;
    private List<Transaction> transactionList;
    private String address;

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvTimestamp, tvResult;

        MyViewHolder(View view) {
            super(view);
            tvTimestamp = view.findViewById(R.id.transactionTimestamp);
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

        Transaction transaction = transactionList.get(position);

//        long transaction = BitcoinUtils.getTransactionValue(t, address);

        int color = transaction.getValue() < 0
                ? ContextCompat.getColor(mContext, R.color.transaction_paid)
                : ContextCompat.getColor(mContext, R.color.transaction_received);

        holder.tvResult.setTextColor(color);

        holder.tvResult.setText(
                BitcoinUtils.formatBitcoinBalanceToString(transaction.getValue())
        );

        holder.tvTimestamp.setText(BitcoinUtils.getConvertedTimeStamp(transaction.getTime()));
//        holder.tvResult.setText("Test " + position);
//        holder.tvTimestamp.setText("Test " + position);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }
}

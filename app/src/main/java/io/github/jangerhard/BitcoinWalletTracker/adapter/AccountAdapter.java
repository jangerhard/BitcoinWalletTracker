package io.github.jangerhard.BitcoinWalletTracker.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import io.github.jangerhard.BitcoinWalletTracker.Activity.AccountDetailActivity;
import io.github.jangerhard.BitcoinWalletTracker.DialogMaker;
import io.github.jangerhard.BitcoinWalletTracker.R;
import io.github.jangerhard.BitcoinWalletTracker.utilities.BitcoinUtils;
import io.github.jangerhard.BitcoinWalletTracker.utilities.TrackedWallet;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.MyViewHolder> {

    private Activity activity;
    private BitcoinUtils utils;
    private DialogMaker dialogMaker;

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView accNickNameFolded, accBalance, accRate;
        private ImageView qrCode;
        int position;

        MyViewHolder(View view) {
            super(view);

            accNickNameFolded = view.findViewById(R.id.tvAccountNameFolded);
            accBalance = view.findViewById(R.id.tvAccountBalanceFolded);
            accRate = view.findViewById(R.id.tvAccountRateFolded);
            qrCode = view.findViewById(R.id.im_account_details_image);

        }
    }

    public AccountAdapter(Activity activity, BitcoinUtils utils, DialogMaker dialogMaker) {
        this.activity = activity;
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

        holder.accBalance.setText(trackedWallet.getFormattedBalance());
        holder.accRate.setText(
                utils.formatBTCtoCurrency(trackedWallet.getFinal_balance()));

//        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
//                Pair.create(holder.accNickNameFolded, activity.getString(R.string.transition_account_nickname)),
//                Pair.create(holder.accBalance, activity.getString(R.string.transition_account_balance)));

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, AccountDetailActivity.class);
            intent.putExtra(AccountDetailActivity.SELECTED_WALLET, trackedWallet);
            //activity.startActivity(intent, options.toBundle());
            activity.startActivity(intent);
        });

        holder.qrCode.setOnClickListener(view -> dialogMaker.showAccountShareDialog(trackedWallet));
    }

    public void handleRemoveSelectedAccount(int position) {
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return utils.getTrackedWallets().length();
    }

}

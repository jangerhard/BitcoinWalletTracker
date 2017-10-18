package io.github.jangerhard.BitcoinWalletTracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.MyViewHolder> {

    private Context mContext;
    private int selectedAccountPosition;
    private String selectedAccountAddress;
    private String selectedAccountNickname;
    private BitcoinUtils utils;

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView accName, accBalance, accRate;
        private ImageView overflow, qrCode;
        public int position;

        MyViewHolder(View view) {
            super(view);
            accName = view.findViewById(R.id.accountName);
            accBalance = view.findViewById(R.id.accountBalance);
            overflow = view.findViewById(R.id.overflow);
            accRate = view.findViewById(R.id.accountRate);
            qrCode = view.findViewById(R.id.thumbnail);
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
        final BitcoinAccount account = utils.getAccounts().get(position);
        holder.accName.setText(utils.getNickname(account.getAddress()));
        holder.accBalance.setText(
                BitcoinUtils.formatBitcoinBalanceToString(
                        account.getFinal_balance())
        );
        holder.accRate.setText(
                utils.formatPriceToString(
                        account.getFinal_balance()));
        holder.position = holder.getAdapterPosition();
        holder.qrCode.setImageBitmap(
                utils.getQRThumbnail(account.getAddress()));

        holder.qrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupQRCode(account.getAddress());
            }
        });

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow, holder.position);
            }
        });
    }

    private void showPopupQRCode(String address) {

        new MaterialStyledDialog.Builder(mContext)
                .setTitle(address)
//                .setDescription(address)
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(new BitmapDrawable(mContext.getResources(),
                        Bitmap.createScaledBitmap(utils.getQRThumbnail(address),
                                150, 150, true)))
//                .setCustomView()
                .withDialogAnimation(true)
                .setPositiveText("Share")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                    }
                })
//                .setHeaderDrawable()
                //.setHeaderDrawable(ContextCompat.getDrawable(this, R.drawable.heaer))
                .show();
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view, int position) {
        // inflate menu
        selectedAccountPosition = position;
        selectedAccountAddress = utils.getAccounts().get(position).getAddress();
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
        utils.setNewNickname(selectedAccountAddress, "Nick" + Math.random());
        notifyItemChanged(selectedAccountPosition);
    }

    private void showRemoveConfirmDialog() {
        new MaterialDialog.Builder(mContext)
                .title("Stop tracking " + utils.getNickname(selectedAccountAddress))
                .content("Are you sure you want to stop tracking " + selectedAccountNickname + "?" +
                        "\nIt has a balance of " + utils.getBalanceOfAccount(selectedAccountAddress))
                .positiveText(mContext.getString(android.R.string.yes))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        removeSelectedAccount();
                    }
                })
                .negativeText(mContext.getString(android.R.string.cancel))
                .show();
    }

    @Override
    public int getItemCount() {
        return utils.getNumberOfAccounts();
    }
}

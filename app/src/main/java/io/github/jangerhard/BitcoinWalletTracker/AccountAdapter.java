package io.github.jangerhard.BitcoinWalletTracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
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
import com.ramotion.foldingcell.FoldingCell;

import java.util.HashSet;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.MyViewHolder> {

    private HashSet<Integer> unfoldedIndexes = new HashSet<>();
    private View.OnClickListener defaultRequestBtnClickListener;

    private Context mContext;
    private int selectedAccountPosition;
    private String selectedAccountAddress;
    private String selectedAccountNickname;
    private BitcoinUtils utils;

    class MyViewHolder extends RecyclerView.ViewHolder {

        // Folded
        TextView accNickNameFolded, accBalance, accRate;
        private ImageView overflow, qrCode;
        public int position;

        // Unfolded
        TextView accAddress, accNickNameUnfolded;
        RecyclerView transactionList;

        MyViewHolder(View view) {
            super(view);

            // Folded
            accNickNameFolded = view.findViewById(R.id.accountName);
            accBalance = view.findViewById(R.id.accountBalance);
            overflow = view.findViewById(R.id.overflow);
            accRate = view.findViewById(R.id.accountRate);
            qrCode = view.findViewById(R.id.thumbnail);

            // Unfolded
            accAddress = view.findViewById(R.id.tv_unfolded_address);
            accNickNameUnfolded = view.findViewById(R.id.tv_unfolded_nickname);
            transactionList = view.findViewById(R.id.transactionList);
        }
    }

    AccountAdapter(Context mContext, BitcoinUtils utils) {
        this.mContext = mContext;
        this.utils = utils;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FoldingCell itemView = (FoldingCell) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_cell, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final BitcoinAccount account = utils.getAccounts().get(position);

        String nickname = utils.getNickname(account.getAddress());
        holder.accNickNameFolded.setText(nickname);
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
                showPopupMenu(holder.overflow, holder.position);
            }
        });

        // Unfolded
        holder.accAddress.setText(account.getAddress());
        holder.accNickNameUnfolded.setText(nickname);

        holder.transactionList.setLayoutManager(
                new LinearLayoutManager(
                        mContext, LinearLayoutManager.VERTICAL, false));

        TransactionAdapter transactionAdapter =
                new TransactionAdapter(mContext, account.getTxs(), account.getAddress());

        holder.transactionList.setAdapter(transactionAdapter);
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

        new MaterialDialog.Builder(mContext)
                .title("Edit nickname")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("New nickname", "Savings", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        utils.setNewNickname(selectedAccountAddress, input.toString());
                        notifyItemChanged(selectedAccountPosition);
                    }
                }).show();
    }

    private void showRemoveConfirmDialog() {
        new MaterialStyledDialog.Builder(mContext)
                .withDialogAnimation(true)
                .setIcon(R.drawable.ic_warning_white_24dp)
                .setTitle("Stop tracking " + selectedAccountNickname + "?")
                .setDescription("It has a balance of " +
                        utils.getBalanceOfAccount(selectedAccountAddress))
                .setPositiveText(mContext.getString(android.R.string.yes))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        removeSelectedAccount();
                    }
                })
                .setNegativeText(mContext.getString(android.R.string.cancel))
                .show();
    }

    @Override
    public int getItemCount() {
        return utils.getNumberOfAccounts();
    }

    // simple methods for register cell state changes
    public void registerToggle(int position) {
        if (unfoldedIndexes.contains(position))
            registerFold(position);
        else
            registerUnfold(position);
    }

    public void registerFold(int position) {
        unfoldedIndexes.remove(position);
    }

    public void registerUnfold(int position) {
        unfoldedIndexes.add(position);
    }

    public View.OnClickListener getDefaultRequestBtnClickListener() {
        return defaultRequestBtnClickListener;
    }

    public void setDefaultRequestBtnClickListener(View.OnClickListener defaultRequestBtnClickListener) {
        this.defaultRequestBtnClickListener = defaultRequestBtnClickListener;
    }
}

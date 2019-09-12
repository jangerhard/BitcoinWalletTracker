package io.github.jangerhard.BitcoinWalletTracker.Activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jangerhard.BitcoinWalletTracker.R
import io.github.jangerhard.BitcoinWalletTracker.adapter.TransactionAdapter
import io.github.jangerhard.BitcoinWalletTracker.utilities.TrackedWallet

class AccountDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_details)

        val trackedWallet = intent.getParcelableExtra<TrackedWallet>(SELECTED_WALLET)

        if (trackedWallet == null) finish()

        findViewById<ImageView>(R.id.bAccountDetailsGoBack).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.tv_unfolded_address).text = trackedWallet.address
        findViewById<TextView>(R.id.tv_unfolded_nickname).text = trackedWallet.nickname

        findViewById<ImageView>(R.id.im_account_details_image).setImageBitmap(trackedWallet.bigQRImage)
        //findViewById<TextView>(R.id.tv_account_number_transactions)
        //findViewById<TextView>(R.id.tv_account_total_received)
        findViewById<TextView>(R.id.tv_account_final_balance).text = trackedWallet.formattedBalance

        findViewById<TextView>(R.id.tv_unfolded_last_transactions).text =
                if (trackedWallet.transactions.size() >= 0)
                    this.getString(R.string.no_activity_on_this_address)
                else
                    this.getString(R.string.latest_transactions)

        val transactionList = findViewById<RecyclerView>(R.id.transactionList)
        transactionList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        transactionList.adapter = TransactionAdapter(this, trackedWallet.transactions, trackedWallet.address)

//        tvAccNumTxs.setText(
//                String.format(Locale.ENGLISH, "%d", account.getN_tx()));
//        tvAccTotReceived.setText(
//                BitcoinUtils.formatBitcoinBalanceToString(account.getTotal_received()));
//        tvAccFinalBalance.setText(
//                BitcoinUtils.formatBitcoinBalanceToString(account.getFinal_balance()));
//
//        if (trackedWallet.getTransactions().size() >= 0)
//            tvRecentTransaction.setText(mContext.getResources().getString(R.string.no_activity_on_this_address));
//        else
//            tvRecentTransaction.setText(mContext.getResources().getString(R.string.latest_transactions));

    }

    companion object {
        const val SELECTED_WALLET = "IncludedWallet"
    }
}

package io.github.jangerhard.BitcoinWalletTracker.Activity

import android.os.Bundle
import android.transition.Explode
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import io.github.jangerhard.BitcoinWalletTracker.R
import io.github.jangerhard.BitcoinWalletTracker.utilities.TrackedWallet
import io.vavr.control.Option

class AccountDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_details)

        Option.of(intent.getParcelableExtra<TrackedWallet>(SELECTED_WALLET))
                .map {

                    findViewById<TextView>(R.id.tv_unfolded_address).text = it.address
                    findViewById<TextView>(R.id.tv_unfolded_nickname).text = it.nickname

                    findViewById<ImageView>(R.id.im_account_details_image).setImageBitmap(it.bigQRImage)
                    //findViewById<TextView>(R.id.tv_account_number_transactions)
                    //findViewById<TextView>(R.id.tv_account_total_received)
                    findViewById<TextView>(R.id.tv_account_final_balance).text = it.formattedBalance
                    //findViewById<TextView>(R.id.tv_unfolded_last_transactions)
                }

        //val transactionList = findViewById<RecyclerView>(R.id.transactionList)
    }

    companion object {
        const val SELECTED_WALLET = "IncludedWallet"
    }
}

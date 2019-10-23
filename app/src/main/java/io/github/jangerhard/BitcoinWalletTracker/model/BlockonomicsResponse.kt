package io.github.jangerhard.BitcoinWalletTracker.model

import io.vavr.collection.List

data class BlockonomicsTransactionsResponse(
        val history: List<Transaction> = List.empty()
) {
    data class Transaction(
            val time: Long,
            val addr: List<String>,
            val value: Long,
            val txid: String
    )
}

data class BlockonomicsBalanceResponse(
        val response: List<AddressInfo> = List.empty()
) {
    data class AddressInfo(
            val confirmed: Long,
            val addr: String,
            val unconfirmed: Long
    )

    override fun toString(): String {
        return "BlockonomicsBalanceResponse(response=$response)"
    }
}

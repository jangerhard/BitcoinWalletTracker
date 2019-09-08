package io.github.jangerhard.BitcoinWalletTracker.model

import io.vavr.collection.List

data class BlockinfoResponse(
        val address: String,
        val n_tx: Long,
        val total_received: Long,
        val total_sent: Long,
        val final_balance: Long,
        val txs: List<Transaction> = List.empty()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BlockinfoResponse

        if (address != other.address) return false
        if (n_tx != other.n_tx) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + n_tx.hashCode()
        return result
    }

    override fun toString(): String {
        return "BlockinfoResponse(address='$address', n_tx=$n_tx, total_received=$total_received, total_sent=$total_sent, final_balance=$final_balance, txs=$txs)"
    }

    data class Transaction(
            val result: Long,
            val out: List<TransactionOut> = List.empty(),
            val inputs: List<TransactionInput> = List.empty(),
            val time: Long
    ) {

        data class TransactionOut(
                val spent: Boolean,
                val value: Long,
                val addr: String
        )

        data class TransactionInput(
                val prevOut: TransactionOut?
        )


        override fun toString(): String {
            return "Transaction(result=$result, out=$out, inputs=$inputs, time=$time)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Transaction

            if (result != other.result) return false
            if (out != other.out) return false
            if (inputs != other.inputs) return false
            if (time != other.time) return false

            return true
        }

        override fun hashCode(): Int {
            var result1 = result.hashCode()
            result1 = 31 * result1 + out.hashCode()
            result1 = 31 * result1 + inputs.hashCode()
            result1 = 31 * result1 + time.hashCode()
            return result1
        }

    }

}

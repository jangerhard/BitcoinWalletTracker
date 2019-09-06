package io.github.jangerhard.BitcoinWalletTracker.model

import io.vavr.collection.List

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

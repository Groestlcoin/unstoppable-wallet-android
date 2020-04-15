package io.horizontalsystems.bankwallet.modules.fulltransactioninfo.providers

import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.collections.ArrayList

class ChainzResponse(
        @SerializedName("hash") override val hash: String,
        @SerializedName("block") override val height: Int,
        @SerializedName("timestamp") val time: Long,
        @SerializedName("confirmations") val confirmationsInt: Int,
        @SerializedName("fees") val fees: Double,
        @SerializedName("inputs") val vin: ArrayList<Vin>,
        @SerializedName("outputs") val vout: ArrayList<Vout>,
        @SerializedName("total_input") val total_input: Double,
        @SerializedName("total_output") val total_output: Double)
    : BitcoinResponse() {

    override val date: Date get() = Date(time * 1000)
    override val inputs: ArrayList<Input> get() = vin as ArrayList<Input>
    override val outputs: ArrayList<Output> get() = vout as ArrayList<Output>
    override val feePerByte: Double? get() = null
    override val confirmations: String get() = confirmationsInt.toString()
    override val fee: Double get() = (total_input - total_output)
    override val size: Int get() = 0

    class Vin(@SerializedName("amount") override val value: Double,
              @SerializedName("addr") override val address: String) : Input() {
    }

    class Vout(@SerializedName("amount") override val value: Double,
               @SerializedName("script")  val scriptPubKey: String,
               @SerializedName("addr") override val address: String) : Output() {
    }
}
package io.horizontalsystems.bankwallet.modules.fulltransactioninfo.providers

import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.collections.ArrayList

/*
Transaction format
txid
version
locktime
size
weight
fee
vin[]
    txid
    vout
    is_coinbase
    scriptsig
    scriptsig_asm
    inner_redeemscript_asm
    inner_witnessscript_asm
    sequence
    witness[]
    prevout (previous output in the same format as in vout below)
    (Elements only)
    is_pegin
    issuance (available for asset issuance transactions, null otherwise)
    asset_id
    is_reissuance
    asset_id
    asset_blinding_nonce
    asset_entropy
    contract_hash
    assetamount or assetamountcommitment
    tokenamount or tokenamountcommitment
vout[]
    scriptpubkey
    scriptpubkey_asm
    scriptpubkey_type
    scriptpubkey_address
    value
    (Elements only)
    valuecommitment
    asset or assetcommitment
    pegout (available for peg-out outputs, null otherwise)
    genesis_hash
    scriptpubkey
    scriptpubkey_asm
    scriptpubkey_address
status
confirmed (boolean)
block_height (available for confirmed transactions, null otherwise)
block_hash (available for confirmed transactions, null otherwise)
block_time (available for confirmed transactions, null otherwise)
 */

class EsploraResponse(
        @SerializedName("txid") override val hash: String,
        @SerializedName("version") val version: Int,
        @SerializedName("locktime") val locktime: Int,
        @SerializedName("size") override val size: Int,
        @SerializedName("weight") val weight: Int,
        @SerializedName("fee") val feeString: String,
        @SerializedName("vin") val vin: ArrayList<Vin>,
        @SerializedName("vout") val vout: ArrayList<Vout>,
        @SerializedName("status") val status: Status
)

        : BitcoinResponse() {

    override val height: Int get() = status.block_height
    override val date: Date get() = Date(status.block_time * 1000)
    override val inputs: ArrayList<Input> get() = vin as ArrayList<Input>
    override val outputs: ArrayList<Output> get() = vout as ArrayList<Output>
    override val feePerByte: Double? get() = feeString.toDouble() / size
    override val confirmations: String get() = if(status.confirmed) "1 or more" else "0"
    override val fee: Double get() = feeString.toDouble() / btcRate

    class Vin(@SerializedName("txid") val txid: String,
              @SerializedName("vout") val n: Int,
              @SerializedName("prevout") val prevout: Prevout,
              @SerializedName("scriptsig") val scriptSig: String) : Input() {
        override val value: Double get() = (prevout.value.toDouble() / btcRate)
        override val address: String get() = prevout.address
    }

    class Prevout(@SerializedName("scriptpubkey_address") val address: String,
                  @SerializedName("value") val value: Int)

    class Vout(@SerializedName("value") val valueString: String,
               @SerializedName("scriptpubkey")  val scriptPubKey: String,
               @SerializedName("scriptpubkey_address") override val address: String) : Output() {
        override val value: Double get() = (valueString.toDouble() / btcRate)
    }

    class Status(@SerializedName("confirmed") val confirmed: Boolean,
                 @SerializedName("block_height") val block_height: Int,
                 @SerializedName("block_hash") val block_hash: String,
                 @SerializedName("block_time") val block_time: Long)
}
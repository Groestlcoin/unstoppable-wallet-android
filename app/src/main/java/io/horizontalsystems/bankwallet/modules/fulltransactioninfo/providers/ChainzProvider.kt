package io.horizontalsystems.bankwallet.modules.fulltransactioninfo.providers

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.horizontalsystems.bankwallet.modules.fulltransactioninfo.FullTransactionInfoModule
import io.horizontalsystems.bankwallet.modules.fulltransactioninfo.FullTransactionInfoModule.Request.GetRequest


class GroestlcoinChainzProvider(val testMode: Boolean) : FullTransactionInfoModule.BitcoinForksProvider {
    override val name: String = "chainz.cryptoid.info/grs"
    private val baseApiUrl = "https://chainz.cryptoid.info/grs${if (testMode) "-test" else ""}"
    override val pingUrl = "$baseApiUrl/api.dws?key=d47da926b82e&q=txinfo&t=nodes"

    override fun url(hash: String): String {
        return "$baseApiUrl/tx.dws?$hash"
    }

    override fun apiRequest(hash: String): FullTransactionInfoModule.Request {
        val url = "$baseApiUrl/api.dws?key=d47da926b82e&q=txinfo&t=$hash"
        return GetRequest(url)
    }

    override fun convert(json: JsonObject): BitcoinResponse {
        return Gson().fromJson(json, ChainzResponse::class.java)
    }
}

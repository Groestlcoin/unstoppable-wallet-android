package io.horizontalsystems.bankwallet.modules.fulltransactioninfo.providers

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.horizontalsystems.bankwallet.modules.fulltransactioninfo.FullTransactionInfoModule
import io.horizontalsystems.bankwallet.modules.fulltransactioninfo.FullTransactionInfoModule.Request.GetRequest


class GroestlcoinEsploraProvider(val testMode: Boolean) : FullTransactionInfoModule.BitcoinForksProvider {
    override val name: String = "esplora.groestlcoin.org"
    private val baseApiUrl = "${if (testMode) "https://esplora-test" else "https://esplora"}.groestlcoin.org"
    override val pingUrl = "$baseApiUrl/api/blocks/tip/height"

    override fun url(hash: String): String {
        return "$baseApiUrl/tx/$hash"
    }

    override fun apiRequest(hash: String): FullTransactionInfoModule.Request {
        val url = "$baseApiUrl/api/tx/$hash"
        return GetRequest(url)
    }

    override fun convert(json: JsonObject): BitcoinResponse {
        return Gson().fromJson(json, EsploraResponse::class.java)
    }
}

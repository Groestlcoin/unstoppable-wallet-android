package io.horizontalsystems.bankwallet.modules.fulltransactioninfo.providers

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.horizontalsystems.bankwallet.modules.fulltransactioninfo.FullTransactionInfoModule
import io.horizontalsystems.bankwallet.modules.fulltransactioninfo.FullTransactionInfoModule.Request.GetRequest


class GroestlcoinBlockbookProvider(val testMode: Boolean) : FullTransactionInfoModule.BitcoinForksProvider {
    override val name: String = "blockbook.groestlcoin.org"
    private val baseApiUrl = "${if (testMode) "https://blockbook-test" else "https://blockbook"}.groestlcoin.org"
    override val pingUrl = "$baseApiUrl/api"

    override fun url(hash: String): String {
        return "$baseApiUrl/tx/$hash"
    }

    override fun apiRequest(hash: String): FullTransactionInfoModule.Request {
        val url = "$baseApiUrl/api/v2/tx/$hash"
        return GetRequest(url)
    }

    override fun convert(json: JsonObject): BitcoinResponse {
        return Gson().fromJson(json, BlockbookResponse::class.java)
    }
}

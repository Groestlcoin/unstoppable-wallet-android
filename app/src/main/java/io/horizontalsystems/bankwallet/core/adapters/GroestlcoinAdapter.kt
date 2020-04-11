package io.horizontalsystems.bankwallet.core.adapters

import io.horizontalsystems.bankwallet.core.*
import io.horizontalsystems.bankwallet.entities.AccountType
import io.horizontalsystems.bankwallet.entities.SyncMode
import io.horizontalsystems.bankwallet.entities.TransactionRecord
import io.horizontalsystems.bankwallet.entities.*
import io.horizontalsystems.bitcoincore.BitcoinCore
import io.horizontalsystems.bitcoincore.core.Bip
import io.horizontalsystems.bitcoincore.models.BalanceInfo
import io.horizontalsystems.bitcoincore.models.BlockInfo
import io.horizontalsystems.core.helpers.DateHelper
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.groestlcoinkit.GroestlcoinKit
import io.horizontalsystems.groestlcoinkit.GroestlcoinKit.NetworkType
import io.reactivex.Single
import java.math.BigDecimal
import java.util.*

class GroestlcoinAdapter(override val kit: GroestlcoinKit) :
        BitcoinBaseAdapter(kit), GroestlcoinKit.Listener, ISendGroestlcoinAdapter {

    constructor(wallet: Wallet, testMode: Boolean) :
            this(createKit(wallet, testMode))

    init {
        kit.listener = this
    }

    //
    // BitcoinBaseAdapter
    //

    override val satoshisInBitcoin: BigDecimal = BigDecimal.valueOf(Math.pow(10.0, decimal.toDouble()))

    //
    // GroestlcoinKit Listener
    //

    override fun onBalanceUpdate(balance: BalanceInfo) {
        balanceUpdatedSubject.onNext(Unit)
    }

    override fun onLastBlockInfoUpdate(blockInfo: BlockInfo) {
        lastBlockUpdatedSubject.onNext(Unit)
    }

    override fun onKitStateUpdate(state: BitcoinCore.KitState) {
        when (state) {
            is BitcoinCore.KitState.Synced -> {
                if (this.state !is AdapterState.Synced) {
                    this.state = AdapterState.Synced
                }
            }
            is BitcoinCore.KitState.NotSynced -> {
                if (this.state !is AdapterState.NotSynced) {
                    this.state = AdapterState.NotSynced
                }
            }
            is BitcoinCore.KitState.Syncing -> {
                this.state.let { currentState ->
                    val newProgress = (state.progress * 100).toInt()
                    val newDate = kit.lastBlockInfo?.timestamp?.let { Date(it * 1000) }

                    if (currentState is AdapterState.Syncing && currentState.progress == newProgress) {
                        val currentDate = currentState.lastBlockDate
                        if (newDate != null && currentDate != null && DateHelper.isSameDay(newDate, currentDate)) {
                            return
                        }
                    }

                    this.state = AdapterState.Syncing(newProgress, newDate)
                }
            }
        }
    }

    override fun onTransactionsUpdate(inserted: List<TransactionInfo>, updated: List<TransactionInfo>) {
        val records = mutableListOf<TransactionRecord>()

        for (info in inserted) {
            records.add(transactionRecord(info))
        }

        for (info in updated) {
            records.add(transactionRecord(info))
        }

        transactionRecordsSubject.onNext(records)
    }

    override fun onTransactionsDelete(hashes: List<String>) {
        // ignored for now
    }

    override fun getTransactions(from: TransactionRecord?, limit: Int): Single<List<TransactionRecord>> {
        return kit.transactions(from?.uid, limit).map { it.map { tx -> transactionRecord(tx) } }
    }

    // ISendGroestlAdapter

    override fun availableBalance(address: String?): BigDecimal {
        return availableBalance(feeRate, address, mapOf())
    }

    override fun fee(amount: BigDecimal, address: String?): BigDecimal {
        return fee(amount, feeRate, address, mapOf())
    }

    override fun validate(address: String) {
        validate(address, mapOf())
    }

    override fun send(amount: BigDecimal, address: String): Single<Unit> {
        return send(amount, address, feeRate, mapOf())
    }

    companion object {

        private const val feeRate = 20L

        private fun getNetworkType(testMode: Boolean) =
                if (testMode) NetworkType.TestNet else NetworkType.MainNet

        private fun getBip(derivation: AccountType.Derivation?): Bip = when (derivation) {
            AccountType.Derivation.bip49 -> Bip.BIP49
            AccountType.Derivation.bip84 -> Bip.BIP84
            else -> Bip.BIP44
        }

        private fun createKit(wallet: Wallet, testMode: Boolean): GroestlcoinKit {
            val account = wallet.account
            val accountType = account.type
            val walletDerivation = wallet.settings[CoinSetting.Derivation]?.let { AccountType.Derivation.valueOf(it) }
            val syncMode = wallet.settings[CoinSetting.SyncMode]?.let { SyncMode.valueOf(it) }
            if (accountType is AccountType.Mnemonic && accountType.words.size == 12) {
                return GroestlcoinKit(context = App.instance,
                        words = accountType.words,
                        walletId = account.id,
                        syncMode = getSyncMode(syncMode),
                        networkType = getNetworkType(testMode),
                        confirmationsThreshold = defaultConfirmationsThreshold,
                        bip = getBip(walletDerivation))
            }

            throw UnsupportedAccountException()
        }

        fun clear(walletId: String, testMode: Boolean) {
            GroestlcoinKit.clear(App.instance, getNetworkType(testMode), walletId)
        }
    }
}

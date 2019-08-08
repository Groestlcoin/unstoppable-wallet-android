package io.horizontalsystems.bankwallet.modules.send.sendviews.fee

import io.horizontalsystems.bankwallet.core.FeeRatePriority
import io.horizontalsystems.bankwallet.entities.CoinValue
import io.horizontalsystems.bankwallet.entities.Currency
import io.horizontalsystems.bankwallet.entities.CurrencyValue
import io.horizontalsystems.bankwallet.entities.Rate
import io.horizontalsystems.bankwallet.modules.send.SendModule
import java.math.BigDecimal


class SendFeePresenter(
        private val interactor: SendFeeModule.IInteractor,
        private val helper: SendFeePresenterHelper,
        override val coinCode: String,
        private val feeCoinCode: String,
        private val currency: Currency,
        override val baseCoinName: String,
        override val tokenProtocol: String)
    : SendFeeModule.IViewDelegate, SendFeeModule.IInteractorDelegate {

    var view: SendFeeViewModel? = null
    private var rate: Rate? = null
    private var fee: BigDecimal = BigDecimal.ZERO
    private var inputType = SendModule.InputType.COIN
    private var feePriority: FeeRatePriority = FeeRatePriority.MEDIUM
    private var insufficientFeeBalance: BigDecimal? = null
    private var feeRate = 0L

    override val validState: Boolean
        get() {
            return insufficientFeeBalance == null
        }

    override fun onViewDidLoad() {
        interactor.getRate(feeCoinCode, currency.code)
        feeRate = interactor.getFeeRate(feePriority)
    }

    override fun onRateFetched(latestRate: Rate?) {
        rate = latestRate
        updateView()
    }

    override fun onFeeSliderChange(progress: Int) {
        feePriority = FeeRatePriority.valueOf(progress)
        feeRate = interactor.getFeeRate(feePriority)
        view?.onFeePriorityChange(feePriority)
    }

    override fun onInputTypeUpdated(inputType: SendModule.InputType) {
        this.inputType = inputType
        updateView()
    }

    override fun getFeeRate(): Long {
        return feeRate
    }

    override fun getFeeCoinValue(): CoinValue {
        return CoinValue(feeCoinCode, fee)
    }

    override fun getFeeCurrencyValue(): CurrencyValue? {
        val currencyAmount = rate?.let { fee.times(it.value) }
        return currencyAmount?.let { CurrencyValue(currency, it) }
    }

    override fun onFeeUpdated(fee: BigDecimal?) {
        this.fee = fee ?: BigDecimal.ZERO
        updateView()
    }

    override fun onInsufficientFeeBalanceError(fee: BigDecimal) {
        insufficientFeeBalance = fee
        view?.setInsufficientFeeBalanceError(CoinValue(feeCoinCode, fee))
    }

    private fun updateView(){
        val reversedInputType = if (inputType == SendModule.InputType.COIN) SendModule.InputType.CURRENCY else SendModule.InputType.COIN

        view?.setPrimaryFee(helper.feeAmount(fee, inputType, rate))
        view?.setSecondaryFee(helper.feeAmount(fee, reversedInputType, rate))
    }
}
package com.mvvm.mybinance.viewmodel

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.*
import com.mvvm.mybinance.model.Coin
import com.mvvm.mybinance.repository.CoinRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val TAG: String = "fhrm"

    var returnJson: String? = null
    private var mCoinRepository: CoinRepository
    private var mCoinList: LiveData<MutableList<Coin>>
    var errorMessage:MutableLiveData<String> = MutableLiveData()
    var symbol: String = ""
    var quantity: String = "0"
    var avgPrice: String = "0"
    var isBTC: MutableLiveData<Boolean>
    var additional_quantity: String = "0"
    var additional_avgPrice: String = "0"

    init {
        isBTC = MutableLiveData(false)
        mCoinRepository = CoinRepository(application)
        mCoinList = mCoinRepository.getAll()
    }

    fun getAll(): LiveData<MutableList<Coin>> = mCoinList

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            mCoinRepository.deleteAll()
        }
    }

    fun insert() {
        viewModelScope.launch(Dispatchers.IO) {

            var _symbol = symbol.toUpperCase()
            var _quantity = quantity.toDouble()
            var _avgPrice = avgPrice.toDouble()
            var _purchaseAmount = (quantity.toDouble() * avgPrice.toDouble())
            var _pair = if (isBTC.value!!) "BTC" else "USDT"
            var lastPrice = try {
                getListPriceFromUrl(_symbol, _pair)
            } catch (e:Exception){
                return@launch
            }

            var _percent =
                String.format("%.2f", ((lastPrice.toDouble() / avgPrice.toDouble()) - 1) * 100)
                    .toDouble()
            var _profit = _purchaseAmount * (_percent / 100)

            mCoinRepository.insert(
                Coin(_symbol, _quantity, _avgPrice, _purchaseAmount, _pair, _percent, _profit)
            )
        }
    }

    fun delete(position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            mCoinRepository.delete(mCoinList.value?.get(position)!!)
        }
    }


    fun update(position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            var coin = mCoinList!!.value!!.get(position)
            var _quantity = coin.quantity + additional_quantity.toDouble()
            var _avgPrice =
                ((coin.avgPrice * coin.quantity) + (additional_avgPrice.toDouble() * additional_quantity.toDouble())) / (coin.quantity + additional_quantity.toDouble())
            var _purchaseAmount = _quantity * _avgPrice
            var lastPrice = getListPriceFromUrl(coin.symbol, coin.pair!!)
            var _percent =
                String.format("%.2f", ((lastPrice.toDouble() / _avgPrice) - 1) * 100).toDouble()
            var _profit = _purchaseAmount * (_percent / 100)

            _quantity = doubleToSatoshi(_quantity)
            _avgPrice = doubleToSatoshi(_avgPrice)
            _purchaseAmount = doubleToSatoshi(_purchaseAmount)

            mCoinList.value?.get(position)?.quantity = _quantity
            mCoinList.value?.get(position)?.avgPrice = _avgPrice
            mCoinList.value?.get(position)?.purchaseAmount = _purchaseAmount
            mCoinList.value?.get(position)?.percent = _percent
            mCoinList.value?.get(position)?.profit = _profit

            mCoinRepository.update(mCoinList.value!![position])
        }

    }


    fun doubleToSatoshi(input: Double): Double {
        return String.format("%.8f", input).toDouble()
    }

    fun refreshProfit() {
        mCoinList.value?.forEach {
            viewModelScope.launch(Dispatchers.IO) {
                var lastPrice = getListPriceFromUrl(it.symbol, it.pair!!)
                var percent =
                    String.format("%.2f", ((lastPrice.toDouble() / it.avgPrice) - 1) * 100)
                        .toDouble()
                var profit = it.purchaseAmount * (percent / 100)

                it.percent = percent
                it.profit = profit
            }
        }
    }

    fun log() {
        getPrice("BTC", "USDT")
        var a = JSONObject(returnJson).getString("lastPrice")
        Log.d(TAG, "MainViewModel -log(),    : ${a}")
    }

    fun clear() {
        symbol = ""
        quantity = "0"
        avgPrice = "0"
        additional_quantity = "0"
        additional_avgPrice = "0"
    }

    fun getPrice(symbol: String?, pair: String) {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }

    @Throws(Throwable::class)
    suspend fun getListPriceFromUrl(symbol: String, pair: String): String {
        val huc =
            URL("https://api.binance.com/api/v1/ticker/24hr?symbol=${symbol?.toUpperCase()}${pair.toUpperCase()}").openConnection() as HttpURLConnection
        huc.requestMethod = "GET"
        huc.addRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0"
        )
        huc.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01")
        huc.connect()
        var `in`: InputStream? = null
        if (huc.responseCode !== 200) {
            `in` = huc.errorStream
        } else {
            `in` = huc.inputStream
        }
        val br = BufferedReader(InputStreamReader(`in`, "UTF-8"))
        var line: String?
        val sb = StringBuilder()
        line = br.readLine()
        while (line != null) {
            sb.append(line)
            line = br.readLine()
        }
        br.close()

        return JSONObject(sb.toString().trimIndent()).getString("lastPrice")
    }


    class Factory(val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(application) as T
        }
    }
}
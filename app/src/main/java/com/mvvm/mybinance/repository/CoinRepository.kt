package com.mvvm.mybinance.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mvvm.mybinance.model.Coin
import com.mvvm.mybinance.model.CoinDao
import com.mvvm.mybinance.model.CoinDatabase

class CoinRepository (application: Application){

    val TAG: String = "fhrm"
    private var mCoinDatabase:CoinDatabase
    private var mCoinDao:CoinDao
    private var mCoinList: LiveData<MutableList<Coin>>

    init {
        mCoinDatabase = CoinDatabase.getInstance(application)
        mCoinDao = mCoinDatabase.coinDao()
        mCoinList = mCoinDao.getAll()
    }

    fun getAll():LiveData<MutableList<Coin>> = mCoinList

    suspend fun deleteAll(){
        mCoinDao.deleteAll()
    }

    suspend fun insert(coin:Coin){
        mCoinDao.insert(coin)
    }

    suspend fun delete(coin:Coin){
        mCoinDao.delete(coin)
    }

    suspend fun update(coin:Coin){
        mCoinDao.update(coin)
    }

}
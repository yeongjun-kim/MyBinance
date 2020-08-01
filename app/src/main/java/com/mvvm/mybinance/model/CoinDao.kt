package com.mvvm.mybinance.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Dao
interface CoinDao {

    @Query("SELECT * FROM Coin ORDER BY symbol")
    fun getAll():LiveData<MutableList<Coin>>

    @Query("DELETE FROM Coin")
    fun deleteAll()

    @Insert
    fun insert(coin:Coin)

    @Delete
    fun delete(coin:Coin)

    @Update
    fun update(coin:Coin)
}


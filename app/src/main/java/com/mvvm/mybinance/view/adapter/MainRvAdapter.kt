package com.mvvm.mybinance.view.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mvvm.mybinance.R
import com.mvvm.mybinance.databinding.ItemMainRvBinding
import com.mvvm.mybinance.model.Coin
import java.text.DecimalFormat

class MainRvAdapter : RecyclerView.Adapter<MainRvAdapter.CustomViewHolder>() {

    var coinList: MutableList<Coin> = mutableListOf()
    var listener:ClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_main_rv,
                parent,
                false
            ), listener
        )
    }

    override fun getItemCount(): Int = coinList.size

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(coinList[position])
    }

    fun setList(inputList: MutableList<Coin>) {
        val diffResult = DiffUtil.calculateDiff(MainRvDiffCallback(coinList, inputList))
        coinList = inputList
        diffResult.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }


    interface ClickListener{
        fun onShortClick(position:Int)
        fun onLongClick(position:Int)
    }

    class CustomViewHolder(val binding: ItemMainRvBinding, val listener: ClickListener?) : RecyclerView.ViewHolder(binding.root ) {
        init {
            itemView.setOnClickListener { listener?.onShortClick(adapterPosition)}
            itemView.setOnLongClickListener {
                listener?.onLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
        }

        fun bind(item: Coin) {
            val color = if(item.percent>0) "#C14040" else "#387DF0"
            binding.apply {
                itemMainRvTvPair.setTextColor(Color.parseColor(color))
                itemMainRvTvProfitPercent.setTextColor(Color.parseColor(color))
                itemMainRvTvProfitPrice.setTextColor(Color.parseColor(color))
                coin = item
                avgPrice = doubleToSatoshi(item.avgPrice)
                purchaseAmount = doubleToSatoshi(item.purchaseAmount)
                profit = doubleToSatoshi(item.profit)
            }
        }

        fun doubleToSatoshi(inputDouble:Double):String{
            val df = DecimalFormat("#.########")
            return df.format(inputDouble).toString()
        }
    }
}
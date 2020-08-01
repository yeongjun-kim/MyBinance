package com.mvvm.mybinance.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.mvvm.mybinance.R
import com.mvvm.mybinance.databinding.ActivityMainBinding
import com.mvvm.mybinance.databinding.DialogAddBinding
import com.mvvm.mybinance.databinding.DialogAdditionalPurchaseBinding
import com.mvvm.mybinance.view.adapter.MainRvAdapter
import com.mvvm.mybinance.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_add.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    val TAG: String = "fhrm"
    var num = 0
    lateinit var mViewModel: MainViewModel
    lateinit var mBinding: ActivityMainBinding
    lateinit var mAdapter: MainRvAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.mvvm.mybinance.R.layout.activity_main)

        initBinding()
        initViewModel()
        initRecyclerView()
        initCoroutine()

    }

    private fun initCoroutine() {
        GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                delay(1000L)
                mAdapter.notifyDataSetChanged()

            }
        }
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                delay(1000L)
                mViewModel.refreshProfit()

            }
        }

    }

    private fun initRecyclerView() {
        mAdapter = MainRvAdapter().apply {
            listener = object : MainRvAdapter.ClickListener {
                override fun onShortClick(position: Int) {
                    openAdditionalPurchase(position)
                }

                override fun onLongClick(position: Int) {
                    openDeleteDialog(position)
                }

            }
        }
        main_rv.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }
    }


    private fun initBinding() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.apply {
            lifecycleOwner = this@MainActivity
            activity = this@MainActivity
        }
    }

    private fun initViewModel() {
        mViewModel = ViewModelProvider(
            this,
            MainViewModel.Factory(application)
        ).get(MainViewModel::class.java)
        mViewModel.getAll().observe(this, Observer {
            mAdapter.setList(it)
        })
        mViewModel.errorMessage.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
    }

    fun openAddDialog() {
        mViewModel.clear()
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add, null, false)
        val binding = DialogAddBinding.bind(view)
        binding.viewModel = mViewModel
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setTitle("추가하기  (Binance에 존재하는 코인)")
            .setPositiveButton("확인") { _, _ ->
                mViewModel.insert()
            }
            .setNegativeButton("취소", null)
            .create()
        dialog.show()
    }

    fun openDeleteAllDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_delete, null, false)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("확인") { _, _ ->
                mViewModel.deleteAll()
            }
            .setNegativeButton("취소", null)
            .create()
        dialog.show()
    }

    fun openDeleteDialog(position: Int) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_delete, null, false)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("확인") { _, _ ->
                mViewModel.delete(position)
            }
            .setNegativeButton("취소", null)
            .create()
        dialog.show()
    }

    fun openAdditionalPurchase(position: Int) {
        mViewModel.clear()
        val view =
            LayoutInflater.from(this).inflate(R.layout.dialog_additional_purchase, null, false)
        val binding = DialogAdditionalPurchaseBinding.bind(view)
        binding.viewModel = mViewModel
        val dialog = AlertDialog.Builder(this)
            .setTitle("추매")
            .setView(view)
            .setPositiveButton("확인") { _, _ ->
                mViewModel.update(position)
            }
            .setNegativeButton("취소", null)
            .create()
        dialog.show()
    }
}


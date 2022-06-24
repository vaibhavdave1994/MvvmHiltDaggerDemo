package com.example.mvvmhiltdaggerdemo.main.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demoapp.other.Status
import com.example.mvvmhiltdaggerdemo.databinding.ActivityMainBinding
import com.example.mvvmhiltdaggerdemo.main.view.adapter.EmployeeAdapter
import com.example.mvvmhiltdaggerdemo.main.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var mAdapter: EmployeeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpUI()
        setUpObservers()
    }


    private fun setUpUI() {
        mAdapter = EmployeeAdapter()
        binding.rvEmployees.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = mAdapter
            addItemDecoration(DividerItemDecoration(this@MainActivity, LinearLayout.VERTICAL))
        }
    }

    private fun setUpObservers() {
        mainViewModel.res.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.progress.visibility = View.GONE
                    binding.rvEmployees.visibility = View.VISIBLE
                    it.data.let { res ->
                        if (res?.status == "success") {
                            res.data?.let { it1 -> mAdapter.submitList(it1) }
                        } else {
                            Snackbar.make(binding.rootView, "Status = false", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
                Status.LOADING -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.rvEmployees.visibility = View.GONE
                }
                Status.ERROR -> {
                    binding.progress.visibility = View.GONE
                    binding.rvEmployees.visibility = View.VISIBLE
                    Snackbar.make(binding.rootView, "Something went wrong", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }
}
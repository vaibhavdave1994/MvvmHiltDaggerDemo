package com.example.mvvmhiltdaggerdemo.repository

import com.example.mvvmhiltdaggerdemo.api.ApiService


class Repository(private val apiService: ApiService) {
    suspend fun getEmployee() = apiService.getEmployees()

}
package com.example.mvvmhiltdaggerdemo.util

import android.util.Log

object LogUtil {
    var isEnableLogs = true
    fun printLog(tag: String?, `object`: Any?) {
        if (isEnableLogs && `object` != null) {
            Log.d(tag, "" + `object`)
        }
    }

    fun printLog(tag: String?, `object`: String?) {
        if (isEnableLogs && `object` != null) {
            Log.d(tag, "" + `object`)
        }
    }

    fun printLog(tag: String?, `object`: String?, tr: Throwable?) {
        if (isEnableLogs && `object` != null) {
            printLog(tag, "" + `object`)
        }
    }
}

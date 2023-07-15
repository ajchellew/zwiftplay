package com.che.zwiftplayhost.utils

import android.util.Log
import java.lang.Exception

object Logger : BaseObservable<Logger.LogCallback>() {

    interface LogCallback {
        fun newLogLine(line: String)
    }

    fun d(tag: String, line: String) {
        Log.d(tag, line)
        callback(line)
    }

    fun i(tag: String, line: String) {
        Log.i(tag, line)
        callback(line)
    }

    fun e(tag: String, line: String) {
        Log.e(tag, line)
        callback(line)
    }

    fun e(tag: String, line: String, exception: Exception) {
        Log.e(tag, line, exception)
        callback(line)
    }

    private fun callback(line: String) {
        for (listener in listeners) {
            listener.newLogLine(line)
        }
    }
}
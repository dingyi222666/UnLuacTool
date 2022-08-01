package com.dingyi.unluactool.core.util

import android.util.Log
import java.util.*


class Logger
private constructor(private val name: String) {
    fun d(msg: String) {
        Log.d(name, msg)
    }

    fun i(msg: String) {
        Log.i(name, msg)
    }

    fun v(msg: String) {
        Log.v(name, msg)
    }

    fun w(msg: String) {
        Log.w(name, msg)
    }

    fun w(msg: String, e: Throwable) {
        Log.w(name, msg, e)
    }

    fun e(msg: String) {
        Log.e(name, msg)
    }

    fun e(msg: String, e: Throwable) {
        Log.e(name, msg, e)
    }

    companion object {
        private val map = WeakHashMap<String, Logger>()

        @Synchronized
        fun getLogger(name: String): Logger {
            var logger = map[name]
            if (logger == null) {
                logger = Logger(name)
                map[name] = logger
            }
            return logger
        }
    }
}
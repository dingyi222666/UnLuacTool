package com.dingyi.unluactool.ktx

import com.dingyi.unluactool.MainApplication
import com.google.gson.Gson

inline fun <reified T> String.decodeToBean(): T {
    return Gson().fromJson(this, T::class.java)
}


object Paths {
    val projectDir =
        lazy(LazyThreadSafetyMode.NONE) { MainApplication.instance.getExternalFilesDir(null)?.resolve("projects")?.absolutePath ?: "" }
}
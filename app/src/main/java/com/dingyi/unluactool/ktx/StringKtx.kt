package com.dingyi.unluactool.ktx

import com.dingyi.unluactool.MainApplication
import com.google.gson.Gson
import java.io.File

inline fun <reified T> String.decodeToBean(): T {
    return Gson().fromJson(this, T::class.java)
}


fun Any.encodeToJson(): String {
    return Gson().toJson(this)
}

fun String.toFile(): File = File(this)

object Paths {
    val projectDir =
        lazy(LazyThreadSafetyMode.NONE) {
            MainApplication.instance.getExternalFilesDir(null)?.resolve("projects")?.absolutePath
                ?: ""
        }
}
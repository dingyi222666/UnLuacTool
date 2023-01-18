package com.dingyi.unluactool.common.ktx

import com.dingyi.unluactool.MainApplication
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

internal val gson = GsonBuilder()
    .create()

internal inline fun <reified T> String.decodeToBean(): T {
    return gson.fromJson(this, T::class.java)
}

internal fun Any.encodeToJson(): String {
    return gson.toJson(this)
}

fun String.toFile(): File = File(this)

object Paths {
    val projectDir =
        lazy(LazyThreadSafetyMode.NONE) {
            MainApplication.instance.getExternalFilesDir(null)?.resolve("projects")?.absolutePath
                ?: ""
        }
}
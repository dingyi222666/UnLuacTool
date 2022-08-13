package com.dingyi.unluactool.common.ktx

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.dingyi.unluactool.MainApplication

fun getString(resId: Int): String = MainApplication.instance.getString(resId)

fun getString(resId: Int, vararg formatArgs: Any): String =
    MainApplication.instance.getString(resId, *formatArgs)


inline fun <reified T> Activity.startActivity(block: Intent.() -> Unit = {}) {
    startActivity(Intent(this, getJavaClass<T>()).apply(block))
}

inline fun Activity.startActivity(targetClass: Class<*>, block: Intent.() -> Unit = {}) {
    startActivity(Intent(this, targetClass).apply(block))
}


inline fun <reified T> Fragment.startActivity(block: Intent.() -> Unit = {}) {
    startActivity(Intent(requireContext(), getJavaClass<T>()).apply(block))
}

inline fun Fragment.startActivity(targetClass: Class<*>, block: Intent.() -> Unit = {}) {
    startActivity(Intent(requireContext(), targetClass).apply(block))
}

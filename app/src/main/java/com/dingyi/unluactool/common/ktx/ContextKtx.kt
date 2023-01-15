package com.dingyi.unluactool.common.ktx

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import androidx.fragment.app.Fragment
import com.dingyi.unluactool.MainApplication

fun getString(resId: Int): String = MainApplication.instance.getString(resId)

fun getString(resId: Int, vararg formatArgs: Any): String =
    MainApplication.instance.getString(resId, *formatArgs)

fun Context.getAttributeColor(resId: Int): Int {
    val typedArray = obtainStyledAttributes(intArrayOf(resId))
    val color = typedArray.getColor(0, 0)
    typedArray.recycle()
    return color
}

fun Fragment.getAttributeColor(resId: Int): Int {
    return requireContext().getAttributeColor(resId)
}

inline fun <reified T> Activity.startActivity(block: Intent.() -> Unit = {}) {
    startActivity(Intent(this, getJavaClass<T>()).apply(block))
}

inline fun Activity.startActivity(targetClass: Class<*>, block: Intent.() -> Unit = {}) {
    startActivity(Intent(this, targetClass).apply(block))
}


fun Activity.getStatusBarHeight(): Int {
    val rectangle = Rect()
    window.decorView.getWindowVisibleDisplayFrame(rectangle)
    return rectangle.top
}

inline fun <reified T> Fragment.startActivity(block: Intent.() -> Unit = {}) {
    startActivity(Intent(requireContext(), getJavaClass<T>()).apply(block))
}

inline fun Fragment.startActivity(targetClass: Class<*>, block: Intent.() -> Unit = {}) {
    startActivity(Intent(requireContext(), targetClass).apply(block))
}

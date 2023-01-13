package com.dingyi.unluactool.common.ktx

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.dingyi.unluactool.MainApplication
import com.google.android.material.snackbar.Snackbar
import kotlin.math.roundToInt

fun Context.getAttributeColor(resId: Int): Int {
    val typedArray = obtainStyledAttributes(intArrayOf(resId))
    val color = typedArray.getColor(0, 0)
    typedArray.recycle()
    return color
}

fun String.showSnackBar(view: View) =
    Snackbar.make(view, this, Snackbar.LENGTH_LONG)
        .apply {
            animationMode = Snackbar.ANIMATION_MODE_SLIDE
        }.show()

@SuppressLint("InternalInsetResource", "DiscouragedApi")
fun getStatusBarHeight(): Int {
    var height = 16.dp
    val resources = MainApplication.instance.resources
    val resourceId = resources
        .getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        height = resources.getDimensionPixelSize(resourceId)
    }
    return height
}

inline val Int.dp: Int
    get() = (MainApplication.instance.resources.displayMetrics.density * this).roundToInt()
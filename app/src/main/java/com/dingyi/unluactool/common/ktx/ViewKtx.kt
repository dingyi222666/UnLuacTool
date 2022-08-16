package com.dingyi.unluactool.common.ktx

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

inline val Int.dp: Int
    get() = (MainApplication.instance.resources.displayMetrics.density * this).roundToInt()
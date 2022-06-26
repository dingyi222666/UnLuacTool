package com.dingyi.unluactool.ktx

import android.content.Context

inline fun Context.getAttributeColor(resId: Int): Int {
    val typedArray = obtainStyledAttributes(intArrayOf(resId))
    val color = typedArray.getColor(0, 0)
    typedArray.recycle()
    return color
}

package com.dingyi.unluactool.common.ktx

import android.graphics.Color
import androidx.annotation.FloatRange


fun Color.setAlpha(
    @FloatRange(from = 0.0, to = 1.0)
    range: Float
): Color {

    val argbColor = toArgb()
    val oldAlpha = (alpha() * 255).toInt()

    return Color.valueOf(
        argbColor - oldAlpha + ((range * 255).toInt().shl(24))
    )
}


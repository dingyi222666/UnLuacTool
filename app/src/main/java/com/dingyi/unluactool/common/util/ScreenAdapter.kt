package com.dingyi.unluactool.common.util

import android.content.res.Configuration
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.ktx.dp

/**
 * @author dingyi
 * @date 2022.8.16
 * @see <a href="https://github.com/Jesse205/Aide-Lua/blob/master/Jesse205Library/src/main/luaLibs/com/Jesse205/util/ScreenFixUtil.lua">this</a>
 */
class ScreenAdapter {

    enum class DeviceType {
        PAD, DEFAULT
    }

    enum class Orientation(private val targetValue: Int) {
        LANDSCAPE(Configuration.ORIENTATION_LANDSCAPE),
        PORTRAIT(Configuration.ORIENTATION_PORTRAIT);

        companion object {
            fun valueOf(targetValue: Int): Orientation {

                return when (targetValue) {
                    LANDSCAPE.targetValue -> LANDSCAPE
                    PORTRAIT.targetValue -> PORTRAIT
                    else -> PORTRAIT
                }
            }
        }

    }

    var currentDeviceType = DeviceType.DEFAULT
        private set

    var currentOrientation = Orientation.LANDSCAPE
        private set

    var currentScreenWidth = 0
        private set

    private var currentSmallestWidth = 0


    val smallestWidth =
        MainApplication.instance.resources.getDimension(R.dimen.smallestWidth)

    fun decodeConfiguration(configuration: Configuration): Boolean {

        var configurationChangedFlag = false

        val smallestWidthForDevice = configuration.smallestScreenWidthDp.dp

        val currentOrientation = Orientation.valueOf(configuration.orientation)

        val currentScreenWidth = configuration.screenWidthDp.dp

        if (smallestWidthForDevice != currentSmallestWidth) {

            currentDeviceType = if (smallestWidthForDevice >= smallestWidth) {
                DeviceType.PAD
            } else {
                DeviceType.DEFAULT
            }

            configurationChangedFlag = true

            currentSmallestWidth = smallestWidthForDevice
        }


        if (currentOrientation != this.currentOrientation) {
            this.currentOrientation = currentOrientation
            configurationChangedFlag = true
        }


        if (currentScreenWidth != this.currentScreenWidth) {
            this.currentScreenWidth = currentScreenWidth
            configurationChangedFlag = true
        }

        return configurationChangedFlag

    }

}
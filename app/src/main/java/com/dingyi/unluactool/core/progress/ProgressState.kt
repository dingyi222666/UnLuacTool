package com.dingyi.unluactool.core.progress

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.dingyi.unluactool.MainApplication

class ProgressState : MutableLiveData<Pair<String, Int>>() {


    private val mainHandler = Handler(Looper.getMainLooper())
    var text = "loading..."
        set(value) {

            setValue(value to progress)

            field = value
        }

    var progress = 0
        set(value) {
            if (value < 0 || value > 100) {
                error("progress状态超出边界")
            }
            setValue(text to value)
            field = value
        }

    override fun setValue(value: Pair<String, Int>?) {
        mainHandler.post {
            super.setValue(value)
        }
    }


}
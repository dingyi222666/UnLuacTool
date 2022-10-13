package com.dingyi.unluactool.core.progress

import androidx.lifecycle.MutableLiveData

class ProgressState : MutableLiveData<Pair<String, Int>>() {


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




}
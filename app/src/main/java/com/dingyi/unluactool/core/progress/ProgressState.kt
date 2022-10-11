package com.dingyi.unluactool.core.progress

class ProgressState {


    var text = "loading..."

    var progress = 0
        set(value) {
            if (value < 0 || value > 100) {
                error("progress状态超出边界")
            }
            field = value
        }
}
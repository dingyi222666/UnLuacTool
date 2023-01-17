package com.dingyi.unluactool.ui.editor.event

import android.view.Menu
import com.dingyi.unluactool.core.event.EventType
import com.dingyi.unluactool.ui.editor.EditorFragmentData

interface MenuListener {
    fun onReload(menu: Menu, currentFragmentData: EditorFragmentData)

    companion object {
        val menuListenerEventType = EventType.create<MenuListener>()
    }
}


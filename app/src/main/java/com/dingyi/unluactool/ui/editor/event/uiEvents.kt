package com.dingyi.unluactool.ui.editor.event

import android.view.Menu
import androidx.appcompat.widget.Toolbar
import com.dingyi.unluactool.core.event.EventType
import com.dingyi.unluactool.ui.editor.EditorFragmentData

interface MenuListener {
    fun onReload(toolbar: Toolbar, currentFragmentData: EditorFragmentData)

    companion object {
        val menuListenerEventType = EventType.create<MenuListener>()
    }
}


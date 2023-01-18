package com.dingyi.unluactool.ui.editor.event

import androidx.appcompat.widget.Toolbar
import com.dingyi.unluactool.core.event.EventType
import com.dingyi.unluactool.ui.editor.fileTab.OpenedFileTabData


interface MenuListener {
    fun onReload(toolbar: Toolbar, currentFragmentData: OpenedFileTabData)

    companion object {
        val menuListenerEventType = EventType.create<MenuListener>()
    }
}


package com.dingyi.unluactool.ui.editor.event

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.dingyi.unluactool.core.event.EventType
import com.dingyi.unluactool.ui.editor.fileTab.OpenedFileTabData


interface MenuListener {
    fun onReloadMenu(toolbar: Toolbar, currentFragmentData: OpenedFileTabData)

    companion object {
        val menuListenerEventType = EventType.create<MenuListener>()
    }
}

fun interface MenuEvent {
    fun onClick(menuItem: MenuItem)
    companion object {
        val eventType = EventType.create<MenuEvent>()
    }
}

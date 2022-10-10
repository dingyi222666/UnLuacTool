package com.dingyi.unluactool

import android.app.Application
import com.dingyi.unluactool.core.event.EventServiceRegistry
import com.dingyi.unluactool.core.project.ProjectServiceRegistry
import com.dingyi.unluactool.core.service.ServiceRegistry
import com.dingyi.unluactool.core.service.ServiceRegistryBuilder
import kotlinx.coroutines.CoroutineScope
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.impl.StandardFileSystemManager

class MainApplication : Application() {


    lateinit var globalServiceRegistry: ServiceRegistry
        private set

    lateinit var fileSystemManager: StandardFileSystemManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        globalServiceRegistry = ServiceRegistryBuilder
            .builder()
            .provider(ProjectServiceRegistry())
            .provider(EventServiceRegistry())
            .displayName("global service")
            .build()

        fileSystemManager = StandardFileSystemManager()

        //need call init method
        fileSystemManager.init()

        VFS.setManager(fileSystemManager)


        CrashHandler.init(this)

    }


    companion object {
        lateinit var instance: MainApplication
            private set
    }
}
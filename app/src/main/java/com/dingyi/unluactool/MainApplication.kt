package com.dingyi.unluactool

import android.app.Application
import com.dingyi.unluactool.core.service.ServiceRegistry
import com.dingyi.unluactool.core.service.ServiceRegistryBuilder
import com.dingyi.unluactool.engine.filesystem.UnLuaCVirtualFileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.impl.StandardFileSystemManager

class MainApplication : Application() {

    lateinit var globalServiceRegistry: ServiceRegistry
        private set

    lateinit var fileSystemManager: StandardFileSystemManager
        private set

    // 不需要取消这个作用域，因为它会随着进程结束而结束
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this

        globalServiceRegistry = ServiceRegistryBuilder
            .builder()
            .displayName("global service")
            .build()

        fileSystemManager = StandardFileSystemManager()

        // need call init method
        fileSystemManager.init()
        fileSystemManager.addProvider("unluac", UnLuaCVirtualFileProvider())
        VFS.setManager(fileSystemManager)

        CrashHandler.init(this)

    }


    companion object {
        lateinit var instance: MainApplication
            private set
    }
}
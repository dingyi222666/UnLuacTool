package com.dingyi.unluactool

import android.app.Application
import com.dingyi.unluactool.core.service.ServiceRegistry
import com.dingyi.unluactool.core.service.ServiceRegistryBuilder

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        globalServiceRegistry = ServiceRegistryBuilder
            .builder()
            .displayName("global service")
            .build()

    }

    companion object {
        lateinit var instance: MainApplication
            private set

        lateinit var globalServiceRegistry: ServiceRegistry
            private set
    }
}
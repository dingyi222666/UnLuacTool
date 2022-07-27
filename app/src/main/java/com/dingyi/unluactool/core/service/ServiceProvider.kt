package com.dingyi.unluactool.core.service

import androidx.annotation.Nullable
import java.lang.reflect.Type

interface ServiceProvider {

    /**
     * Locates a service instance of the given type. Returns null if this provider does not provide a service of this type.
     */
    @Nullable
    fun getService(serviceType: Class<*>): Service?


    fun getAll(): Iterator<Service>

    companion object EmptyServiceProvider : ServiceProvider {
        override fun getService(serviceType: Class<*>): Service? {
            return null
        }

        override fun getAll(): Iterator<Service> = EmptyIterator

    }

    object EmptyIterator : Iterator<Service> {
        override fun hasNext(): Boolean {
            return false
        }

        override fun next(): Service {
            return object : Service {
                override fun get(): Any {
                    TODO("Not yet implemented")
                }

                override fun getDisplayName(): String {
                    TODO("Not yet implemented")
                }

            }
        }

    }
}
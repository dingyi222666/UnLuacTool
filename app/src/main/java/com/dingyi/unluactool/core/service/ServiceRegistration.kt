package com.dingyi.unluactool.core.service


interface ServiceRegistration {
    /**
     * Adds a service to this registry.
     * @param serviceType The type to make this service visible as.
     * @param serviceInstance The service implementation.
     */
    fun <T> add(serviceType: Class<T>, serviceInstance: T)

    /**
     * Adds a service to this registry. The implementation class should have a single public constructor, and this constructor can take services to be injected as parameters.
     *
     * @param serviceType The service implementation to make visible.
     */
    fun add(serviceType: Class<*>)

    /**
     * Adds a service provider bean to this registry. This provider may define factory and decorator methods. See [DefaultServiceRegistry] for details.
     */
    fun addProvider(provider: ServiceProvider)
}
package com.dingyi.unluactool

import com.dingyi.unluactool.core.service.ServiceRegistryBuilder
import com.dingyi.unluactool.core.service.get
import org.junit.Test
import org.junit.Assert.*
import java.time.Year


class ServiceTest {

    @Test
    fun testService() {

        val serviceRegistry = ServiceRegistryBuilder.builder()
            .displayName("test")
            .provider(this)
            .build()

        val testObj = serviceRegistry.get<Test2>()
        assertEquals(testObj.age, 56)
        testObj.age = 45
        assertEquals(testObj.age, 45)
        val test2Obj = serviceRegistry.get<Test3>()
        assertEquals(test2Obj.year, Year.now().value - testObj.age)
    }

    protected fun createTest(): Test2 {
        return Test2()
    }

    protected fun createTest2(test2: Test2): Test3 {
        return Test3().apply {
            year = Year.now().value - test2.age
        }
    }

    class Test2 {
        var age = 56
    }

    class Test3 {
        var year = 0
    }

}
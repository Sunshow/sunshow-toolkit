package net.sunshow.toolkit.core.qbean.sample.bean

import kotlin.test.Test

class KspQBeanTest {

    @Test
    fun testQBean() {
        println(QKtFooBar.id)
    }

    @Test
    fun testQBeanCreator() {
        val creator = KtFooBarCreator.builder()
            .withFoo("test")
            .withBar(1)
            .build()
        println(creator.getCreateProperties())
    }

    @Test
    fun testQBeanUpdater() {
        val updater = KtFooBarUpdater.builder(123)
            .withFoo("updated")
            .withBar(2)
            .build()
        println(updater.getUpdateProperties())
        println(updater.getUpdateId())
    }


}
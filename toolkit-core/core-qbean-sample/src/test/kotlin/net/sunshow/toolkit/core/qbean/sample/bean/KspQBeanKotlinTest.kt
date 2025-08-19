package net.sunshow.toolkit.core.qbean.sample.bean

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class KspQBeanKotlinTest {
    
    @Test
    fun `test QBean object constants`() {
        // 测试生成的常量
        assertEquals("foo", QKtFooBar.foo)
        assertEquals("bar", QKtFooBar.bar)
        assertEquals("id", QKtFooBar.id)
    }
    
    @Test
    fun `test Creator with DSL style`() {
        // 使用 DSL 风格创建
        val creator = QKtFooBar.create {
            foo = "test"
            bar = 123
        }
        
        assertNotNull(creator)
        assertEquals("test", creator.getFoo())
        assertEquals(123, creator.getBar())
        assertTrue(creator.getCreateProperties().contains("foo"))
        assertTrue(creator.getCreateProperties().contains("bar"))
    }
    
    @Test
    fun `test Creator with builder style`() {
        // 使用传统 Builder 风格（兼容 Java）
        val creator = KtFooBarCreator.builder()
            .withFoo("builder test")
            .withBar(456)
            .build()
        
        assertEquals("builder test", creator.getFoo())
        assertEquals(456, creator.getBar())
    }
    
    @Test
    fun `test Creator with mixed style`() {
        // 混合使用 Builder 和 DSL 风格
        val creator = KtFooBarCreator.builder()
            .withFoo("mixed")
            .apply {
                bar = 789  // 使用属性风格
            }
            .build()
        
        assertEquals("mixed", creator.getFoo())
        assertEquals(789, creator.getBar())
    }
    
    @Test
    fun `test Updater with DSL style`() {
        // 使用 DSL 风格更新
        val updater = QKtFooBar.update(100) {
            foo = "updated"
            bar = 200
        }
        
        assertNotNull(updater)
        assertEquals(100, updater.getUpdateId())
        assertEquals("updated", updater.getFoo())
        assertEquals(200, updater.getBar())
        assertTrue(updater.getUpdateProperties().contains("foo"))
        assertTrue(updater.getUpdateProperties().contains("bar"))
    }
    
    @Test
    fun `test Updater with builder style`() {
        // 使用传统 Builder 风格
        val updater = KtFooBarUpdater.builder(101)
            .withFoo("builder update")
            .withBar(301)
            .build()
        
        assertEquals(101, updater.getUpdateId())
        assertEquals("builder update", updater.getFoo())
        assertEquals(301, updater.getBar())
    }
    
    @Test
    fun `test Updater partial update`() {
        // 测试部分更新
        val updater = QKtFooBar.update(102) {
            foo = "only foo"
            // 不更新 bar
        }
        
        assertEquals(102, updater.getUpdateId())
        assertEquals("only foo", updater.getFoo())
        assertNull(updater.getBar())
        assertTrue(updater.getUpdateProperties().contains("foo"))
        assertFalse(updater.getUpdateProperties().contains("bar"))
    }
    
    @Test
    fun `test property setter with null value`() {
        // 测试 null 值处理 - null 值也应该被标记
        val creator = QKtFooBar.create {
            foo = "test"
            bar = null  // 应该被标记
        }
        
        assertEquals("test", creator.getFoo())
        assertNull(creator.getBar())
        assertTrue(creator.getCreateProperties().contains("foo"))
        assertTrue(creator.getCreateProperties().contains("bar"))  // null 值也应该被标记
    }
    
    @Test
    fun `test updater property setter with null value`() {
        // 测试 Updater null 值处理 - null 值也应该被标记
        val updater = QKtFooBar.update(103) {
            foo = null  // 显式设置为 null
            bar = 999
        }
        
        assertEquals(103, updater.getUpdateId())
        assertNull(updater.getFoo())
        assertEquals(999, updater.getBar())
        assertTrue(updater.getUpdateProperties().contains("foo"))  // null 值也应该被标记
        assertTrue(updater.getUpdateProperties().contains("bar"))
    }
}
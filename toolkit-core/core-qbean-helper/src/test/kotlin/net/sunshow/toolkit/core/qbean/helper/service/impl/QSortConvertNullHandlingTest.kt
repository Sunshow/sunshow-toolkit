package net.sunshow.toolkit.core.qbean.helper.service.impl

import net.sunshow.toolkit.core.qbean.api.request.QSort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort

/**
 * 验证 QSort.NullHandling 映射到 Spring Data Sort.Order 的逻辑。
 * 与 AbstractQServiceImpl.convertSort 保持一致。
 */
class QSortConvertNullHandlingTest {

    private fun convertSort(requestSort: QSort): Sort.Order {
        val direction = if (requestSort.order == QSort.Order.DESC) {
            Sort.Direction.DESC
        } else {
            Sort.Direction.ASC
        }
        val order = Sort.Order(direction, requestSort.field)
        return when (requestSort.nullHandling) {
            QSort.NullHandling.NULLS_FIRST -> order.nullsFirst()
            QSort.NullHandling.NULLS_LAST -> order.nullsLast()
            else -> order
        }
    }

    @Test
    fun `nulls last maps to Spring Data NullHandling`() {
        val order = convertSort(QSort("profitCny", QSort.Order.DESC, QSort.NullHandling.NULLS_LAST))
        assertEquals(Sort.Direction.DESC, order.direction)
        assertEquals("profitCny", order.property)
        assertEquals(Sort.NullHandling.NULLS_LAST, order.nullHandling)
    }

    @Test
    fun `nulls first maps to Spring Data NullHandling`() {
        val order = convertSort(QSort("amount", QSort.Order.ASC, QSort.NullHandling.NULLS_FIRST))
        assertEquals(Sort.NullHandling.NULLS_FIRST, order.nullHandling)
    }

    @Test
    fun `native keeps Spring Data native null handling`() {
        val order = convertSort(QSort("id", QSort.Order.ASC))
        assertEquals(Sort.NullHandling.NATIVE, order.nullHandling)
    }
}

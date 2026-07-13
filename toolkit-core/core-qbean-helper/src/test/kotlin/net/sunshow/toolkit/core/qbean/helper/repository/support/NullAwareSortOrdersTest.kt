package net.sunshow.toolkit.core.qbean.helper.repository.support

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort

class NullAwareSortOrdersTest {

    @Test
    fun `requiresNullAwareOrdering is false for native or unsorted`() {
        assertFalse(NullAwareSortOrders.requiresNullAwareOrdering(Sort.unsorted()))
        assertFalse(
            NullAwareSortOrders.requiresNullAwareOrdering(
                Sort.by(Sort.Order.desc("profitCny"))
            )
        )
    }

    @Test
    fun `requiresNullAwareOrdering is true for nulls last or first`() {
        assertTrue(
            NullAwareSortOrders.requiresNullAwareOrdering(
                Sort.by(Sort.Order.desc("profitCny").nullsLast())
            )
        )
        assertTrue(
            NullAwareSortOrders.requiresNullAwareOrdering(
                Sort.by(Sort.Order.asc("amount").nullsFirst())
            )
        )
    }

    @Test
    fun `mixed sort still requires null aware path`() {
        assertTrue(
            NullAwareSortOrders.requiresNullAwareOrdering(
                Sort.by(
                    Sort.Order.asc("id"),
                    Sort.Order.desc("profitCny").nullsLast()
                )
            )
        )
    }
}

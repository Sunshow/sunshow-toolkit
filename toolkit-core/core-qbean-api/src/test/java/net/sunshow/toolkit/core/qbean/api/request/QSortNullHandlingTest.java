package net.sunshow.toolkit.core.qbean.api.request;

import net.sunshow.toolkit.core.qbean.api.search.FieldSort;
import net.sunshow.toolkit.core.qbean.api.search.PageSearch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class QSortNullHandlingTest {

    @Test
    void defaultNullHandlingIsNative() {
        QSort sort = new QSort("profitCny", QSort.Order.DESC);
        Assertions.assertEquals(QSort.NullHandling.NATIVE, sort.getNullHandling());
    }

    @Test
    void qPageAddOrderWithNullHandling() {
        QPage page = QPage.newInstance()
                .addOrder("profitCny", QSort.Order.DESC, QSort.NullHandling.NULLS_LAST);

        Assertions.assertEquals(1, page.getSortList().size());
        QSort sort = page.getSortList().get(0);
        Assertions.assertEquals("profitCny", sort.getField());
        Assertions.assertEquals(QSort.Order.DESC, sort.getOrder());
        Assertions.assertEquals(QSort.NullHandling.NULLS_LAST, sort.getNullHandling());
    }

    @Test
    void qPageAddOrderAcceptsFullQSort() {
        QSort input = new QSort("amount", QSort.Order.ASC, QSort.NullHandling.NULLS_FIRST);
        QPage page = QPage.newInstance().addOrder(input);

        Assertions.assertSame(input, page.getSortList().get(0));
        Assertions.assertEquals(QSort.NullHandling.NULLS_FIRST, page.getSortList().get(0).getNullHandling());
    }

    @Test
    void pageSearchPropagatesNullHandling() {
        FieldSort fieldSort = new FieldSort();
        fieldSort.setField("profitCny");
        fieldSort.setDirection(QSort.Order.DESC);
        fieldSort.setNullHandling(QSort.NullHandling.NULLS_LAST);

        PageSearch search = new PageSearch();
        search.setSorts(Collections.singletonList(fieldSort));

        QPage page = search.toQPage();
        QSort sort = page.getSortList().get(0);
        Assertions.assertEquals("profitCny", sort.getField());
        Assertions.assertEquals(QSort.Order.DESC, sort.getOrder());
        Assertions.assertEquals(QSort.NullHandling.NULLS_LAST, sort.getNullHandling());
    }
}

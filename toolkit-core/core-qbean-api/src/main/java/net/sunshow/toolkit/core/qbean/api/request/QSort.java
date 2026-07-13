package net.sunshow.toolkit.core.qbean.api.request;

import java.io.Serializable;

public class QSort implements Serializable {
    private String field;
    private Order order;
    /**
     * NULL 排序优先级，默认 NATIVE 交由数据库处理，保持兼容。
     */
    private NullHandling nullHandling = NullHandling.NATIVE;

    public QSort() {
    }

    public QSort(String field, Order order) {
        this.field = field;
        this.order = order;
    }

    public QSort(String field, Order order, NullHandling nullHandling) {
        this.field = field;
        this.order = order;
        this.nullHandling = nullHandling != null ? nullHandling : NullHandling.NATIVE;
    }

    public String getField() {
        return field;
    }

    public Order getOrder() {
        return order;
    }

    public NullHandling getNullHandling() {
        return nullHandling;
    }

    public void setNullHandling(NullHandling nullHandling) {
        this.nullHandling = nullHandling != null ? nullHandling : NullHandling.NATIVE;
    }

    public enum Order {
        ASC,
        DESC
    }

    public enum NullHandling {
        /**
         * 使用数据库默认 NULL 排序行为
         */
        NATIVE,
        NULLS_FIRST,
        NULLS_LAST
    }
}

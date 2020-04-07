package net.sunshow.toolkit.core.qbean.request;

import java.io.Serializable;

public class QSort implements Serializable {
    private String field;
    private Order order;

    public QSort() {
    }

    public QSort(String field, Order order) {
        this.field = field;
        this.order = order;
    }

    public String getField() {
        return field;
    }

    public Order getOrder() {
        return order;
    }

    public enum Order {
        ASC,
        DESC
    }
}

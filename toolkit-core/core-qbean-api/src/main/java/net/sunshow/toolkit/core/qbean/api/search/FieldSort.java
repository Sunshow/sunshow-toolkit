package net.sunshow.toolkit.core.qbean.api.search;

import net.sunshow.toolkit.core.qbean.api.request.QSort;

public class FieldSort {

    private String field;

    private QSort.Order direction;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public QSort.Order getDirection() {
        return direction;
    }

    public void setDirection(QSort.Order direction) {
        this.direction = direction;
    }

}

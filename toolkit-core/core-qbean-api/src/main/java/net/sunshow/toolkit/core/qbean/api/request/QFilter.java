package net.sunshow.toolkit.core.qbean.api.request;


import net.sunshow.toolkit.core.qbean.api.enums.Operator;

import java.io.Serializable;
import java.util.List;

/**
 * 请求过滤条件
 * Created by sunshow on 5/19/15.
 */
public class QFilter implements Serializable {

    private String field;
    private Object value;
    private Operator operator;
    private List<Object> valueList;

    public QFilter() {
    }

    public QFilter(Operator operator, String field) {
        this.field = field;
        this.operator = operator;
    }

    public QFilter(Operator operator, String field, Object value) {
        this.field = field;
        this.value = value;
        this.operator = operator;
    }

    public QFilter(Operator operator, String field, List<Object> valueList) {
        this.field = field;
        this.valueList = valueList;
        this.operator = operator;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public List<Object> getValueList() {
        return valueList;
    }

    public void setValueList(List<Object> valueList) {
        this.valueList = valueList;
    }
}

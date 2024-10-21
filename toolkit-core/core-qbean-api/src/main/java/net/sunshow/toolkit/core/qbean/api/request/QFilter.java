package net.sunshow.toolkit.core.qbean.api.request;


import net.sunshow.toolkit.core.qbean.api.enums.Operator;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

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

    public static QFilter equal(String field, Object value) {
        return new QFilter(Operator.EQUAL, field, value);
    }

    public static QFilter lessEqual(String field, Comparable<?> value) {
        return new QFilter(Operator.LESS_EQUAL, field, value);
    }

    public static QFilter greaterEqual(String field, Comparable<?> value) {
        return new QFilter(Operator.GREATER_EQUAL, field, value);
    }

    public static QFilter lessThan(String field, Comparable<?> value) {
        return new QFilter(Operator.LESS_THAN, field, value);
    }

    public static QFilter greaterThan(String field, Comparable<?> value) {
        return new QFilter(Operator.GREATER_THAN, field, value);
    }

    public static QFilter between(String field, @NotNull Comparable<?> low, @NotNull Comparable<?> high) {
        return new QFilter(Operator.BETWEEN, field, Arrays.asList(low, high));
    }

    public static QFilter in(String field, Iterable<Object> valueList) {
        return new QFilter(Operator.IN, field, Arrays.asList(StreamSupport.stream(valueList.spliterator(), false).toArray(Object[]::new)));
    }

    public static QFilter notIn(String field, Iterable<Object> valueList) {
        return new QFilter(Operator.NOT_IN, field, Arrays.asList(StreamSupport.stream(valueList.spliterator(), false).toArray(Object[]::new)));
    }

    public static QFilter like(String field, String value) {
        return new QFilter(Operator.LIKE, field, value);
    }

    public static QFilter like(String field, String value, char escapeChar) {
        return new QFilter(Operator.LIKE_ESCAPE, field, Arrays.asList(value, escapeChar));
    }

    public static QFilter notNull(String field) {
        return new QFilter(Operator.NOT_NULL, field);
    }

    public static QFilter isNull(String field) {
        return new QFilter(Operator.IS_NULL, field);
    }

    public static QFilter notEqual(String field, Object value) {
        return new QFilter(Operator.NOT_EQUAL, field, value);
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

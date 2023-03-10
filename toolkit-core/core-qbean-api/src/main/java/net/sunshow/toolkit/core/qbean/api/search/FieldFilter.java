package net.sunshow.toolkit.core.qbean.api.search;

import net.sunshow.toolkit.core.qbean.api.enums.Operator;

public class FieldFilter {

    private String field;

    private Object[] values;

    private Operator operator;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

}

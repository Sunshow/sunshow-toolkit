package net.sunshow.toolkit.core.qbean.api.search;

import net.sunshow.toolkit.core.qbean.api.enums.Operator;
import net.sunshow.toolkit.core.qbean.api.request.QFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FieldFilter {

    private String field;

    private Object[] values;

    private Operator operator;

    public QFilter toQFilter() {
        if (Operator.isZero(operator)) {
            return new QFilter(operator, field);
        }
        if (Operator.isUnary(operator)) {
            return new QFilter(operator, field, values[0]);
        }
        if (Operator.isBinary(operator) || Operator.isCollection(operator)) {
            return new QFilter(operator, field, Arrays.asList(values));
        }
        if (Operator.isLogical(operator)) {
            List<QFilter> filterList = new ArrayList<>();
            for (Object value : values) {
                filterList.add(((FieldFilter) value).toQFilter());
            }
            return new QFilter(operator, null, filterList);
        }
        QFilter qFilter = new QFilter();
        qFilter.setField(field);
        qFilter.setOperator(operator);
        qFilter.setValue(values);
        return qFilter;
    }

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

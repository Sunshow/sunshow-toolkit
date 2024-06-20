package net.sunshow.toolkit.core.qbean.api.request;

import net.sunshow.toolkit.core.qbean.api.enums.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * 封装请求对象供自定义查询使用
 * 支持自定义字段的各种运算比较
 * Created by sunshow.
 */
public class QRequest implements Serializable {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<QSelect> selectList;

    private List<QFilter> filterList;

    private boolean distinct;

    public QRequest() {

    }

    public static QRequest newInstance() {
        return new QRequest();
    }

    public QRequest select(String... fields) {
        if (selectList == null) {
            selectList = new ArrayList<>();
        }
        for (String field : fields) {
            selectList.add(new QSelect(field));
        }
        return this;
    }

    public QRequest filter(Operator operator, String field, Object... values) {
        if (filterList == null) {
            filterList = new ArrayList<>();
        }
        try {
            if (values == null) {
                values = new Object[0];
            }
            if (Operator.isLogical(operator)) {
                if (operator == Operator.OR || operator == Operator.AND) {
                    if (field != null && !field.isEmpty()) {
                        throw new IllegalArgumentException("OR 和 AND 操作不允许指定 field");
                    }

                    for (Object value : values) {
                        if (!(value instanceof QFilter)) {
                            throw new IllegalArgumentException("逻辑运算参数不为 filter 表达式");
                        }
                    }

                    filterList.add(new QFilter(operator, null, Arrays.asList(values)));
                }
            } else if (Operator.isZero(operator)) {
                // 支持不需要参数的运算符
                if (values.length > 0) {
                    throw new IllegalArgumentException("参数个数不为0");
                }
                filterList.add(new QFilter(operator, field));
            } else if (Operator.isUnary(operator)) {
                // 单目运算符
                if (values.length > 1) {
                    throw new IllegalArgumentException("单目运算符参数个数超过1个");
                }
                filterList.add(new QFilter(operator, field, values[0]));
            } else if (Operator.isBinary(operator)) {
                if (values.length != 2) {
                    throw new IllegalArgumentException("双目运算符参数个数不为1个");
                }
                filterList.add(new QFilter(operator, field, Arrays.asList(values)));
            } else if (Operator.isCollection(operator)) {
                filterList.add(new QFilter(operator, field, Arrays.asList(values)));
            } else {
                throw new IllegalArgumentException("暂未支持的运算符操作");
            }
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        return this;
    }

    public QRequest filterEqual(String field, Object value) {
        return this.filter(Operator.EQUAL, field, value);
    }

    public QRequest filterLessEqual(String field, Object value) {
        return this.filter(Operator.LESS_EQUAL, field, value);
    }

    public QRequest filterGreaterEqual(String field, Object value) {
        return this.filter(Operator.GREATER_EQUAL, field, value);
    }

    public QRequest filterLessThan(String field, Object value) {
        return this.filter(Operator.LESS_THAN, field, value);
    }

    public QRequest filterGreaterThan(String field, Object value) {
        return this.filter(Operator.GREATER_THAN, field, value);
    }

    public QRequest filterBetween(String field, Object low, Object high) {
        if (low != null && high != null) {
            return this.filter(Operator.BETWEEN, field, low, high);
        }
        if (low == null && high == null) {
            return this;
        } else if (low == null) {
            return this.filterLessEqual(field, high);
        } else {
            return this.filterGreaterEqual(field, low);
        }
    }

    public QRequest filterIn(String field, Iterable<Object> valueList) {
        return this.filter(Operator.IN, field, StreamSupport.stream(valueList.spliterator(), false).toArray(Object[]::new));
    }

    public QRequest filterNotIn(String field, Iterable<Object> valueList) {
        return this.filter(Operator.NOT_IN, field, StreamSupport.stream(valueList.spliterator(), false).toArray(Object[]::new));
    }

    public QRequest filterLike(String field, String value) {
        return this.filter(Operator.LIKE, field, value);
    }

    public QRequest filterLike(String field, String value, char escapeChar) {
        return this.filter(Operator.LIKE_ESCAPE, field, value, escapeChar);
    }

    public QRequest filterNotNull(String field) {
        return this.filter(Operator.NOT_NULL, field);
    }

    public QRequest filterNotEqual(String field, Object value) {
        return this.filter(Operator.NOT_EQUAL, field, value);
    }

    public QRequest filterOr(QFilter... filters) {
        return this.filter(Operator.OR, null, (Object[]) filters);
    }

    public QRequest distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public List<QFilter> getFilterList() {
        return filterList;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<QSelect> getSelectList() {
        return selectList;
    }

    public void setSelectList(List<QSelect> selectList) {
        this.selectList = selectList;
    }
}


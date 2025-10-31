package net.sunshow.toolkit.core.qbean.api.request

import net.sunshow.toolkit.core.qbean.api.enums.Operator

/**
 * 请求过滤条件
 */
class QFilter {

    var field: String? = null
    var value: Any? = null
    var operator: Operator? = null
    var valueList: List<Any>? = null

    constructor()

    constructor(operator: Operator, field: String?) {
        this.field = field
        this.operator = operator
    }

    constructor(operator: Operator, field: String?, value: Any?) {
        this.field = field
        this.value = value
        this.operator = operator
    }

    constructor(operator: Operator, field: String?, valueList: List<Any>?) {
        this.field = field
        this.valueList = valueList
        this.operator = operator
    }

    init {
        // 验证：如果 operator 不为 null 且不是逻辑操作符，field 不能为 null
        if (operator != null && !Operator.isLogical(operator) && field == null) {
            throw IllegalArgumentException("field cannot be null for non-logical operator: $operator")
        }
    }

    companion object {
        @JvmStatic
        fun equal(field: String, value: Any): QFilter {
            return QFilter(Operator.EQUAL, field, value)
        }

        @JvmStatic
        fun lessEqual(field: String, value: Comparable<*>): QFilter {
            return QFilter(Operator.LESS_EQUAL, field, value)
        }

        @JvmStatic
        fun greaterEqual(field: String, value: Comparable<*>): QFilter {
            return QFilter(Operator.GREATER_EQUAL, field, value)
        }

        @JvmStatic
        fun lessThan(field: String, value: Comparable<*>): QFilter {
            return QFilter(Operator.LESS_THAN, field, value)
        }

        @JvmStatic
        fun greaterThan(field: String, value: Comparable<*>): QFilter {
            return QFilter(Operator.GREATER_THAN, field, value)
        }

        @JvmStatic
        fun between(field: String, low: Comparable<*>, high: Comparable<*>): QFilter {
            return QFilter(Operator.BETWEEN, field, listOf(low, high))
        }

        @JvmStatic
        fun `in`(field: String, valueList: Iterable<Any>): QFilter {
            return QFilter(Operator.IN, field, valueList.toList())
        }

        @JvmStatic
        fun notIn(field: String, valueList: Iterable<Any>): QFilter {
            return QFilter(Operator.NOT_IN, field, valueList.toList())
        }

        @JvmStatic
        fun like(field: String, value: String): QFilter {
            return QFilter(Operator.LIKE, field, value)
        }

        @JvmStatic
        fun like(field: String, value: String, escapeChar: Char): QFilter {
            return QFilter(Operator.LIKE_ESCAPE, field, listOf(value, escapeChar))
        }

        @JvmStatic
        fun notNull(field: String): QFilter {
            return QFilter(Operator.NOT_NULL, field)
        }

        @JvmStatic
        fun isNull(field: String): QFilter {
            return QFilter(Operator.IS_NULL, field)
        }

        @JvmStatic
        fun notEqual(field: String, value: Any): QFilter {
            return QFilter(Operator.NOT_EQUAL, field, value)
        }

        @JvmStatic
        fun matchText(field: String, value: String): QFilter {
            return QFilter(Operator.MATCH_TEXT, field, value)
        }

        @JvmStatic
        fun matchKeyword(field: String, value: String): QFilter {
            return QFilter(Operator.MATCH_KEYWORD, field, value)
        }

        @JvmStatic
        fun or(vararg filters: QFilter): QFilter {
            return or(filters.toList())
        }

        @JvmStatic
        fun or(filterList: List<QFilter>): QFilter {
            val list = filterList.map { it as Any }
            return QFilter(Operator.OR, null, list)
        }
    }
}

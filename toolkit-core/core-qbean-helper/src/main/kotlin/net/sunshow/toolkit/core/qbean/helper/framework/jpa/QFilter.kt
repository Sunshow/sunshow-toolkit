package net.sunshow.toolkit.core.qbean.helper.framework.jpa

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import net.sunshow.toolkit.core.qbean.api.enums.Operator
import net.sunshow.toolkit.core.qbean.api.request.QFilter

private val logger = KotlinLogging.logger {}

/**
 * 将 QFilter 转换为 JPA Predicate
 *
 * @param root JPA Root 对象
 * @param cb CriteriaBuilder 用于构建查询条件
 * @return 转换后的 Predicate，如果无法转换则返回 null
 */
fun <T> QFilter.toPredicate(root: Root<T>, cb: CriteriaBuilder): Predicate? {
    @Suppress("UNCHECKED_CAST")
    when (operator) {
        Operator.EQUAL -> {
            return cb.equal(root.get<Any>(field), value)
        }

        Operator.GREATER_EQUAL -> {
            return if (value is Comparable<*>) {
                cb.greaterThanOrEqualTo(root.get(field), value as Comparable<Any>)
            } else {
                throw IllegalArgumentException("字段($field)不是可比较对象, value=$value")
            }
        }

        Operator.LESS_EQUAL -> {
            return if (value is Comparable<*>) {
                cb.lessThanOrEqualTo(root.get(field), value as Comparable<Any>)
            } else {
                throw IllegalArgumentException("字段($field)不是可比较对象, value=$value")
            }
        }

        Operator.GREATER_THAN -> {
            return if (value is Comparable<*>) {
                cb.greaterThan(root.get(field), value as Comparable<Any>)
            } else {
                throw IllegalArgumentException("字段($field)不是可比较对象, value=$value")
            }
        }

        Operator.LESS_THAN -> {
            return if (value is Comparable<*>) {
                cb.lessThan(root.get(field), value as Comparable<Any>)
            } else {
                throw IllegalArgumentException("字段($field)不是可比较对象, value=$value")
            }
        }

        Operator.BETWEEN -> {
            val val1 = valueList?.get(0)
            val val2 = valueList?.get(1)

            return if (val1 is Comparable<*> && val2 is Comparable<*>) {
                cb.between(root.get(field), val1 as Comparable<Any>, val2 as Comparable<Any>)
            } else {
                throw IllegalArgumentException("字段($field)不是可比较对象, value1=$val1, value2=$val2")
            }
        }

        Operator.IN -> {
            return root.get<Any>(field).`in`(valueList)
        }

        Operator.NOT_IN -> {
            return cb.not(root.get<Any>(field).`in`(valueList))
        }

        Operator.LIKE -> {
            return cb.like(root.get(field), value.toString())
        }

        Operator.LIKE_ESCAPE -> {
            return cb.like(
                root.get(field),
                valueList?.get(0).toString(),
                valueList?.get(1) as Char
            )
        }

        Operator.NOT_NULL -> {
            return cb.isNotNull(root.get<Any>(field))
        }

        Operator.IS_NULL -> {
            return cb.isNull(root.get<Any>(field))
        }

        Operator.NOT_EQUAL -> {
            return cb.notEqual(root.get<Any>(field), value)
        }

        Operator.AND, Operator.OR -> {
            if (valueList.isNullOrEmpty()) {
                return null
            }
            if (!field.isNullOrBlank()) {
                logger.error {
                    ("OR 和 AND 操作不允许指定 field: $field")
                }
                return null
            }

            val predicateList = mutableListOf<Predicate>()
            for (o in valueList!!) {
                val f = o as QFilter
                // 递归调用
                f.toPredicate(root, cb)
                    ?.apply {
                        predicateList.add(this)
                    }
            }

            if (predicateList.isNotEmpty()) {
                return when (operator) {
                    Operator.AND -> cb.and(*predicateList.toTypedArray())
                    Operator.OR -> cb.or(*predicateList.toTypedArray())
                    else -> null
                }
            }
            return null
        }

        else -> {
            logger.error {
                "不支持的运算符, op=$operator"
            }
            return null
        }
    }
}

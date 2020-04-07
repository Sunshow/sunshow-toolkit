package net.sunshow.toolkit.core.qbean.api.enums;

/**
 * 运算符类型
 *
 * @author sunshow
 */
public enum Operator {

    EQUAL,
    NOT_EQUAL,

    LESS_EQUAL,
    GREATER_EQUAL,

    LESS_THAN,
    GREATER_THAN,

    BETWEEN,

    IN,
    NOT_IN,

    LIKE,

    IS_NULL,
    NOT_NULL,

    AND,
    OR,
    NOT,   // 暂不支持, 预留

    ;

    public static boolean isZero(Operator operator) {
        return operator == IS_NULL || operator == NOT_NULL;
    }

    public static boolean isUnary(Operator operator) {
        return operator == EQUAL || operator == NOT_EQUAL || operator == LESS_EQUAL || operator == GREATER_EQUAL || operator == LESS_THAN || operator == GREATER_THAN;
    }

    public static boolean isBinary(Operator operator) {
        return operator == BETWEEN;
    }

    public static boolean isCollection(Operator operator) {
        return operator == IN || operator == NOT_IN;
    }

    public static boolean isLogical(Operator operator) {
        return operator == AND || operator == OR || operator == NOT;
    }
}

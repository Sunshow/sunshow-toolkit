package net.sunshow.toolkit.core.qbean.helper.repository.support;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Data JPA 的 Criteria 路径不支持 Sort.NullHandling（会抛 UnsupportedOperationException）。
 * 在此用 Hibernate {@link HibernateCriteriaBuilder#asc(Expression, boolean)} /
 * {@link HibernateCriteriaBuilder#desc(Expression, boolean)} 生成 NULLS FIRST/LAST；
 * 非 Hibernate 时回退为 CASE 表达式模拟。
 */
public final class NullAwareSortOrders {

    private NullAwareSortOrders() {
    }

    public static boolean requiresNullAwareOrdering(@Nullable Sort sort) {
        if (sort == null || !sort.isSorted()) {
            return false;
        }
        for (Sort.Order order : sort) {
            if (order.getNullHandling() != Sort.NullHandling.NATIVE) {
                return true;
            }
        }
        return false;
    }

    public static <S> Specification<S> asOrderSpecification(Sort sort) {
        return (root, query, cb) -> {
            if (query == null || isCountQuery(query)) {
                return null;
            }
            query.orderBy(toJpaOrders(sort, root, cb));
            return null;
        };
    }

    static boolean isCountQuery(CriteriaQuery<?> query) {
        Class<?> resultType = query.getResultType();
        return resultType == Long.class || resultType == long.class;
    }

    @SuppressWarnings("unchecked")
    static List<jakarta.persistence.criteria.Order> toJpaOrders(Sort sort, Root<?> root, CriteriaBuilder cb) {
        List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            Expression<?> expression = resolvePath(root, order.getProperty());
            if (order.isIgnoreCase() && String.class.equals(expression.getJavaType())) {
                expression = cb.lower((Expression<String>) expression);
            }
            appendOrder(orders, cb, expression, order);
        }
        return orders;
    }

    static void appendOrder(List<jakarta.persistence.criteria.Order> orders,
                            CriteriaBuilder cb,
                            Expression<?> expression,
                            Sort.Order order) {
        Sort.NullHandling nullHandling = order.getNullHandling();
        if (nullHandling == Sort.NullHandling.NATIVE) {
            orders.add(order.isAscending() ? cb.asc(expression) : cb.desc(expression));
            return;
        }

        boolean nullsFirst = nullHandling == Sort.NullHandling.NULLS_FIRST;

        if (cb instanceof HibernateCriteriaBuilder hcb) {
            // second arg: nullsFirst
            orders.add(order.isAscending()
                    ? hcb.asc(expression, nullsFirst)
                    : hcb.desc(expression, nullsFirst));
            return;
        }

        // 非 Hibernate：CASE 模拟 NULLS FIRST/LAST
        Expression<Integer> nullRank = cb.<Integer>selectCase()
                .when(cb.isNull(expression), nullsFirst ? 0 : 1)
                .otherwise(nullsFirst ? 1 : 0);
        orders.add(cb.asc(nullRank));
        orders.add(order.isAscending() ? cb.asc(expression) : cb.desc(expression));
    }

    static Path<?> resolvePath(From<?, ?> from, String property) {
        String[] parts = property.split("\\.");
        Path<?> path = from;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }
}

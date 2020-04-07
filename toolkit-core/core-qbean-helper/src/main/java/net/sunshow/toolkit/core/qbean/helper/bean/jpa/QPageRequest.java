package net.sunshow.toolkit.core.qbean.helper.bean.jpa;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * author: sunshow.
 */
public class QPageRequest extends PageRequest {

    private boolean withoutCountQuery = false;

    /**
     * Creates a new {@link QPageRequest} with sort parameters applied.
     *
     * @param page zero-based page index.
     * @param size the size of the page to be returned.
     * @param sort can be {@literal null}.
     */
    public QPageRequest(int page, int size, Sort sort) {
        super(page, size, sort);
    }

    public boolean isWithoutCountQuery() {
        return withoutCountQuery;
    }

    public void setWithoutCountQuery(boolean withoutCountQuery) {
        this.withoutCountQuery = withoutCountQuery;
    }
}

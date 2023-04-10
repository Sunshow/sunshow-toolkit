package net.sunshow.toolkit.core.qbean.api.search;

import net.sunshow.toolkit.core.qbean.api.enums.Operator;
import net.sunshow.toolkit.core.qbean.api.request.QPage;
import net.sunshow.toolkit.core.qbean.api.request.QRequest;

import java.util.List;

public class PageSearch {

    /**
     * zero based
     */
    private int page = 0;

    private int pageSize = 10;

    private List<FieldFilter> filters;

    private List<FieldSort> sorts;

    public QRequest toQRequest() {
        QRequest result = QRequest.newInstance();

        if (filters != null) {
            for (FieldFilter filter : filters) {
                if (filter.getOperator() == Operator.AND || filter.getOperator() == Operator.OR) {
                    Object[] values = new Object[filter.getValues().length];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = ((FieldFilter) filter.getValues()[i]).toQFilter();
                    }
                    result.filter(filter.getOperator(), null, values);
                } else {
                    result.filter(filter.getOperator(), filter.getField(), filter.getValues());
                }
            }
        }

        return result;
    }

    public QPage toQPage() {
        return toQPage(false);
    }

    public QPage toQPage(boolean withoutCountQuery) {
        QPage result = QPage.newInstance()
                .paging(page, pageSize)
                .withoutCountQuery(withoutCountQuery);

        if (sorts != null) {
            for (FieldSort sort : sorts) {
                result.addOrder(sort.getField(), sort.getDirection());
            }
        }

        return result;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<FieldFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<FieldFilter> filters) {
        this.filters = filters;
    }

    public List<FieldSort> getSorts() {
        return sorts;
    }

    public void setSorts(List<FieldSort> sorts) {
        this.sorts = sorts;
    }

}

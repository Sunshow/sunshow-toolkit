package net.sunshow.toolkit.core.qbean.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 封装分页请求
 * 支持排序
 * Created by sunshow.
 */
public class QPage implements Serializable {

    private List<QSort> sortList;

    private int page = 0;
    private int pageSize = 10;

    /**
     * 避免翻页慢查询, 指定上次查询的有序ID
     */
    private Long limitId;

    /**
     * 默认就不生成 count query
     */
    private boolean withoutCountQuery = true;

    private QPage() {

    }

    public static QPage newInstance() {
        return new QPage();
    }

    public QPage addOrder(String field) {
        return this.addOrder(field, QSort.Order.ASC);
    }

    public QPage addOrder(String field, QSort.Order order) {
        if (sortList == null) {
            sortList = new ArrayList<>();
        }
        this.sortList.add(new QSort(field, order));
        return this;
    }

    public QPage paging(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
        return this;
    }

    public QPage limitId(long limitId) {
        this.limitId = limitId;
        return this;
    }

    public QPage withoutCountQuery(boolean withoutCountQuery) {
        this.withoutCountQuery = withoutCountQuery;
        return this;
    }

    /**
     * 下一页继续查找
     */
    public QPage pagingNext() {
        this.page++;
        return this;
    }

    public List<QSort> getSortList() {
        return sortList;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Long getLimitId() {
        return limitId;
    }

    public boolean isWithoutCountQuery() {
        return withoutCountQuery;
    }
}


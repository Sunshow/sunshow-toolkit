package net.sunshow.toolkit.core.qbean.api.request;

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

    private int pageIndex = 0;
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

    public QPage paging(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
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
        this.pageIndex++;
        return this;
    }

    public List<QSort> getSortList() {
        return sortList;
    }

    public void setSortList(List<QSort> sortList) {
        this.sortList = sortList;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Long getLimitId() {
        return limitId;
    }

    public void setLimitId(Long limitId) {
        this.limitId = limitId;
    }

    public void setWithoutCountQuery(boolean withoutCountQuery) {
        this.withoutCountQuery = withoutCountQuery;
    }

    public boolean isWithoutCountQuery() {
        return withoutCountQuery;
    }
}


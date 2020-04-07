package net.sunshow.toolkit.core.qbean.response;

import net.sunshow.toolkit.core.qbean.request.QPage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 封装响应对象供自定义查询使用
 * 支持分页
 * Created by sunshow on 5/19/15.
 */
public class QResponse<E> implements Serializable {

    /* 回写请求时的分页设置 */
    private int page;
    private int pageSize;

    /* 满足条件的记录总数 */
    private long total = 0;

    private Collection<E> pagedData;

    public QResponse(int page, int pageSize, Collection<E> pagedData, long total) {
        this.page = page;
        this.pageSize = pageSize;
        this.pagedData = pagedData;
        this.total = total;
    }

    public QResponse(int page, int pageSize, Collection<E> pagedData) {
        this(page, pageSize, pagedData, 0);
    }

    public QResponse(int page, int pageSize) {
        this(page, pageSize, null);
    }

    public QResponse(QPage requestPage) {
        this(requestPage.getPage(), requestPage.getPageSize());
    }

    public QResponse() {
    }

    public int getCount() {
        if (pagedData == null) {
            return 0;
        }

        return pagedData.size();
    }

    public int getPageTotal() {
        if (total <= 0) {
            return 0;
        }

        return (int) Math.ceil((double) total / pageSize);
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

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public Collection<E> getPagedData() {
        if (pagedData == null) {
            pagedData = new ArrayList<>();
        }
        return pagedData;
    }

    public void setPagedData(Collection<E> pagedData) {
        this.pagedData = pagedData;
    }
}


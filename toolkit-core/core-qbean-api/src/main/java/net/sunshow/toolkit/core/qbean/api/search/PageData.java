package net.sunshow.toolkit.core.qbean.api.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageData<E> {

    /* 回写请求时的分页设置 */
    private int page;
    private int pageSize;

    /* 满足条件的记录总数 */
    private long total = 0;

    private Collection<E> data;

    public PageData(int page, int pageSize, Collection<E> data, long total) {
        this.page = page;
        this.pageSize = pageSize;
        if (data == null) {
            this.data = new ArrayList<>();
        } else {
            this.data = data;
        }
        this.total = total;
    }

    public PageData(int page, int pageSize, Collection<E> data) {
        this(page, pageSize, data, 0);
    }

    public PageData(int page, int pageSize) {
        this(page, pageSize, null);
    }

    public PageData() {
    }

    public <T> PageData<T> map(Function<E, T> mapper) {
        List<T> mapList = this.getData().stream()
                .map(mapper)
                .collect(Collectors.toList());
        return new PageData<>(this.getPage(), this.getPageSize(), mapList, this.getTotal());
    }

    public int getCount() {
        if (data == null) {
            return 0;
        }

        return data.size();
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

    public Collection<E> getData() {
        if (data == null) {
            data = new ArrayList<>();
        }
        return data;
    }

    public void setData(Collection<E> data) {
        this.data = data;
    }
}

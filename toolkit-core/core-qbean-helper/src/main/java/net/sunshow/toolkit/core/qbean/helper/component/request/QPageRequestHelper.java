package net.sunshow.toolkit.core.qbean.helper.component.request;

import net.sunshow.toolkit.core.qbean.api.request.QPage;
import net.sunshow.toolkit.core.qbean.api.request.QRequest;
import net.sunshow.toolkit.core.qbean.api.response.QResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class QPageRequestHelper {

    public static <T> List<T> request(QRequest request, QPage requestPage, BiFunction<QRequest, QPage, QResponse<T>> biFunction) throws RuntimeException {
        return request(request, requestPage, biFunction, 0);
    }

    public static <T> List<T> request(QRequest request, QPage requestPage, BiFunction<QRequest, QPage, QResponse<T>> biFunction, int maxPagingCount) throws RuntimeException {
        List<T> result = new ArrayList<>();

        while (true) {
            QResponse<T> response = biFunction.apply(request, requestPage);

            if (response == null || response.getCount() == 0) {
                break;
            }

            result.addAll(response.getPagedData());

            if (response.getCount() < requestPage.getPageSize()) {
                break;
            }

            requestPage.pagingNext();

            if (maxPagingCount > 0 && requestPage.getPageIndex() >= maxPagingCount) {
                // 达到最大翻页次数
                break;
            }
        }

        return result;
    }

}

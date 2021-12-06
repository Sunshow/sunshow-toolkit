package net.sunshow.toolkit.core.base.enums;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 基础枚举助手
 * 将枚举接口方法实现细节统一封装，将来会产生大量枚举，方法逻辑修改只需处理助手类
 *
 * @author qatang
 */
public class BaseEnumHelper {

    /**
     * 根据断言获取枚举对象
     *
     * @param predicate 断言
     * @param enums 枚举数组
     * @param <T>   泛型
     * @return 枚举对象
     */
    public static <T extends BaseEnum> T getByPredicate(Predicate<T> predicate, T[] enums) {
        return Arrays.stream(enums)
                .filter(predicate)
                .findAny()
                .orElseThrow(InvalidEnumValueException::new);
    }


    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @param enums 枚举数组
     * @param <T>   泛型
     * @return 枚举对象
     */
    public static <T extends BaseEnum> T getByValue(int value, T[] enums) {
        return getByPredicate(e -> e.getValue() == value, enums);
    }

    /**
     * 根据枚举名称获取枚举对象
     *
     * @param name  枚举名称
     * @param enums 枚举数组
     * @param <T>   泛型
     * @return 枚举对象
     */
    public static <T extends BaseEnum> T getByName(String name, T[] enums) {
        return getByPredicate(e -> name.equals(e.getName()), enums);
    }

    /**
     * 获取全部枚举对象列表
     *
     * @param enums 枚举数组
     * @param <T>   泛型
     * @return 枚举对象列表
     */
    public static <T extends BaseEnum> List<T> getAllList(T[] enums) {
        return Arrays.asList(enums);
    }

    /**
     * 获取枚举对象列表
     *
     * @param enums 枚举数组
     * @param <T>   泛型
     * @return 枚举对象列表
     */
    public static <T extends BaseEnum> List<T> getList(T[] enums) {
        return Arrays.stream(enums)
                .filter(e -> e.getValue() != -1)
                .collect(Collectors.toList());
    }
}

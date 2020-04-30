package net.sunshow.toolkit.core.base.enums;

import java.util.List;

public enum EnableStatus implements BaseEnum {
    ALL(-1, "全部"),
    //    DEFAULT(0, "默认"),
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private int value;
    private String name;

    EnableStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }

    public static EnableStatus get(int value) {
        return BaseEnumHelper.getByValue(value, values());
    }

    public static List<EnableStatus> list() {
        return BaseEnumHelper.getList(values());
    }

    public static List<EnableStatus> listAll() {
        return BaseEnumHelper.getAllList(values());
    }
}

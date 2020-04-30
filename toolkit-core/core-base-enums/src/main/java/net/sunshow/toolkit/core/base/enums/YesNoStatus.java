package net.sunshow.toolkit.core.base.enums;

import java.util.List;

public enum YesNoStatus implements BaseEnum {
    ALL(-1, "全部"),
    //    DEFAULT(0, "默认"),
    NO(0, "否"),
    YES(1, "是");

    private int value;
    private String name;

    YesNoStatus(int value, String name) {
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

    public static YesNoStatus get(int value) {
        return BaseEnumHelper.getByValue(value, values());
    }

    public static List<YesNoStatus> list() {
        return BaseEnumHelper.getList(values());
    }

    public static List<YesNoStatus> listAll() {
        return BaseEnumHelper.getAllList(values());
    }
}

package net.sunshow.toolkit.core.base.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public enum YesNoStatus {
    ALL(-1, "全部"),
    //    DEFAULT(0, "默认"),
    NO(0, "否"),
    YES(1, "是");

    private static Logger logger = LoggerFactory.getLogger(YesNoStatus.class);

    private static final Object _LOCK = new Object();

    private static Map<Integer, YesNoStatus> _MAP;
    private static List<YesNoStatus> _LIST;
    private static List<YesNoStatus> _ALL_LIST;

    static {
        synchronized (_LOCK) {
            Map<Integer, YesNoStatus> map = new HashMap<>();
            List<YesNoStatus> list = new ArrayList<>();
            List<YesNoStatus> listAll = new ArrayList<>();
            for (YesNoStatus value : YesNoStatus.values()) {
                map.put(value.getValue(), value);
                listAll.add(value);
                if (!value.equals(ALL)) {
                    list.add(value);
                }
            }

            _MAP = Collections.unmodifiableMap(map);
            _LIST = Collections.unmodifiableList(list);
            _ALL_LIST = Collections.unmodifiableList(listAll);
        }
    }

    private int value;
    private String name;

    YesNoStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }

    public static YesNoStatus get(int value) {
        try {
            return _MAP.get(value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static List<YesNoStatus> list() {
        return _LIST;
    }

    public static List<YesNoStatus> listAll() {
        return _ALL_LIST;
    }
}

package net.sunshow.toolkit.core.base.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public enum EnableStatus {
    ALL(-1, "全部"),
    //    DEFAULT(0, "默认"),
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private static Logger logger = LoggerFactory.getLogger(EnableStatus.class);

    private static final Object _LOCK = new Object();

    private static Map<Integer, EnableStatus> _MAP;
    private static List<EnableStatus> _LIST;
    private static List<EnableStatus> _ALL_LIST;

    static {
        synchronized (_LOCK) {
            Map<Integer, EnableStatus> map = new HashMap<>();
            List<EnableStatus> list = new ArrayList<>();
            List<EnableStatus> listAll = new ArrayList<>();
            for (EnableStatus value : EnableStatus.values()) {
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

    EnableStatus(int value, String name) {
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

    public static EnableStatus get(int value) {
        try {
            return _MAP.get(value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static List<EnableStatus> list() {
        return _LIST;
    }

    public static List<EnableStatus> listAll() {
        return _ALL_LIST;
    }
}

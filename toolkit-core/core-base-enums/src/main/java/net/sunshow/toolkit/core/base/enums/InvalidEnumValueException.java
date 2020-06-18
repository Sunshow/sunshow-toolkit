package net.sunshow.toolkit.core.base.enums;

public class InvalidEnumValueException extends RuntimeException {

    public InvalidEnumValueException() {
        this("无效的枚举值");
    }

    public InvalidEnumValueException(String message) {
        super(message);
    }
}

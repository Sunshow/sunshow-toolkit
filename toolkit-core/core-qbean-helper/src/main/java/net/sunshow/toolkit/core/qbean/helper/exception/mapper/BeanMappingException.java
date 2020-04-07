package net.sunshow.toolkit.core.qbean.helper.exception.mapper;

/**
 * Bean对象映射异常
 * Created by sunshow.
 */
public class BeanMappingException extends RuntimeException {

    public BeanMappingException() {
        this("Bean对象映射异常");
    }

    public BeanMappingException(String message) {
        super(message);
    }

    public BeanMappingException(Throwable cause) {
        super(cause);
    }

    public BeanMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}

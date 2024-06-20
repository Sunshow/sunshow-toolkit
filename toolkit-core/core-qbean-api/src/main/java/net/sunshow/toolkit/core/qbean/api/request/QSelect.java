package net.sunshow.toolkit.core.qbean.api.request;


import java.io.Serializable;

/**
 * 请求查询字段.
 */
public class QSelect implements Serializable {

    /**
     * 字段名
     */
    private String field;

    /**
     * 字段别名
     */
    private String alias;

    // TODO 扩展支持例如 count/sum 等特殊字段

    private QSelect() {
    }

    public QSelect(String field) {
        this.field = field;
    }

    public QSelect(String field, String alias) {
        this.field = field;
        this.alias = alias;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
    
}

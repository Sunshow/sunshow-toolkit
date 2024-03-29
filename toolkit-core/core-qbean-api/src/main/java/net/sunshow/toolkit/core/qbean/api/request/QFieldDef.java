package net.sunshow.toolkit.core.qbean.api.request;

import net.sunshow.toolkit.core.qbean.api.enums.Control;
import net.sunshow.toolkit.core.qbean.api.enums.Operator;
import net.sunshow.toolkit.core.qbean.api.enums.Wildcard;

/**
 * author: sunshow.
 */
public class QFieldDef {

    // 查询条件中定义的名字
    private String fieldName;

    // 查询条件中定义的类型
    private Class<?> fieldType;

    // 最终查询的列名
    private String name;

    private Control control;

    private String label;

    private String placeholder;

    private String ref;

    private String refId;

    private String refName;

    private String template;

    private boolean desc;

    private Operator operator;

    private Wildcard wildcard;

    private boolean searchable;

    private boolean sortable;

    // 默认排序字段
    private boolean defaultSort;

    private int sortPriority;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public boolean isDefaultSort() {
        return defaultSort;
    }

    public void setDefaultSort(boolean defaultSort) {
        this.defaultSort = defaultSort;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Wildcard getWildcard() {
        return wildcard;
    }

    public void setWildcard(Wildcard wildcard) {
        this.wildcard = wildcard;
    }

    public int getSortPriority() {
        return sortPriority;
    }

    public void setSortPriority(int sortPriority) {
        this.sortPriority = sortPriority;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public void setFieldType(Class<?> fieldType) {
        this.fieldType = fieldType;
    }
}

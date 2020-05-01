package net.sunshow.toolkit.core.qbean.api.request;

import net.sunshow.toolkit.core.qbean.api.enums.Control;

/**
 * author: sunshow.
 */
public class QFieldDef {

    private String name;

    private Control control;

    private String label;

    private String placeholder;

    private String ref;

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
    
}

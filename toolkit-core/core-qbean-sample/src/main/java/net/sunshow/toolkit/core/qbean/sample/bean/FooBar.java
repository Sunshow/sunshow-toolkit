package net.sunshow.toolkit.core.qbean.sample.bean;

import net.sunshow.toolkit.core.qbean.api.annotation.*;
import net.sunshow.toolkit.core.qbean.api.bean.AbstractQBean;

import java.time.LocalDateTime;

/**
 * author: sunshow.
 */
@QBean
@QBeanCreator
@QBeanUpdater
public class FooBar extends AbstractQBean {
    /**
     * 主键
     */
    @QBeanID
    @QBeanCreatorIgnore
    private Long id;

    private String foo;

    private Integer bar;

    @QBeanCreatorIgnore
    private LocalDateTime createdTime;

    @QBeanCreatorIgnore
    private LocalDateTime updatedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    public Integer getBar() {
        return bar;
    }

    public void setBar(Integer bar) {
        this.bar = bar;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}

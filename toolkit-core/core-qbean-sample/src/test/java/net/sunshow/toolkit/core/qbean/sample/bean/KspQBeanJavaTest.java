package net.sunshow.toolkit.core.qbean.sample.bean;

import org.junit.jupiter.api.Test;

public class KspQBeanJavaTest {

    @Test
    public void testQBean() {
        System.out.println(QKtFooBar.id);
    }

    @Test
    public void testQBeanCreator() {
        KtFooBarCreator creator = KtFooBarCreator.builder()
                .withFoo("test")
                .withBar(1)
                .build();
        System.out.println(creator.getCreateProperties());
    }
    
    @Test
    public void testQBeanUpdater() {
        KtFooBarUpdater updater = KtFooBarUpdater.builder(123)
                .withFoo("updated")
                .withBar(2)
                .build();
        System.out.println(updater.getUpdateProperties());
        System.out.println(updater.getUpdateId());
    }

}

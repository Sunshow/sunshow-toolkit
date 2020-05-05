package net.sunshow.code.generator.template.qbean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QBeanSampleTest {

    QTemplate template;

    @BeforeEach
    public void init() {
        template = new QTemplate();
        template.setOutputPath("/Users/sunshow/Downloads/sample");
        template.setPackagePathPrefix("net.sunshow.code.sample");
        template.setModuleName("foo");
        template.setBeanName("Bar");
        template.setFoModuleName("foo1");
        template.setControllerModuleName("foo2");
        template.setTemplatePrefix("admin/foobar");
        template.setShiroResource("foobar");
        template.setRequestMappingPrefix("/admin/foobar");
    }

    @Test
    public void testGenerateStructure() throws Exception {
        QBeanGenerator.generate(template);
        QEntityGenerator.generate(template);
        QRepositoryGenerator.generate(template);
        QExceptionGenerator.generate(template);
        QServiceGenerator.generate(template);
        QServiceImplGenerator.generate(template);
    }

    @Test
    public void testGenerateByQBeanFields() throws Exception {
        QCreateFOGenerator.generate(template);
        QUpdateFOGenerator.generate(template);
        QSearchFOGenerator.generate(template);
        QCRUDControllerGenerator.generate(template);
    }
}

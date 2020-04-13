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
}

package net.sunshow.code.generator.template.qbean;

import org.junit.jupiter.api.Test;

public class QBeanSampleTest {

    @Test
    public void testGenerateSample() throws Exception {
        QTemplate template = new QTemplate();
        template.setOutputPath("/Users/sunshow/Downloads/sample");
        template.setPackagePathPrefix("net.sunshow.code.sample");
        template.setModuleName("foo");
        template.setBeanName("Bar");

        QBeanGenerator.generate(template);
        QEntityGenerator.generate(template);
        QRepositoryGenerator.generate(template);
        QExceptionGenerator.generate(template);
        QServiceGenerator.generate(template);
        QServiceImplGenerator.generate(template);
    }
}

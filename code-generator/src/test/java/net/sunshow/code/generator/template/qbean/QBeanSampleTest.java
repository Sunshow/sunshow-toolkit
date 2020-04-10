package net.sunshow.code.generator.template.qbean;

import org.junit.jupiter.api.Test;

public class QBeanSampleTest {

    @Test
    public void testGenerateSample() throws Exception {
        QBeanTemplate template = new QBeanTemplate();
        template.setOutputPath("/Users/sunshow/Downloads/sample");
        template.setPackagePathPrefix("net.sunshow.code.sample");
        template.setModuleName("foo");
        template.setBeanName("Bar");

        QBeanGenerator.generate(template);
        QBeanEntityGenerator.generate(template);
        QBeanRepositoryGenerator.generate(template);
        QBeanExceptionGenerator.generate(template);
    }
}

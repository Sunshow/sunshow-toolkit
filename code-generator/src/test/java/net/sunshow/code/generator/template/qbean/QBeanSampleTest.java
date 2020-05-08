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
    public void testGeneratePhase1() throws Exception {
        // 第一阶段
        // 生成 Service 接口和除了DAO和PO之外的文件结构
        QBeanGenerator.generate(template);
        QExceptionGenerator.generate(template);
        QServiceGenerator.generate(template);
    }

    @Test
    public void testGeneratePhase2() throws Exception {
        // 第二阶段
        // 完成 VO 属性定义和注释后生成具体实现
        QEntityGenerator.generate(template);
        QRepositoryGenerator.generate(template);
        QServiceImplGenerator.generate(template);
        
        QCreateFOGenerator.generate(template);
        QUpdateFOGenerator.generate(template);
        QSearchFOGenerator.generate(template);
        QCRUDControllerGenerator.generate(template);
    }
}

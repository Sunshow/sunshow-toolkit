package net.sunshow.code.generator.template.openapi;

import net.sunshow.code.generator.template.openapi.retrofit1.Retrofit1InterfaceGenerator;
import net.sunshow.code.generator.template.openapi.retrofit1.Retrofit1RequestGenerator;
import net.sunshow.code.generator.template.openapi.retrofit1.Retrofit1ResponseGenerator;
import net.sunshow.code.generator.template.openapi.retrofit1.Retrofit1Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OpenApiRetrofit1SampleTest {

    String openApiDoc = "http://192.168.50.23:20001/v3/api-docs";

    OpenApiParser parser;

    Retrofit1Template template;

    String module = "merchant";

    String subModule = "employee";

    @BeforeEach
    public void testInit() {
        parser = new OpenApiParser(openApiDoc);

        template = new Retrofit1Template();
        template.setModuleName(module);
        template.setPackagePathPrefix("tech.xiaoman.nplus6.merchant.api");
        template.setOutputPath("/Users/sunshow/GIT/MambaAITech/nplus6-merchant/src/main/java");
    }

    @Test
    public void testGenerate() throws Exception {
        String endpoint = "/merchant/employee/is_bind";
        EndpointDef def = parser.parse(endpoint, module, subModule);

        Retrofit1RequestGenerator.generate(template, parser, def);
        Retrofit1ResponseGenerator.generate(template, parser, def);
        Retrofit1InterfaceGenerator.generate(template, parser, def);
    }
}

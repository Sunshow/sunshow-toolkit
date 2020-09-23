package net.sunshow.code.generator.template.openapi;

import net.sunshow.code.generator.template.openapi.retrofit1.*;
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

    }

    @Test
    public void testGenerate() throws Exception {
        String endpoint = "/merchant/employee/is_bind";
        EndpointDef def = parser.parse(endpoint, module, subModule);

        template = new Retrofit1Template();
        template.setModuleName(module);
        template.setPackagePathPrefix("tech.xiaoman.nplus6.merchant");
        template.setOutputPath("/Users/sunshow/GIT/MambaAITech/nplus6-merchant/src/main/java");

        template.init(def);

        Retrofit1RequestGenerator.generate(template, parser, def);
        Retrofit1ResponseGenerator.generate(template, parser, def);
        Retrofit1InterfaceGenerator.generate(template, parser, def);
        Retrofit1FOGenerator.generate(template, parser, def);
        Retrofit1ControllerGenerator.generate(template, parser, def);
    }
}

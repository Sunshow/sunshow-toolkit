package net.sunshow.code.generator.template.openapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OpenApiParserTest {

    String openApiDoc = "http://192.168.50.23:20001/v3/api-docs";

    OpenApiParser parser;

    @BeforeEach
    public void testInit() {
        parser = new OpenApiParser(openApiDoc);
    }

    @Test
    public void testParse() {
        parser.parse("/merchant/employee/is_bind", "merchant", null);
    }
}

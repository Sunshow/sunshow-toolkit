package net.sunshow.code.generator.template.openapi;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.javapoet.TypeName;

public class OpenApiUtils {

    public static TypeName generateTypeName(ObjectNode node) {
        String type = node.get("type").asText();
        String format = null;
        if (node.has("format")) {
            format = node.get("format").asText();
        }

        switch (type) {
            case "integer": {
                if (format != null) {
                    switch (format) {
                        case "int64":
                            return TypeName.LONG.box();
                    }
                }
                return TypeName.INT.box();
            }
            case "boolean": {
                return TypeName.BOOLEAN;
            }
            default:
                return TypeName.OBJECT;
        }
    }

}

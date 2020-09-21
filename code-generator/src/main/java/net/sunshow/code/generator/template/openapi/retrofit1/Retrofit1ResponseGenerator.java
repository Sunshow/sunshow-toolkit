package net.sunshow.code.generator.template.openapi.retrofit1;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.sunshow.code.generator.template.openapi.EndpointDef;
import net.sunshow.code.generator.template.openapi.EndpointMethodDef;
import net.sunshow.code.generator.template.openapi.OpenApiParser;
import net.sunshow.code.generator.template.openapi.OpenApiUtils;
import net.sunshow.code.generator.template.qbean.QTemplate;
import net.sunshow.code.generator.util.GenerateUtils;

import javax.lang.model.element.Modifier;
import java.io.File;

public class Retrofit1ResponseGenerator {

    public static void generate(Retrofit1Template template, OpenApiParser parser, EndpointDef def) throws Exception {
        EndpointMethodDef methodDef = def.getMethodDefList().get(0);
        if (methodDef.getResponseSchemaRef() == null) {
            return;
        }
        ObjectNode schemaNode = (ObjectNode) parser.getSchemas().get(methodDef.getResponseSchemaRef());
        if (!schemaNode.has("properties")) {
            return;
        }

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(def.getNamePrefix() + GenerateUtils.lowerCamelToUpperCamel(template.getResponseSuffix()))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(QTemplate.ClassNameLombokData)
                .addAnnotation(QTemplate.ClassNameLombokNoArgsConstructor);

        typeSpecBuilder.addAnnotation(QTemplate.ClassNameLombokAllArgsConstructor);

        // 创建属性
        ObjectNode propertiesNode = (ObjectNode) schemaNode.get("properties");

        propertiesNode.fieldNames().forEachRemaining(field -> {
            ObjectNode node = (ObjectNode) propertiesNode.get(field);
            TypeName typeName = OpenApiUtils.generateTypeName(node);
            String title = null;
            if (node.has("title")) {
                title = node.get("title").asText();
            }

            FieldSpec.Builder builder = FieldSpec.builder(typeName, field, Modifier.PRIVATE);
            if (title != null) {
                builder.addJavadoc(title);
            }
            typeSpecBuilder.addField(builder.build());
        });

        JavaFile javaFile = JavaFile.builder(template.getResponsePackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

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

public class Retrofit1RequestGenerator {

    public static void generate(Retrofit1Template template, OpenApiParser parser, EndpointDef def) throws Exception {
        EndpointMethodDef methodDef = def.getMethodDefList().get(0);
        if (methodDef.getRequestSchemaRef() == null) {
            return;
        }

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(template.getNamePrefix() + GenerateUtils.lowerCamelToUpperCamel(template.getRequestSuffix()))
                .addModifiers(Modifier.PUBLIC);

        if (def.isPageable()) {
            typeSpecBuilder.addAnnotation(QTemplate.ClassNameLombokGetter)
                    .addAnnotation(QTemplate.ClassNameLombokSetter)
                    .superclass(template.getPageableRequestClassName());
        } else {
            typeSpecBuilder.addAnnotation(QTemplate.ClassNameLombokData);
        }

        typeSpecBuilder.addAnnotation(QTemplate.ClassNameLombokNoArgsConstructor);

        ObjectNode schemaNode = (ObjectNode) parser.getSchemas().get(methodDef.getRequestSchemaRef());
        if (schemaNode.has("properties")) {
            // 有属性 创建构造函数
            typeSpecBuilder.addAnnotation(QTemplate.ClassNameLombokAllArgsConstructor);

            // 创建属性
            ObjectNode propertiesNode = (ObjectNode) schemaNode.get("properties");

            propertiesNode.fieldNames().forEachRemaining(field -> {
                if (def.isPageable() && template.getPageableRequestProperties().contains(field)) {
                    return;
                }
                ObjectNode node = (ObjectNode) propertiesNode.get(field);
                TypeName typeName = OpenApiUtils.generateTypeName(node);
                String title = node.get("title").asText();

                FieldSpec.Builder builder = FieldSpec.builder(typeName, field, Modifier.PRIVATE)
                        .addJavadoc(title);
                typeSpecBuilder.addField(builder.build());
            });
        }

        JavaFile javaFile = JavaFile.builder(template.getRequestPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

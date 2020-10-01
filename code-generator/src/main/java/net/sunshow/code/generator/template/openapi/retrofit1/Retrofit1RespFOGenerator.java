package net.sunshow.code.generator.template.openapi.retrofit1;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.javapoet.*;
import net.sunshow.code.generator.template.openapi.EndpointDef;
import net.sunshow.code.generator.template.openapi.EndpointMethodDef;
import net.sunshow.code.generator.template.openapi.OpenApiParser;
import net.sunshow.code.generator.template.openapi.OpenApiUtils;
import net.sunshow.code.generator.template.qbean.QTemplate;

import javax.lang.model.element.Modifier;
import java.io.File;

public class Retrofit1RespFOGenerator {

    public static void generate(Retrofit1Template template, OpenApiParser parser, EndpointDef def) throws Exception {
        EndpointMethodDef methodDef = def.getMethodDefList().get(0);
        if (methodDef.getResponseSchemaRef() == null) {
            return;
        }
        ObjectNode schemaNode = (ObjectNode) parser.getSchemas().get(methodDef.getResponseSchemaRef());
        if (!schemaNode.has("properties")) {
            return;
        }
        if (!def.isPageable() || !def.isPageableResponseHasExtraProperties()) {
            // 只在有额外分页属性时生成
            return;
        }

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(template.getNamePrefix() + template.getRespFOSuffix())
                .addModifiers(Modifier.PUBLIC);
        if (def.isPageable()) {
            TypeName limitRespTypeName = ParameterizedTypeName.get(template.getLimitRespFOClassName(), ClassName.OBJECT);

            typeSpecBuilder.addAnnotation(QTemplate.ClassNameLombokGetter)
                    .addAnnotation(QTemplate.ClassNameLombokSetter)
                    .superclass(limitRespTypeName);
        } else {
            typeSpecBuilder.addAnnotation(QTemplate.ClassNameLombokData);
        }
        typeSpecBuilder.addAnnotation(QTemplate.ClassNameLombokNoArgsConstructor);
        typeSpecBuilder.addAnnotation(QTemplate.ClassNameLombokAllArgsConstructor);

        // 创建属性
        ObjectNode propertiesNode = (ObjectNode) schemaNode.get("properties");

        propertiesNode.fieldNames().forEachRemaining(field -> {
            if (def.isPageable() && template.getPageableResponseProperties().contains(field)) {
                return;
            }
            if (def.isPageable() && def.getPageableListProperty().equals(field)) {
                return;
            }
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

        JavaFile javaFile = JavaFile.builder(template.getFOPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

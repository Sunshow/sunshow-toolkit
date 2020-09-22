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
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.io.File;

public class Retrofit1FOGenerator {

    public static void generate(Retrofit1Template template, OpenApiParser parser, EndpointDef def) throws Exception {
        EndpointMethodDef methodDef = def.getMethodDefList().get(0);
        if (methodDef.getRequestSchemaRef() == null) {
            return;
        }

        ObjectNode schemaNode = (ObjectNode) parser.getSchemas().get(methodDef.getRequestSchemaRef());
        if (StringUtils.isNotEmpty(template.getFoIgnoreSessionProperty())) {
            if (schemaNode.has("properties") && schemaNode.get("properties").size() == 1 && schemaNode.get("properties").has(template.getFoIgnoreSessionProperty())) {
                // 仅需要传递 session 属性 不生成 FO
                return;
            }
        }

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(def.getNamePrefixCanonical() + template.getFOSuffix())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(QTemplate.ClassNameLombokData)
                .addAnnotation(QTemplate.ClassNameLombokNoArgsConstructor);


        if (schemaNode.has("properties")) {
            // 有属性 创建构造函数
            typeSpecBuilder.addAnnotation(QTemplate.ClassNameLombokAllArgsConstructor);

            // 创建属性
            ObjectNode propertiesNode = (ObjectNode) schemaNode.get("properties");

            propertiesNode.fieldNames().forEachRemaining(field -> {
                if (!field.equals(template.getFoIgnoreSessionProperty())) {
                    ObjectNode node = (ObjectNode) propertiesNode.get(field);
                    TypeName typeName = OpenApiUtils.generateTypeName(node);
                    String title = node.get("title").asText();

                    FieldSpec.Builder builder = FieldSpec.builder(typeName, field, Modifier.PRIVATE)
                            .addJavadoc(title);
                    typeSpecBuilder.addField(builder.build());
                }
            });
        }

        JavaFile javaFile = JavaFile.builder(template.getFOPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

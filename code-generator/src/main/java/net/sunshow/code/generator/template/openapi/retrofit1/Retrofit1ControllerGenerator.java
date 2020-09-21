package net.sunshow.code.generator.template.openapi.retrofit1;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.javapoet.*;
import net.sunshow.code.generator.template.openapi.EndpointDef;
import net.sunshow.code.generator.template.openapi.EndpointMethodDef;
import net.sunshow.code.generator.template.openapi.OpenApiParser;
import net.sunshow.code.generator.template.qbean.QTemplate;
import net.sunshow.code.generator.util.GenerateUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;

public class Retrofit1ControllerGenerator {

    public static void generate(Retrofit1Template template, OpenApiParser parser, EndpointDef def) throws Exception {
        EndpointMethodDef methodDef = def.getMethodDefList().get(0);

        AnnotationSpec classRequestMappingAnnotationSpec = AnnotationSpec.builder(QTemplate.ClassNameSpringRequestMapping)
                .addMember("value", "$S", String.format("%s/%s", def.getModule(), def.getSubModule()))
                .build();
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(def.getApiName() + GenerateUtils.lowerCamelToUpperCamel(template.getControllerSuffix()))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(QTemplate.ClassNameLombokSlf4j)
                .addAnnotation(QTemplate.ClassNameLombokRequiredArgsConstructor)
                .addAnnotation(QTemplate.ClassNameSpringRestController)
                .addAnnotation(classRequestMappingAnnotationSpec);

        String endpointInstance = GenerateUtils.upperCamelToLowerCamel(def.getApiName() + GenerateUtils.lowerCamelToUpperCamel(template.getEndpointSuffix()));
        ClassName endpointClassName = ClassName.get(template.getEndpointPackagePath(), def.getApiName() + GenerateUtils.lowerCamelToUpperCamel(template.getEndpointSuffix()));
        {
            FieldSpec fieldSpec = FieldSpec.builder(endpointClassName, endpointInstance, Modifier.PRIVATE, Modifier.FINAL).build();
            typeSpecBuilder.addField(fieldSpec);
        }

        {
            String mappingName = GenerateUtils.upperCamelToLowerCamel(StringUtils.substringAfter(def.getNamePrefix(), GenerateUtils.lowerCamelToUpperCamel(def.getSubModule())));

            AnnotationSpec methodRequestMappingAnnotationSpec = AnnotationSpec.builder(QTemplate.ClassNameSpringPostMapping)
                    .addMember("value", "$S", String.format("/%s", mappingName))
                    .build();

            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(mappingName)
                    .addAnnotation(methodRequestMappingAnnotationSpec)
                    .addModifiers(Modifier.PUBLIC)
                    .addException(Exception.class)
                    .returns(template.getControllerRespFOClassName());

            ObjectNode schemaNode = (ObjectNode) parser.getSchemas().get(methodDef.getRequestSchemaRef());
            if (schemaNode.has("properties")) {
                if (StringUtils.isNotEmpty(template.getFoIgnoreSessionProperty()) && schemaNode.get("properties").size() == 1 && schemaNode.get("properties").has(template.getFoIgnoreSessionProperty())) {

                } else {
                    // 需要生成请求参数
                    ParameterSpec parameterSpec = ParameterSpec.builder(ClassName.get(template.getFOPackagePath(), def.getNamePrefix() + template.getFOSuffix()), "fo")
                            .addAnnotation(QTemplate.ClassNameSpringRequestBody)
                            .addAnnotation(QTemplate.ClassNameJavaxValid)
                            .build();
                    methodSpecBuilder.addParameter(parameterSpec);
                }
            }

            // 要请求体
            if (methodDef.getRequestSchemaRef() != null) {
                ClassName requestClassName = ClassName.get(template.getRequestPackagePath(), def.getNamePrefix() + GenerateUtils.lowerCamelToUpperCamel(template.getRequestSuffix()));
                methodSpecBuilder.addStatement("$T request = new $T()", requestClassName, requestClassName);
            }
            // 要响应体
            if (methodDef.getResponseSchemaRef() != null && parser.getSchemas().get(methodDef.getResponseSchemaRef()).has("properties")) {
                ClassName responseClassName = ClassName.get(template.getResponsePackagePath(), def.getNamePrefix() + GenerateUtils.lowerCamelToUpperCamel(template.getResponseSuffix()));
                methodSpecBuilder.addStatement("$T<$T> responseWrapper = $N.$N(request).execute().body()", template.getResponseWrapperClassName(), responseClassName, endpointInstance, GenerateUtils.upperCamelToLowerCamel(def.getNamePrefix()));
                methodSpecBuilder.addStatement("$T response = $T.assertWrapperSuccess(responseWrapper)", responseClassName, template.getResponseHelperClassName());
                methodSpecBuilder.addStatement("return $T.ok(response)", template.getControllerRespFOClassName());
            } else {
                methodSpecBuilder.addStatement("$T<$T> responseWrapper = $N.$N(request).execute().body()", template.getResponseWrapperClassName(), TypeName.VOID.box(), endpointInstance, GenerateUtils.upperCamelToLowerCamel(def.getNamePrefix()));
                methodSpecBuilder.addStatement("$T.assertWrapperSuccess(responseWrapper)", template.getResponseHelperClassName());
                methodSpecBuilder.addStatement("return $T.ok()", template.getControllerRespFOClassName());
            }


            typeSpecBuilder.addMethod(methodSpecBuilder.build());
        }


        JavaFile javaFile = JavaFile.builder(template.getControllerPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        System.out.println(javaFile.toString());
        //javaFile.writeTo(new File(template.getOutputPath()));
    }

}

package net.sunshow.code.generator.template.openapi.retrofit1;

import com.squareup.javapoet.*;
import net.sunshow.code.generator.template.openapi.EndpointDef;
import net.sunshow.code.generator.template.openapi.EndpointMethodDef;
import net.sunshow.code.generator.template.openapi.OpenApiParser;
import net.sunshow.code.generator.util.GenerateUtils;

import javax.lang.model.element.Modifier;

public class Retrofit1InterfaceGenerator {

    public static void generate(Retrofit1Template template, OpenApiParser parser, EndpointDef def) throws Exception {
        EndpointMethodDef methodDef = def.getMethodDefList().get(0);

        TypeSpec.Builder typeSpecBuilder = TypeSpec.interfaceBuilder(template.getModulePrefix() + GenerateUtils.lowerCamelToUpperCamel(template.getEndpointSuffix()))
                .addModifiers(Modifier.PUBLIC);

        AnnotationSpec postAnnotationSpec = AnnotationSpec.builder(Retrofit1Template.ClassNameRetrofitPost)
                .addMember("value", "$S", def.getEndpoint().startsWith("/") ? def.getEndpoint().substring(1) : def.getEndpoint())
                .build();
        TypeName responseClassName;
        if (methodDef.getResponseSchemaRef() == null || !parser.getSchemas().get(methodDef.getResponseSchemaRef()).has("properties")) {
            // 如果没有响应体
            responseClassName = ParameterizedTypeName.get(template.getResponseWrapperClassName(), TypeName.VOID.box());
        } else {
            responseClassName = ParameterizedTypeName.get(template.getResponseWrapperClassName(), ClassName.get(template.getResponsePackagePath(), template.getNamePrefix() + GenerateUtils.lowerCamelToUpperCamel(template.getResponseSuffix())));
        }
        TypeName callClassName = ParameterizedTypeName.get(Retrofit1Template.ClassNameRetrofitCall, responseClassName);

        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(GenerateUtils.upperCamelToLowerCamel(def.getName()))
                .addAnnotation(postAnnotationSpec)
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(callClassName);

        if (methodDef.getTagList() != null && !methodDef.getTagList().isEmpty()) {
            methodSpecBuilder.addJavadoc(methodDef.getTagList().get(0));
        }

        // 如果有请求体
        if (methodDef.getRequestSchemaRef() != null) {
            ParameterSpec parameterSpec = ParameterSpec.builder(ClassName.get(template.getRequestPackagePath(), template.getNamePrefix() + GenerateUtils.lowerCamelToUpperCamel(template.getRequestSuffix())), "request")
                    .addAnnotation(Retrofit1Template.ClassNameRetrofitBody)
                    .build();
            methodSpecBuilder.addParameter(parameterSpec);
        }

        typeSpecBuilder.addMethod(methodSpecBuilder.build());

        JavaFile javaFile = JavaFile.builder(template.getEndpointPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        System.out.println(javaFile.toString());
        //javaFile.writeTo(new File(template.getOutputPath()));
    }

}

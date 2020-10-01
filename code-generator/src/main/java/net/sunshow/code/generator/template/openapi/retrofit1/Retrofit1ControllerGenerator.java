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
import java.util.Iterator;

public class Retrofit1ControllerGenerator {

    public static void generate(Retrofit1Template template, OpenApiParser parser, EndpointDef def) throws Exception {
        EndpointMethodDef methodDef = def.getMethodDefList().get(0);

        AnnotationSpec classRequestMappingAnnotationSpec = AnnotationSpec.builder(QTemplate.ClassNameSpringRequestMapping)
                .addMember("value", "$S", String.format("/%s/%s", def.getModule(), def.getSubModule()))
                .build();
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(template.getModulePrefix() + GenerateUtils.lowerCamelToUpperCamel(template.getControllerSuffix()))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(QTemplate.ClassNameLombokSlf4j)
                .addAnnotation(QTemplate.ClassNameLombokRequiredArgsConstructor)
                .addAnnotation(QTemplate.ClassNameSpringRestController)
                .addAnnotation(classRequestMappingAnnotationSpec);

        String endpointInstance = GenerateUtils.upperCamelToLowerCamel(template.getModulePrefix() + GenerateUtils.lowerCamelToUpperCamel(template.getEndpointSuffix()));
        ClassName endpointClassName = ClassName.get(template.getEndpointPackagePath(), template.getModulePrefix() + GenerateUtils.lowerCamelToUpperCamel(template.getEndpointSuffix()));
        {
            FieldSpec fieldSpec = FieldSpec.builder(endpointClassName, endpointInstance, Modifier.PRIVATE, Modifier.FINAL).build();
            typeSpecBuilder.addField(fieldSpec);
        }

        {
            String mappingName = GenerateUtils.upperCamelToLowerCamel(def.getName());

            AnnotationSpec methodRequestMappingAnnotationSpec = AnnotationSpec.builder(QTemplate.ClassNameSpringPostMapping)
                    .addMember("value", "$S", String.format("/%s", mappingName))
                    .build();

            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(mappingName)
                    .addAnnotation(methodRequestMappingAnnotationSpec)
                    .addModifiers(Modifier.PUBLIC)
                    .addException(Exception.class)
                    .returns(template.getControllerRespFOClassName());

            // 要请求体
            String requestInstance = "";
            if (methodDef.getRequestSchemaRef() != null) {
                ObjectNode schemaNode = (ObjectNode) parser.getSchemas().get(methodDef.getRequestSchemaRef());
                if (schemaNode.has("properties")) {
                    if (StringUtils.isNotEmpty(template.getFoIgnoreSessionProperty()) && schemaNode.get("properties").size() == 1 && schemaNode.get("properties").has(template.getFoIgnoreSessionProperty())) {

                    } else {
                        // 需要生成请求参数
                        ParameterSpec parameterSpec = ParameterSpec.builder(ClassName.get(template.getFOPackagePath(), template.getNamePrefix() + template.getFOSuffix()), "fo")
                                .addAnnotation(QTemplate.ClassNameSpringRequestBody)
                                .addAnnotation(QTemplate.ClassNameJavaxValid)
                                .build();
                        methodSpecBuilder.addParameter(parameterSpec);
                        requestInstance = "request";
                    }
                }

                if (StringUtils.isNotEmpty(requestInstance)) {
                    ClassName requestClassName = ClassName.get(template.getRequestPackagePath(), template.getNamePrefix() + GenerateUtils.lowerCamelToUpperCamel(template.getRequestSuffix()));
                    methodSpecBuilder.addCode("$T $N = new $T(", requestClassName, requestInstance, requestClassName);

                    // 有属性 创建构造函数
                    // 创建属性
                    ObjectNode propertiesNode = (ObjectNode) schemaNode.get("properties");
                    Iterator<String> fieldNamesIterator = propertiesNode.fieldNames();
                    boolean needComma = false;
                    while (fieldNamesIterator.hasNext()) {
                        String field = fieldNamesIterator.next();
                        if (def.isPageable() && template.getPageableRequestProperties().contains(field)) {
                            continue;
                        }

                        if (needComma) {
                            methodSpecBuilder.addCode(", ");
                            needComma = false;
                        }

                        methodSpecBuilder.addCode("fo.$N()", GenerateUtils.lowerCamelToGetter(field));

                        if (fieldNamesIterator.hasNext()) {
                            needComma = true;
                        }
                    }

                    methodSpecBuilder.addCode(");\n");

                    if (def.isPageable()) {
                        methodSpecBuilder.addStatement("$N.setPageIndex(fo.getPage())", requestInstance);
                        methodSpecBuilder.addStatement("$N.setPageSize(fo.getLimit())", requestInstance);
                    }
                }
            }
            // 要响应体
            if (methodDef.getResponseSchemaRef() != null && parser.getSchemas().get(methodDef.getResponseSchemaRef()).has("properties")) {
                ClassName responseClassName = ClassName.get(template.getResponsePackagePath(), template.getNamePrefix() + GenerateUtils.lowerCamelToUpperCamel(template.getResponseSuffix()));
                methodSpecBuilder.addStatement("$T<$T> responseWrapper = $N.$N($N).execute().body()", template.getResponseWrapperClassName(), responseClassName, endpointInstance, GenerateUtils.upperCamelToLowerCamel(def.getName()), requestInstance);
                methodSpecBuilder.addStatement("$T response = $T.assertWrapperSuccess(responseWrapper)", responseClassName, template.getResponseHelperClassName());

                if (def.isPageable()) {
                    if (def.isPageableResponseHasExtraProperties()) {
                        ClassName respFOClassName = ClassName.get(template.getFOPackagePath(), template.getNamePrefix() + template.getRespFOSuffix());
                        methodSpecBuilder.addCode("$T respFO = new $T(", respFOClassName, respFOClassName);

                        ObjectNode propertiesNode = (ObjectNode) parser.getSchemas().get(methodDef.getResponseSchemaRef()).get("properties");
                        Iterator<String> fieldNamesIterator = propertiesNode.fieldNames();
                        boolean needComma = false;
                        while (fieldNamesIterator.hasNext()) {
                            String field = fieldNamesIterator.next();
                            if (template.getPageableResponseProperties().contains(field)) {
                                continue;
                            }
                            if (def.getPageableListProperty().equals(field)) {
                                continue;
                            }

                            if (needComma) {
                                methodSpecBuilder.addCode(", ");
                                needComma = false;
                            }

                            methodSpecBuilder.addCode("response.$N()", GenerateUtils.lowerCamelToGetter(field));

                            if (fieldNamesIterator.hasNext()) {
                                needComma = true;
                            }
                        }

                        methodSpecBuilder.addCode(");\n");

                        methodSpecBuilder.addStatement("respFO.setItems(response.$N())", GenerateUtils.lowerCamelToGetter(def.getPageableListProperty()));
                        methodSpecBuilder.addStatement("respFO.setTotal(response.getTotalCount())");
                        methodSpecBuilder.addStatement("respFO.setTotalPage(response.getTotalPageCount())");

                        methodSpecBuilder.addStatement("return $T.ok(respFO)", template.getControllerRespFOClassName());
                    } else {
                        methodSpecBuilder.addStatement("return $T.ok(new $T<>(response.$N(), response.getTotalCount(), response.getTotalPageCount()))",
                                template.getControllerRespFOClassName(),
                                template.getLimitRespFOClassName(),
                                GenerateUtils.lowerCamelToGetter(def.getPageableListProperty()));
                    }
                } else {
                    methodSpecBuilder.addStatement("return $T.ok(response)", template.getControllerRespFOClassName());
                }
            } else {
                methodSpecBuilder.addStatement("$T<$T> responseWrapper = $N.$N($N).execute().body()", template.getResponseWrapperClassName(), TypeName.VOID.box(), endpointInstance, GenerateUtils.upperCamelToLowerCamel(def.getName()), requestInstance);
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

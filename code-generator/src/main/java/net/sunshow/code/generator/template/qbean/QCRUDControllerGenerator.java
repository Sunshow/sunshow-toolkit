package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.*;
import net.sunshow.code.generator.util.GenerateUtils;

import javax.lang.model.element.Modifier;
import java.io.File;

public class QCRUDControllerGenerator {

    public static void generate(QTemplate template) throws Exception {
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(template.getControllerName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(QTemplate.ClassNameSpringController);

        if (template.getRequestMappingPrefix() != null) {
            AnnotationSpec annotationSpec = AnnotationSpec.builder(QTemplate.ClassNameSpringRequestMapping).addMember("value", "$S", template.getRequestMappingPrefix()).build();
            typeSpecBuilder.addAnnotation(annotationSpec);
        }

        if (template.isLombok()) {
            typeSpecBuilder
                    .addAnnotation(QTemplate.ClassNameLombokSlf4j);
        }

        String serviceInstance = GenerateUtils.upperCamelToLowerCamel(template.getServiceName());
        String beanInstance = GenerateUtils.upperCamelToLowerCamel(template.getBeanName());

        // autowire service
        {
            FieldSpec fieldSpec = FieldSpec.builder(template.getServiceClassName(), serviceInstance, Modifier.PRIVATE)
                    .addAnnotation(QTemplate.ClassNameSpringAutowired)
                    .build();
            typeSpecBuilder.addField(fieldSpec);
        }

        // add list method
        {
            ParameterSpec searchParameterSpec = ParameterSpec.builder(template.getSearchFOClassName(), "search")
                    .addAnnotation(QTemplate.ClassNameJavaxValid)
                    .build();

            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("list")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addParameter(searchParameterSpec)
                    .addParameter(QTemplate.ClassNamePageFO, "page")
                    .addParameter(QTemplate.ClassNameSortFO, "sort")
                    .addParameter(QTemplate.ClassNameSpringModelMap, "modelMap")
                    .addStatement("$T request = $T.convertQRequest(search)", QTemplate.ClassNameQRequest, QTemplate.ClassNameQBeanSearchHelper)
                    .addCode("\n")
                    .beginControlFlow("if (sort.getSortFields() == null)")
                    .addStatement("sort.setSortFields(new String[]{$T.$N + \"|\" + $T.Order.DESC.name()})", ClassName.get(template.getBeanPackagePath(), "Q" + template.getBeanName()), template.getIdName(), QTemplate.ClassNameQSort)
                    .endControlFlow()
                    .addCode("\n")
                    .addStatement("$T.addAttributes(modelMap, $N.findAll(request, page.toQPage(sort.toQSortList())), search, sort)", QTemplate.ClassNameQSearchModelHelper, serviceInstance)
                    .addCode("\n")
                    .addStatement("return $S", template.getTemplatePrefix() + "/list");

            if (template.getShiroResource() != null) {
                AnnotationSpec annotationSpec = AnnotationSpec.builder(QTemplate.ClassNameShiroRequiresPermissions).addMember("value", "$S", template.getShiroResource() + ":list").build();
                methodSpecBuilder.addAnnotation(annotationSpec);
            }
            {
                AnnotationSpec annotationSpec = AnnotationSpec.builder(QTemplate.ClassNameSpringRequestMapping).addMember("value", "$S", "/list").build();
                methodSpecBuilder.addAnnotation(annotationSpec);
            }

            typeSpecBuilder.addMethod(methodSpecBuilder.build());
        }

        // add create get
        {
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("create")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addParameter(QTemplate.ClassNameSpringModelMap, "modelMap")
                    .addStatement("return $S", template.getTemplatePrefix() + "/create");

            if (template.getShiroResource() != null) {
                AnnotationSpec annotationSpec = AnnotationSpec.builder(QTemplate.ClassNameShiroRequiresPermissions).addMember("value", "$S", template.getShiroResource() + ":create").build();
                methodSpecBuilder.addAnnotation(annotationSpec);
            }
            {
                AnnotationSpec annotationSpec = AnnotationSpec.builder(QTemplate.ClassNameSpringGetMapping).addMember("value", "$S", "/create").build();
                methodSpecBuilder.addAnnotation(annotationSpec);
            }

            typeSpecBuilder.addMethod(methodSpecBuilder.build());
        }

        // add create post
        {
            ParameterSpec foParameterSpec = ParameterSpec.builder(template.getCreateFOClassName(), "fo")
                    .addAnnotation(QTemplate.ClassNameJavaxValid)
                    .build();

            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("create")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addParameter(foParameterSpec)
                    .addParameter(QTemplate.ClassNameSpringModelMap, "modelMap")
                    .addStatement("$T.Builder builder = $T.builder()", template.getBeanCreatorClassName(), template.getBeanCreatorClassName())
                    .addStatement("$T.copyPropertiesToCreatorBuilder(builder, $T.class, fo)", QTemplate.ClassNameQBeanCreatorHelper, template.getBeanCreatorClassName())
                    .addStatement("$N.save(builder.build())", serviceInstance)
                    .addStatement("return $S", "redirect:/" + template.getRequestMappingPrefix() + "/list");

            if (template.getShiroResource() != null) {
                AnnotationSpec annotationSpec = AnnotationSpec.builder(QTemplate.ClassNameShiroRequiresPermissions).addMember("value", "$S", template.getShiroResource() + ":create").build();
                methodSpecBuilder.addAnnotation(annotationSpec);
            }
            {
                AnnotationSpec annotationSpec = AnnotationSpec.builder(QTemplate.ClassNameSpringPostMapping).addMember("value", "$S", "/create").build();
                methodSpecBuilder.addAnnotation(annotationSpec);
            }

            typeSpecBuilder.addMethod(methodSpecBuilder.build());
        }

        // add update get
        {
            ParameterSpec idParameterSpec = ParameterSpec.builder(template.getIdClassName(), template.getIdName())
                    .addAnnotation(QTemplate.ClassNameJavaxValid)
                    .addAnnotation(QTemplate.ClassNameJavaxNotNull)
                    .addAnnotation(QTemplate.ClassNameSpringRequestParam)
                    .build();

            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("update")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addParameter(idParameterSpec)
                    .addParameter(QTemplate.ClassNameSpringModelMap, "modelMap")
                    .addStatement("$T $N = $N.getBy$NEnsure($N)", template.getBeanClassName(), beanInstance, serviceInstance, GenerateUtils.lowerCamelToUpperCamel(template.getIdName()), template.getIdName())
                    .addStatement("modelMap.addAttribute($S, $N)", beanInstance, beanInstance)
                    .addStatement("return $S", template.getTemplatePrefix() + "/update");

            if (template.getShiroResource() != null) {
                AnnotationSpec annotationSpec = AnnotationSpec.builder(QTemplate.ClassNameShiroRequiresPermissions).addMember("value", "$S", template.getShiroResource() + ":update").build();
                methodSpecBuilder.addAnnotation(annotationSpec);
            }
            {
                AnnotationSpec annotationSpec = AnnotationSpec.builder(QTemplate.ClassNameSpringGetMapping).addMember("value", "$S", "/update").build();
                methodSpecBuilder.addAnnotation(annotationSpec);
            }

            typeSpecBuilder.addMethod(methodSpecBuilder.build());
        }

        // add update post
        {
            ParameterSpec foParameterSpec = ParameterSpec.builder(template.getUpdateFOClassName(), "fo")
                    .addAnnotation(QTemplate.ClassNameJavaxValid)
                    .build();

            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("update")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addParameter(foParameterSpec)
                    .addParameter(QTemplate.ClassNameSpringModelMap, "modelMap")
                    .addStatement("$T.Builder builder = $T.builder(fo.get$N())", template.getBeanUpdaterClassName(), template.getBeanUpdaterClassName(), GenerateUtils.lowerCamelToUpperCamel(template.getIdName()))
                    .addStatement("$T.copyPropertiesToUpdateBuilder(builder, $T.class, fo)", QTemplate.ClassNameQBeanUpdaterHelper, template.getBeanUpdaterClassName())
                    .addStatement("$N.update(builder.build())", serviceInstance)
                    .addStatement("return $S", "redirect:/" + template.getRequestMappingPrefix() + "/list");

            if (template.getShiroResource() != null) {
                AnnotationSpec annotationSpec = AnnotationSpec.builder(QTemplate.ClassNameShiroRequiresPermissions).addMember("value", "$S", template.getShiroResource() + ":update").build();
                methodSpecBuilder.addAnnotation(annotationSpec);
            }
            {
                AnnotationSpec annotationSpec = AnnotationSpec.builder(QTemplate.ClassNameSpringPostMapping).addMember("value", "$S", "/update").build();
                methodSpecBuilder.addAnnotation(annotationSpec);
            }

            typeSpecBuilder.addMethod(methodSpecBuilder.build());
        }

        if (template.isControllerDelete()) {
            ParameterSpec idParameterSpec = ParameterSpec.builder(template.getIdClassName(), template.getIdName())
                    .addAnnotation(QTemplate.ClassNameJavaxValid)
                    .addAnnotation(QTemplate.ClassNameJavaxNotNull)
                    .addAnnotation(QTemplate.ClassNameSpringRequestParam)
                    .build();

            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("delete")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(QTemplate.ClassNameResponseFO)
                    .addParameter(idParameterSpec)
                    .addParameter(QTemplate.ClassNameSpringModelMap, "modelMap")
                    .addStatement("$T $N = $N.getBy$NEnsure($N)", template.getBeanClassName(), beanInstance, serviceInstance, GenerateUtils.lowerCamelToUpperCamel(template.getIdName()), template.getIdName())
                    .addStatement("$N.deleteBy$N($N.get$N())", serviceInstance, GenerateUtils.lowerCamelToUpperCamel(template.getIdName()), beanInstance, GenerateUtils.lowerCamelToUpperCamel(template.getIdName()))
                    .addStatement("return $T.ok()", QTemplate.ClassNameResponseFO);

            if (template.getShiroResource() != null) {
                AnnotationSpec annotationSpec = AnnotationSpec.builder(QTemplate.ClassNameShiroRequiresPermissions).addMember("value", "$S", template.getShiroResource() + ":delete").build();
                methodSpecBuilder.addAnnotation(annotationSpec);
            }
            {
                AnnotationSpec annotationSpec = AnnotationSpec.builder(QTemplate.ClassNameSpringGetMapping).addMember("value", "$S", "/delete").build();
                methodSpecBuilder.addAnnotation(annotationSpec);
            }

            typeSpecBuilder.addMethod(methodSpecBuilder.build());
        }

        JavaFile javaFile = JavaFile.builder(template.getControllerPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

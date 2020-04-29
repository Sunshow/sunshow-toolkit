package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.*;
import net.sunshow.code.generator.util.GenerateUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class QServiceGenerator {

    public static void generate(QTemplate template) throws Exception {
        TypeSpec.Builder typeSpecBuilder = TypeSpec.interfaceBuilder(template.getServiceName())
                .addModifiers(Modifier.PUBLIC);

        // 添加默认方法

        // 按ID获取
        {
            TypeName optionalTypeName = ParameterizedTypeName.get(ClassName.get(Optional.class), template.getBeanClassName());
            MethodSpec methodSpec = MethodSpec.methodBuilder("getBy" + GenerateUtils.lowerCamelToUpperCamel(template.getIdName()))
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(optionalTypeName)
                    .addParameter(template.getIdClassName(), template.getIdName())
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        // 按ID确认获取
        {
            MethodSpec methodSpec = MethodSpec.methodBuilder("getBy" + GenerateUtils.lowerCamelToUpperCamel(template.getIdName()) + "Ensure")
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(template.getBeanClassName())
                    .addParameter(template.getIdClassName(), template.getIdName())
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        // 按ID批量获取
        {
            TypeName listTypeName = ParameterizedTypeName.get(ClassName.get(List.class), template.getBeanClassName());
            TypeName idCollectionTypeName = ParameterizedTypeName.get(ClassName.get(Collection.class), template.getIdClassName());
            MethodSpec methodSpec = MethodSpec.methodBuilder("findBy" + GenerateUtils.lowerCamelToUpperCamel(template.getIdName()) + "Collection")
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(listTypeName)
                    .addParameter(idCollectionTypeName, template.getIdName() + "Collection")
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        // 新建
        {
            MethodSpec methodSpec = MethodSpec.methodBuilder("save")
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(template.getBeanClassName())
                    .addParameter(template.getBeanCreatorClassName(), "creator")
                    .addException(template.getExceptionClassName())
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        // 更新
        {
            MethodSpec methodSpec = MethodSpec.methodBuilder("update")
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(template.getBeanClassName())
                    .addParameter(template.getBeanUpdaterClassName(), "updater")
                    .addException(template.getExceptionClassName())
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        // 自定义分页查询
        {
            TypeName apiResponseTypeName = ParameterizedTypeName.get(QTemplate.ClassNameQResponse, template.getBeanClassName());

            MethodSpec methodSpec = MethodSpec.methodBuilder("findAll")
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(apiResponseTypeName)
                    .addParameter(QTemplate.ClassNameQRequest, "request")
                    .addParameter(QTemplate.ClassNameQPage, "requestPage")
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        JavaFile javaFile = JavaFile.builder(template.getServicePackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }
}

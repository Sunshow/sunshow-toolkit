package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.*;
import net.sunshow.code.generator.util.GenerateUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.util.Collection;
import java.util.List;

public class QRepositoryGenerator {

    public static void generate(QTemplate template) throws Exception {
        TypeName baseRepositoryTypeName = ParameterizedTypeName.get(QTemplate.ClassNameBaseRepository, template.getEntityClassName(), template.getIdClassName());

        TypeSpec.Builder typeSpecBuilder = TypeSpec.interfaceBuilder(template.getRepositoryName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(baseRepositoryTypeName);

        // 添加默认方法

        // 按ID批量获取
        {
            TypeName listTypeName = ParameterizedTypeName.get(ClassName.get(List.class), template.getEntityClassName());
            TypeName idCollectionTypeName = ParameterizedTypeName.get(ClassName.get(Collection.class), template.getIdClassName());
            MethodSpec methodSpec = MethodSpec.methodBuilder(String.format("findBy%sInOrderBy%sDesc", GenerateUtils.lowerCamelToUpperCamel(template.getIdName()), GenerateUtils.lowerCamelToUpperCamel(template.getIdName())))
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(listTypeName)
                    .addParameter(idCollectionTypeName, template.getIdName() + "Collection")
                    .build();
            typeSpecBuilder.addMethod(methodSpec);
        }

        JavaFile javaFile = JavaFile.builder(template.getRepositoryPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

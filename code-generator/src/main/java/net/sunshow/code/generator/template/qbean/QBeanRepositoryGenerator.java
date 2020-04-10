package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;

public class QBeanRepositoryGenerator {

    public static void generate(QBeanTemplate template) throws Exception {
        TypeName baseRepositoryTypeName = ParameterizedTypeName.get(QBeanTemplate.ClassNameBaseRepository, template.getEntityClassName(), template.getIdClassName());

        TypeSpec.Builder typeSpecBuilder = TypeSpec.interfaceBuilder(template.getRepositoryName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(baseRepositoryTypeName);

        JavaFile javaFile = JavaFile.builder(template.getRepositoryPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

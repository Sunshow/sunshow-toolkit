package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.io.File;

public class QBeanGenerator {

    public static void generate(QTemplate template) throws Exception {
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(template.getBeanName())
                .addModifiers(Modifier.PUBLIC)
                .superclass(QTemplate.ClassNameAbstractQBean);

        if (template.isLombok()) {
            typeSpecBuilder
                    .addAnnotation(QTemplate.ClassNameLombokGetter)
                    .addAnnotation(QTemplate.ClassNameLombokSetter);
        }

        typeSpecBuilder.addAnnotation(QTemplate.ClassNameQBean);

        if (template.isBeanCreator()) {
            typeSpecBuilder.addAnnotation(QTemplate.ClassNameQBeanCreator);
        }
        if (template.isBeanUpdater()) {
            typeSpecBuilder.addAnnotation(QTemplate.ClassNameQBeanUpdater);
        }

        // 添加ID
        if (StringUtils.isNotBlank(template.getIdName())) {
            FieldSpec.Builder builder = FieldSpec.builder(template.getIdClassName(), template.getIdName(), Modifier.PRIVATE)
                    .addAnnotation(QTemplate.ClassNameQBeanID)
                    .addAnnotation(QTemplate.ClassNameQBeanCreatorIgnore);
            typeSpecBuilder.addField(builder.build());
        }

        // 添加默认字段
        {
            FieldSpec.Builder builder = FieldSpec.builder(QTemplate.ClassNameLocalDateTime, QTemplate.FieldNameCreatedTime, Modifier.PRIVATE)
                    .addAnnotation(QTemplate.ClassNameQBeanUpdaterIgnore);
            typeSpecBuilder.addField(builder.build());
        }
        {
            FieldSpec.Builder builder = FieldSpec.builder(QTemplate.ClassNameLocalDateTime, QTemplate.FieldNameUpdatedTime, Modifier.PRIVATE)
                    .addAnnotation(QTemplate.ClassNameQBeanCreatorIgnore)
                    .addAnnotation(QTemplate.ClassNameQBeanUpdaterIgnore);
            typeSpecBuilder.addField(builder.build());
        }


        JavaFile javaFile = JavaFile.builder(template.getBeanPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

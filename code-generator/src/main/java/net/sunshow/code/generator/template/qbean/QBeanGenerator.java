package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import net.sunshow.code.generator.util.GenerateUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.io.File;

public class QBeanGenerator {

    public static void generate(QBeanTemplate template) throws Exception {
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(template.getBeanName())
                .addModifiers(Modifier.PUBLIC)
                .superclass(QBeanTemplate.ClassNameAbstractQBean);

        if (template.isLombok()) {
            typeSpecBuilder
                    .addAnnotation(QBeanTemplate.ClassNameLombokGetter)
                    .addAnnotation(QBeanTemplate.ClassNameLombokSetter);
        }

        typeSpecBuilder.addAnnotation(QBeanTemplate.ClassNameQBean);

        if (template.isBeanCreator()) {
            typeSpecBuilder.addAnnotation(QBeanTemplate.ClassNameQBeanCreator);
        }
        if (template.isBeanUpdater()) {
            typeSpecBuilder.addAnnotation(QBeanTemplate.ClassNameQBeanUpdater);
        }

        // 添加ID
        if (StringUtils.isNotBlank(template.getIdName())) {
            FieldSpec.Builder builder = FieldSpec.builder(template.getIdClassName(), template.getIdName(), Modifier.PRIVATE)
                    .addAnnotation(QBeanTemplate.ClassNameQBeanID)
                    .addAnnotation(QBeanTemplate.ClassNameQBeanCreatorIgnore);
            typeSpecBuilder.addField(builder.build());
        }

        // 添加默认字段
        {
            FieldSpec.Builder builder = FieldSpec.builder(QBeanTemplate.ClassNameLocalDateTime, QBeanTemplate.FieldNameCreatedTime, Modifier.PRIVATE)
                    .addAnnotation(QBeanTemplate.ClassNameQBeanUpdaterIgnore);
            typeSpecBuilder.addField(builder.build());
        }
        {
            FieldSpec.Builder builder = FieldSpec.builder(QBeanTemplate.ClassNameLocalDateTime, QBeanTemplate.FieldNameUpdatedTime, Modifier.PRIVATE)
                    .addAnnotation(QBeanTemplate.ClassNameQBeanCreatorIgnore)
                    .addAnnotation(QBeanTemplate.ClassNameQBeanUpdaterIgnore);
            typeSpecBuilder.addField(builder.build());
        }


        JavaFile javaFile = JavaFile.builder(template.getBeanPackagePath(), typeSpecBuilder.build()).indent(GenerateUtils.INTENT).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

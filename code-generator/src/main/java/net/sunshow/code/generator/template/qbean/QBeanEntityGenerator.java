package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import net.sunshow.code.generator.util.GenerateUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.io.File;

public class QBeanEntityGenerator {

    public static void generate(QBeanTemplate template) throws Exception {
        AnnotationSpec tableAnnotationSpec = AnnotationSpec.builder(QBeanTemplate.ClassNameJpaTable).addMember("name", "$S", template.getEntityTableName()).build();
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(template.getEntityName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(QBeanTemplate.ClassNameBaseEntity)
                .addAnnotation(tableAnnotationSpec)
                .addAnnotation(QBeanTemplate.ClassNameJpaEntity)
                .addAnnotation(QBeanTemplate.ClassNameHibernateDynamicInsert)
                .addAnnotation(QBeanTemplate.ClassNameHibernateDynamicUpdate);

        if (template.isLombok()) {
            typeSpecBuilder
                    .addAnnotation(QBeanTemplate.ClassNameLombokGetter)
                    .addAnnotation(QBeanTemplate.ClassNameLombokSetter);
        }

        // 添加ID
        if (StringUtils.isNotBlank(template.getIdName())) {
            AnnotationSpec generatedValueAnnotationSpec = AnnotationSpec.builder(QBeanTemplate.ClassNameJpaGeneratedValue)
                    .addMember("strategy", "$T.IDENTITY", QBeanTemplate.ClassNameJpaGenerationType).build();
            FieldSpec.Builder builder = FieldSpec.builder(template.getIdClassName(), template.getIdName(), Modifier.PRIVATE)
                    .addAnnotation(QBeanTemplate.ClassNameJpaId)
                    .addAnnotation(generatedValueAnnotationSpec);
            typeSpecBuilder.addField(builder.build());
        }

        // 添加默认字段
        {
            AnnotationSpec columnAnnotationSpec = AnnotationSpec.builder(QBeanTemplate.ClassNameJpaColumn)
                    .addMember("name", "$S", GenerateUtils.lowerCamelToLowerUnderScore(QBeanTemplate.FieldNameCreatedTime))
                    .addMember("nullable", "false")
                    .addMember("updatable", "false")
                    .build();
            FieldSpec.Builder builder = FieldSpec.builder(QBeanTemplate.ClassNameLocalDateTime, QBeanTemplate.FieldNameCreatedTime, Modifier.PRIVATE)
                    .addAnnotation(columnAnnotationSpec);
            typeSpecBuilder.addField(builder.build());
        }
        {
            AnnotationSpec columnAnnotationSpec = AnnotationSpec.builder(QBeanTemplate.ClassNameJpaColumn)
                    .addMember("name", "$S", GenerateUtils.lowerCamelToLowerUnderScore(QBeanTemplate.FieldNameUpdatedTime))
                    .addMember("nullable", "false")
                    .build();
            FieldSpec.Builder builder = FieldSpec.builder(QBeanTemplate.ClassNameLocalDateTime, QBeanTemplate.FieldNameUpdatedTime, Modifier.PRIVATE)
                    .addAnnotation(columnAnnotationSpec);
            typeSpecBuilder.addField(builder.build());
        }

        JavaFile javaFile = JavaFile.builder(template.getEntityPackagePath(), typeSpecBuilder.build()).indent(GenerateUtils.INTENT).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

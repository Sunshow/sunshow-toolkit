package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.*;
import net.sunshow.code.generator.util.GenerateUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.io.File;

public class QEntityGenerator {

    public static void generate(QTemplate template) throws Exception {
        AnnotationSpec tableAnnotationSpec = AnnotationSpec.builder(QTemplate.ClassNameJpaTable).addMember("name", "$S", template.getEntityTableName()).build();
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(template.getEntityName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(QTemplate.ClassNameBaseEntity)
                .addAnnotation(tableAnnotationSpec)
                .addAnnotation(QTemplate.ClassNameJpaEntity)
                .addAnnotation(QTemplate.ClassNameHibernateDynamicInsert)
                .addAnnotation(QTemplate.ClassNameHibernateDynamicUpdate);

        if (template.isLombok()) {
            typeSpecBuilder
                    .addAnnotation(QTemplate.ClassNameLombokGetter)
                    .addAnnotation(QTemplate.ClassNameLombokSetter);
        }

        // 添加ID
        if (StringUtils.isNotBlank(template.getIdName())) {
            AnnotationSpec generatedValueAnnotationSpec = AnnotationSpec.builder(QTemplate.ClassNameJpaGeneratedValue)
                    .addMember("strategy", "$T.IDENTITY", QTemplate.ClassNameJpaGenerationType).build();
            FieldSpec.Builder builder = FieldSpec.builder(template.getIdClassName(), template.getIdName(), Modifier.PRIVATE)
                    .addAnnotation(QTemplate.ClassNameJpaId)
                    .addAnnotation(generatedValueAnnotationSpec);
            typeSpecBuilder.addField(builder.build());
        }

        // 添加默认字段
        {
            AnnotationSpec columnAnnotationSpec = AnnotationSpec.builder(QTemplate.ClassNameJpaColumn)
                    .addMember("name", "$S", GenerateUtils.lowerCamelToLowerUnderScore(QTemplate.FieldNameCreatedTime))
                    .addMember("nullable", "false")
                    .addMember("updatable", "false")
                    .build();
            FieldSpec.Builder builder = FieldSpec.builder(QTemplate.ClassNameLocalDateTime, QTemplate.FieldNameCreatedTime, Modifier.PRIVATE)
                    .addAnnotation(columnAnnotationSpec);
            typeSpecBuilder.addField(builder.build());
        }
        {
            AnnotationSpec columnAnnotationSpec = AnnotationSpec.builder(QTemplate.ClassNameJpaColumn)
                    .addMember("name", "$S", GenerateUtils.lowerCamelToLowerUnderScore(QTemplate.FieldNameUpdatedTime))
                    .addMember("nullable", "false")
                    .build();
            FieldSpec.Builder builder = FieldSpec.builder(QTemplate.ClassNameLocalDateTime, QTemplate.FieldNameUpdatedTime, Modifier.PRIVATE)
                    .addAnnotation(columnAnnotationSpec);
            typeSpecBuilder.addField(builder.build());
        }

        // 添加默认方法
        {
            MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreate")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(QTemplate.ClassNameJpaPrePersist)
                    .beginControlFlow("if (this.$L() == null)", GenerateUtils.lowerCamelToGetter(QTemplate.FieldNameCreatedTime))
                    .addStatement("$L = LocalDateTime.now()", QTemplate.FieldNameCreatedTime)
                    .endControlFlow()
                    .beginControlFlow("if (this.$L() == null)", GenerateUtils.lowerCamelToGetter(QTemplate.FieldNameUpdatedTime))
                    .addStatement("$L = LocalDateTime.now()", QTemplate.FieldNameUpdatedTime)
                    .endControlFlow();
            typeSpecBuilder.addMethod(builder.build());
        }
        {
            MethodSpec.Builder builder = MethodSpec.methodBuilder("onUpdate")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(QTemplate.ClassNameJpaPreUpdate)
                    .addStatement("$L = LocalDateTime.now()", QTemplate.FieldNameUpdatedTime);
            typeSpecBuilder.addMethod(builder.build());
        }

        JavaFile javaFile = JavaFile.builder(template.getEntityPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

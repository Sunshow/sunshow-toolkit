package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.*;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaSource;
import net.sunshow.code.generator.util.GenerateUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileReader;

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

        // 是否支持软删除
        if (template.isSoftDelete()) {
            AnnotationSpec whereAnnotationSpec = AnnotationSpec.builder(QTemplate.ClassNameHibernateWhere)
                    .addMember("clause", "$N = 0", GenerateUtils.lowerCamelToLowerUnderScore(QTemplate.FieldNameDeletedTime)).build();
            typeSpecBuilder.addAnnotation(whereAnnotationSpec);
        }

        if (template.isLombok()) {
            typeSpecBuilder
                    .addAnnotation(QTemplate.ClassNameLombokGetter)
                    .addAnnotation(QTemplate.ClassNameLombokSetter);
        }

        // 添加ID
        {
            AnnotationSpec generatedValueAnnotationSpec = AnnotationSpec.builder(QTemplate.ClassNameJpaGeneratedValue)
                    .addMember("strategy", "$T.IDENTITY", QTemplate.ClassNameJpaGenerationType).build();
            FieldSpec.Builder builder = FieldSpec.builder(template.getIdClassName(), template.getIdName(), Modifier.PRIVATE)
                    .addAnnotation(QTemplate.ClassNameJpaId)
                    .addAnnotation(generatedValueAnnotationSpec);
            typeSpecBuilder.addField(builder.build());
        }

        // 根据 QBean 属性生成代码
        JavaSource src = new JavaProjectBuilder().addSource(new FileReader(
                String.format("%s/%s.java", GenerateUtils.packageNameToPath(new File(template.getOutputPath()).toPath(), template.getBeanPackagePath()), template.getBeanName())));

        JavaClass beanClass = src.getClasses().get(0);
        for (JavaField field : beanClass.getFields()) {
            // 只处理 private 非 static
            if (!field.isPrivate() || field.isStatic()) {
                continue;
            }

            String fieldName = field.getName();
            // 跳过已经做了默认处理的字段
            if (fieldName.equals(template.getIdName()) ||
                    fieldName.equals(QTemplate.FieldNameCreatedTime) ||
                    fieldName.equals(QTemplate.FieldNameUpdatedTime)) {
                continue;
            }

            // 添加属性
            JavaClass fieldType = field.getType();
            FieldSpec.Builder builder = FieldSpec.builder(ClassName.get(fieldType.getPackageName(), fieldType.getName()), fieldName, Modifier.PRIVATE);
            // 如果有驼峰 加入 Column 注解指定字段名
            if (fieldName.chars().anyMatch(Character::isUpperCase)) {
                AnnotationSpec columnAnnotationSpec = AnnotationSpec.builder(QTemplate.ClassNameJpaColumn)
                        .addMember("name", "$S", GenerateUtils.lowerCamelToLowerUnderScore(fieldName))
                        .build();
                builder.addAnnotation(columnAnnotationSpec);
            }

            typeSpecBuilder.addField(builder.build());
        }

        // 添加默认字段
        if (template.isSoftDelete()) {
            AnnotationSpec columnAnnotationSpec = AnnotationSpec.builder(QTemplate.ClassNameJpaColumn)
                    .addMember("name", "$S", GenerateUtils.lowerCamelToLowerUnderScore(QTemplate.FieldNameDeletedTime))
                    .build();
            FieldSpec.Builder builder = FieldSpec.builder(Long.class, QTemplate.FieldNameDeletedTime, Modifier.PRIVATE)
                    .addAnnotation(columnAnnotationSpec);
            typeSpecBuilder.addField(builder.build());
        }
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

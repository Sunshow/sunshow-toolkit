package net.sunshow.code.generator.template.qbean;

import com.google.common.base.MoreObjects;
import com.squareup.javapoet.*;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaSource;
import net.sunshow.code.generator.util.GenerateUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileReader;

public class QCreateFOGenerator {

    public static void generate(QTemplate template) throws Exception {
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(template.getCreateFOName())
                .addModifiers(Modifier.PUBLIC);
        if (template.isLombok()) {
            typeSpecBuilder
                    .addAnnotation(QTemplate.ClassNameLombokData);
        }

        // 解析 QBean 生成
        JavaProjectBuilder builder = new JavaProjectBuilder();
        JavaSource src = builder.addSource(new FileReader(
                String.format("%s/%s.java", GenerateUtils.packageNameToPath(new File(template.getOutputPath()).toPath(), template.getBeanPackagePath()), template.getBeanName())));

        JavaClass beanClass = src.getClasses().get(0);
        for (JavaField field : beanClass.getFields()) {
            // 只处理 private 非 static
            if (!field.isPrivate() || field.isStatic()) {
                continue;
            }
            // 忽略掉 QBeanCreatorIgnore
            boolean ignore = false;
            for (JavaAnnotation annotation : field.getAnnotations()) {
                JavaClass annotationType = annotation.getType();
                if (annotationType.getName().equals(QTemplate.ClassNameQBeanCreatorIgnore.simpleName())) {
                    ignore = true;
                    break;
                }
            }
            if (ignore) {
                continue;
            }
            // 添加 Field
            JavaClass fieldType = field.getType();

            // 默认添加 NotNull 验证, String 添加 NotBlank 验证
            ClassName validateClassName = QTemplate.ClassNameJavaxNotNull;
            if (fieldType.getName().equals("String")) {
                validateClassName = QTemplate.ClassNameJavaxNotBlank;
            }
            AnnotationSpec.Builder fieldAnnotationSpecBuilder = AnnotationSpec.builder(validateClassName);
            fieldAnnotationSpecBuilder.addMember("message", "$S", String.format("%s不能为空", MoreObjects.firstNonNull(field.getComment(), field.getName())));
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(ClassName.get(fieldType.getPackageName(), fieldType.getName()), field.getName(), Modifier.PRIVATE)
                    .addAnnotation(fieldAnnotationSpecBuilder.build());
            typeSpecBuilder.addField(fieldBuilder.build());
        }

        JavaFile javaFile = JavaFile.builder(template.getCreateFOPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

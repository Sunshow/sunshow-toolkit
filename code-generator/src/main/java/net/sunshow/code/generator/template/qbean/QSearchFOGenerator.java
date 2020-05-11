package net.sunshow.code.generator.template.qbean;

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

public class QSearchFOGenerator {

    public static void generate(QTemplate template) throws Exception {
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(template.getSearchFOName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(QTemplate.ClassNameLombokData)
                .addAnnotation(QTemplate.ClassNameQField);

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

            AnnotationSpec.Builder fieldAnnotationSpecBuilder = AnnotationSpec.builder(QTemplate.ClassNameQField);

            boolean sortable = false;
            // 创建时间和更新时间默认允许排序
            if (QTemplate.FieldNameCreatedTime.equals(field.getName())) {
                sortable = true;
                fieldAnnotationSpecBuilder.addMember("control", "$T.DATETIME", QTemplate.ClassNameQFieldControl);
            } else if (QTemplate.FieldNameUpdatedTime.equals(field.getName())) {
                fieldAnnotationSpecBuilder.addMember("control", "$T.DATETIME", QTemplate.ClassNameQFieldControl);
                sortable = true;
            } else {
                // ID 自动允许排序
                for (JavaAnnotation annotation : field.getAnnotations()) {
                    JavaClass annotationType = annotation.getType();
                    if (annotationType.getName().equals(QTemplate.ClassNameQBeanID.simpleName())) {
                        sortable = true;
                        break;
                    }
                }
            }
            if (sortable) {
                fieldAnnotationSpecBuilder.addMember("sortable", "true");
            }

            // 从评论中读取 placeholder
            String comment = field.getComment();
            if (comment == null || comment.isEmpty()) {
                if (field.getName().equalsIgnoreCase(template.getIdName())) {
                    comment = "ID";
                } else if (field.getName().equalsIgnoreCase(QTemplate.FieldNameCreatedTime)) {
                    comment = "创建时间";
                } else if (field.getName().equalsIgnoreCase(QTemplate.FieldNameUpdatedTime)) {
                    comment = "更新时间";
                } else {
                    comment = GenerateUtils.lowerCamelToUpperCamel(field.getName());
                }
            }
            fieldAnnotationSpecBuilder.addMember("placeholder", "$S", comment);

            // 添加 Field
            JavaClass fieldType = field.getType();
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(ClassName.get(fieldType.getPackageName(), fieldType.getName()), field.getName(), Modifier.PRIVATE)
                    .addAnnotation(fieldAnnotationSpecBuilder.build());
            typeSpecBuilder.addField(fieldBuilder.build());
        }

        JavaFile javaFile = JavaFile.builder(template.getUpdateFOPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

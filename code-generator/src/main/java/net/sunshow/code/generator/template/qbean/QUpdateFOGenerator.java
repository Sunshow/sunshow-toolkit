package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaSource;
import net.sunshow.code.generator.util.GenerateUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileReader;

public class QUpdateFOGenerator {

    public static void generate(QTemplate template) throws Exception {
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(template.getUpdateFOName())
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
            // 忽略掉 QBeanUpdaterIgnore
            boolean ignore = false;
            for (JavaAnnotation annotation : field.getAnnotations()) {
                JavaClass annotationType = annotation.getType();
                if (annotationType.getName().equals(QTemplate.ClassNameQBeanUpdaterIgnore.simpleName())) {
                    ignore = true;
                    break;
                }
            }
            if (ignore) {
                continue;
            }
            // 添加 Field
            JavaClass fieldType = field.getType();
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(ClassName.get(fieldType.getPackageName(), fieldType.getName()), field.getName(), Modifier.PRIVATE);
            typeSpecBuilder.addField(fieldBuilder.build());
        }

        JavaFile javaFile = JavaFile.builder(template.getUpdateFOPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

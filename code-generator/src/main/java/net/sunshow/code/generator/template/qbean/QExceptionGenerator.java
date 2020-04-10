package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;

public class QExceptionGenerator {

    public static void generate(QTemplate template) throws Exception {
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(template.getExceptionName())
                .addModifiers(Modifier.PUBLIC)
                .superclass(QTemplate.ClassNameRuntimeException);

        // 添加构造函数
        {
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("this($S)", template.getExceptionDefaultMessage())
                    .build();
            typeSpecBuilder.addMethod(constructor);
        }
        {
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(String.class, "message")
                    .addStatement("super(message)")
                    .build();
            typeSpecBuilder.addMethod(constructor);
        }
        {
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Throwable.class, "cause")
                    .addStatement("super(cause)")
                    .build();
            typeSpecBuilder.addMethod(constructor);
        }
        {
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(String.class, "message")
                    .addParameter(Throwable.class, "cause")
                    .addStatement("super(message, cause)")
                    .build();
            typeSpecBuilder.addMethod(constructor);
        }

        JavaFile javaFile = JavaFile.builder(template.getExceptionPackagePath(), typeSpecBuilder.build()).indent(template.getIndent()).skipJavaLangImports(true).build();

        javaFile.writeTo(new File(template.getOutputPath()));
    }

}

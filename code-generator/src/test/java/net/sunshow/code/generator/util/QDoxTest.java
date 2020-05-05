package net.sunshow.code.generator.util;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaSource;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.util.List;

/**
 * author: sunshow.
 */
public class QDoxTest {

    @Test
    public void testReadFieldList() throws Exception {
        JavaProjectBuilder builder = new JavaProjectBuilder();
        JavaSource src = builder.addSource(new FileReader("/Users/sunshow/GIT/toolkit/code-generator/src/test/java/net/sunshow/code/generator/util/TestParser.java"));

        List<JavaClass> javaClassList = src.getClasses();
        for (JavaClass javaClass : javaClassList) {
            System.out.println(javaClass.getName());
            List<JavaAnnotation> javaAnnotationList = javaClass.getAnnotations();
            for (JavaAnnotation javaAnnotation : javaAnnotationList) {
                System.out.println(javaAnnotation.getType().getPackageName() + "." + javaAnnotation.getType().getName());
            }
            List<JavaField> javaFieldList = javaClass.getFields();
            for (JavaField javaField : javaFieldList) {
                System.out.println(javaField.getName());
                System.out.println(javaField.isPrivate());
                System.out.println(javaField.isStatic());
                System.out.println(javaField.getComment());
            }
        }
    }
}

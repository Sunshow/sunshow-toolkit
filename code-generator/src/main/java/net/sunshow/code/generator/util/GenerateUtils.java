package net.sunshow.code.generator.util;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;

public class GenerateUtils {
    public final static String INTENT = "    ";

    public static MethodSpec.Builder createGetterBuilder(TypeName typeName, String fieldName, Modifier visibility) {
        String prefix = "get";
        if (typeName.equals(TypeName.get(boolean.class))) {
            prefix = "is";
        }

        return MethodSpec.methodBuilder(prefix + lowerCamelToUpperCamel(fieldName))
                .addModifiers(visibility)
                .addStatement("return this.$N", fieldName)
                .returns(typeName);
    }

    public static MethodSpec createGetter(TypeName typeName, String fieldName, Modifier visibility) {
        return createGetterBuilder(typeName, fieldName, visibility).build();
    }

    public static MethodSpec.Builder createSetterBuilder(TypeName typeName, String fieldName, Modifier visibility) {
        String prefix = "set";

        return MethodSpec.methodBuilder(prefix + lowerCamelToUpperCamel(fieldName))
                .addModifiers(visibility)
                .addParameter(typeName, fieldName)
                .addStatement("this.$N = $N", fieldName, fieldName);
    }

    public static MethodSpec createSetter(TypeName typeName, String fieldName, Modifier visibility) {
        return createSetterBuilder(typeName, fieldName, visibility).build();
    }

    public static String lowerCamelToLowerUnderScore(String upperCamel) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, upperCamel);
    }

    public static String upperCamelToLowerUnderScore(String upperCamel) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, upperCamel);
    }

    public static String upperCamelToLowerCamel(String upperCamel) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, upperCamel);
    }

    public static String lowerCamelToUpperCamel(String lowerCamel) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, lowerCamel);
    }

    public static String lowerCamelToGetter(String lowerCamel) {
        return "get" + lowerCamelToUpperCamel(lowerCamel);
    }

    public static String combinePackagePath(String basePackagePath, String subPackage, String moduleName) {
        if (StringUtils.isBlank(moduleName)) {
            return Joiner.on('.').join(basePackagePath, subPackage);
        } else {
            return Joiner.on('.').join(basePackagePath, subPackage, moduleName);
        }
    }
}

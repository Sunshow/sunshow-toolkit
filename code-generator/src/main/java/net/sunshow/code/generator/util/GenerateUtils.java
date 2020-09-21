package net.sunshow.code.generator.util;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class GenerateUtils {

    private static Set<String> baseClassSimpleName = new HashSet<>();

    static {
        baseClassSimpleName.add("String");
        baseClassSimpleName.add("Long");
        baseClassSimpleName.add("Integer");
        baseClassSimpleName.add("Boolean");
        baseClassSimpleName.add("long");
        baseClassSimpleName.add("int");
        baseClassSimpleName.add("boolean");
    }

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

    public static String lowerUnderScoreToUpperCamel(String upperCamel) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, upperCamel);
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

    public static Path packageNameToPath(Path directory, String packageName) {
        Path outputDirectory = directory;
        if (!packageName.isEmpty()) {
            for (String packageComponent : packageName.split("\\.")) {
                outputDirectory = outputDirectory.resolve(packageComponent);
            }
        }
        return outputDirectory;
    }

    public static boolean classTypeWildEquals(String type1, String type2) {
        return StringUtils.equals(classSimpleName(type1), classSimpleName(type2));
    }

    public static String classSimpleName(String className) {
        if (StringUtils.contains(className, ".")) {
            return StringUtils.substringAfterLast(className, ".");
        }
        return className;
    }

    public static boolean isBaseClassType(String className) {
        return baseClassSimpleName.contains(classSimpleName(className));
    }

    public static boolean isCustomEnumClassType(String className) {
        String simpleName = classSimpleName(className);
        return simpleName.endsWith("Type") || simpleName.endsWith("Status");
    }
}

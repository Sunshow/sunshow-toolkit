package net.sunshow.code.generator.template.openapi.retrofit1;

import com.squareup.javapoet.ClassName;
import net.sunshow.code.generator.util.GenerateUtils;

public class Retrofit1Utils {

    public static ClassName getRequestClassName(Retrofit1Template template, String namePrefix) {
        return ClassName.get(template.getRequestPackagePath(), namePrefix + GenerateUtils.lowerCamelToUpperCamel(template.getRequestSuffix()));
    }

    public static ClassName getResponseClassName(Retrofit1Template template, String namePrefix) {
        return ClassName.get(template.getResponsePackagePath(), namePrefix + GenerateUtils.lowerCamelToUpperCamel(template.getResponseSuffix()));
    }
    
}

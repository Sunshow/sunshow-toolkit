package net.sunshow.code.generator.template.openapi.retrofit1;

import com.squareup.javapoet.ClassName;
import lombok.Getter;
import lombok.Setter;
import net.sunshow.code.generator.template.openapi.EndpointDef;
import net.sunshow.code.generator.util.GenerateUtils;

@Setter
@Getter
public class Retrofit1Template {

    public static final ClassName ClassNameRetrofitPost = ClassName.get("retrofit2.http", "POST");
    public static final ClassName ClassNameRetrofitBody = ClassName.get("retrofit2.http", "Body");
    public static final ClassName ClassNameRetrofitCall = ClassName.get("retrofit2", "Call");

    public void init(EndpointDef def) {
        if (this.isModuleNamePrefix()) {
            this.setNamePrefix(GenerateUtils.lowerCamelToUpperCamel(def.getSubModule()) + GenerateUtils.lowerCamelToUpperCamel(def.getModule()) + def.getName());
        } else {
            this.setNamePrefix(GenerateUtils.lowerCamelToUpperCamel(def.getSubModule()) + def.getName());
        }
        if (this.isModulePrefixReverse()) {
            this.setModulePrefix(GenerateUtils.lowerCamelToUpperCamel(def.getSubModule()) + GenerateUtils.lowerCamelToUpperCamel(def.getModule()));
        } else {
            this.setModulePrefix(GenerateUtils.lowerCamelToUpperCamel(def.getModule()) + GenerateUtils.lowerCamelToUpperCamel(def.getSubModule()));
        }
    }

    private String namePrefix;

    private String modulePrefix;

    private String indent = "    ";

    // 基础包路径, 生成代码的上级包路径, 在此路径下分包输出
    private String packagePathPrefix;

    // 模块名称, 生成代码都放在各自包下的子模块包下
    private String moduleName;

    private boolean moduleNamePrefix = false;

    // 模块前缀倒装, 即子模块名在前
    private boolean modulePrefixReverse = false;

    // 输出路径
    private String outputPath = "";

    private String requestSuffix = "request";

    private String responseSuffix = "response";

    private String endpointSuffix = "api";

    private String foSuffix = "FO";

    private String controllerSuffix = "Controller";

    private ClassName responseWrapperClassName = ClassName.get(packagePathPrefix + ".api.response", "NSixResponseWrapper");

    private ClassName responseHelperClassName = ClassName.get(packagePathPrefix + ".component.nsix", "NSixResponseHelper");

    private ClassName controllerRespFOClassName = ClassName.get("net.sunshow.cms.module.common.fo", "RestResponseFO");

    private String foIgnoreSessionProperty = "employeeId";

    public String getApiPackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, "api", "");
    }

    public String getRequestPackagePath() {
        return GenerateUtils.combinePackagePath(getApiPackagePath(), requestSuffix, moduleName);
    }

    public String getResponsePackagePath() {
        return GenerateUtils.combinePackagePath(getApiPackagePath(), responseSuffix, moduleName);
    }

    public String getEndpointPackagePath() {
        return GenerateUtils.combinePackagePath(getApiPackagePath(), "endpoint", moduleName);
    }

    public String getFOPackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, "fo", moduleName);
    }

    public String getControllerPackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, "controller", moduleName);
    }

    public String getFOSuffix() {
        return foSuffix;
    }
}

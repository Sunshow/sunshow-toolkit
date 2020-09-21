package net.sunshow.code.generator.template.openapi.retrofit1;

import com.squareup.javapoet.ClassName;
import lombok.Getter;
import lombok.Setter;
import net.sunshow.code.generator.util.GenerateUtils;

@Setter
@Getter
public class Retrofit1Template {

    public static final ClassName ClassNameRetrofitPost = ClassName.get("retrofit2.http", "POST");
    public static final ClassName ClassNameRetrofitBody = ClassName.get("retrofit2.http", "Body");
    public static final ClassName ClassNameRetrofitCall = ClassName.get("retrofit2", "Call");

    private String indent = "    ";

    // 基础包路径, 生成代码的上级包路径, 在此路径下分包输出
    private String packagePathPrefix;

    // 模块名称, 生成代码都放在各自包下的子模块包下
    private String moduleName;

    // 输出路径
    private String outputPath = "";

    private String requestSuffix = "request";

    private String responseSuffix = "response";

    private String endpointSuffix = "api";

    private String foSuffix = "FO";

    private String controllerSuffix = "Controller";

    private ClassName responseWrapperClassName = ClassName.get("tech.xiaoman.nplus6.merchant.api.response", "NSixResponseWrapper");

    private ClassName responseHelperClassName = ClassName.get("tech.xiaoman.nplus6.merchant.component.nsix", "NSixResponseHelper");

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

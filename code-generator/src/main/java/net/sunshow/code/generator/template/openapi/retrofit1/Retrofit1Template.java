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

    private String apiSuffix = "api";

    private ClassName responseWrapperClassName = ClassName.get("tech.xiaoman.nplus6.merchant.api.response", "NSixResponseWrapper");

    public String getRequestPackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, requestSuffix, moduleName);
    }

    public String getResponsePackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, responseSuffix, moduleName);
    }

    public String getApiPackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, "endpoint", "");
    }
}

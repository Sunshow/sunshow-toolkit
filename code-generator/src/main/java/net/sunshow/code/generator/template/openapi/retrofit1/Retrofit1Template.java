package net.sunshow.code.generator.template.openapi.retrofit1;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import lombok.Getter;
import lombok.Setter;
import net.sunshow.code.generator.template.openapi.EndpointDef;
import net.sunshow.code.generator.template.openapi.EndpointMethodDef;
import net.sunshow.code.generator.template.openapi.OpenApiParser;
import net.sunshow.code.generator.util.GenerateUtils;

import java.util.Set;

@Setter
@Getter
public class Retrofit1Template {

    public static final ClassName ClassNameRetrofitPost = ClassName.get("retrofit2.http", "POST");
    public static final ClassName ClassNameRetrofitBody = ClassName.get("retrofit2.http", "Body");
    public static final ClassName ClassNameRetrofitCall = ClassName.get("retrofit2", "Call");

    public void init(EndpointDef def) {
        init(def, null);
    }

    public void init(EndpointDef def, OpenApiParser parser) {
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

        EndpointMethodDef methodDef = def.getMethodDefList().get(0);
        if (methodDef.getRequestSchemaRef() != null && parser != null) {
            {
                ObjectNode schemaNode = (ObjectNode) parser.getSchemas().get(methodDef.getRequestSchemaRef());
                if (schemaNode.has("properties")) {
                    // 判断是否是分页请求
                    ObjectNode propertyNode = (ObjectNode) schemaNode.get("properties");
                    propertyNode.fieldNames().forEachRemaining(f -> {
                        if (pageableRequestProperties.contains(f)) {
                            def.setPageable(true);
                        } else {
                            def.setPageableRequestHasExtraProperties(true);
                        }
                    });
                }
            }

            {
                ObjectNode schemaNode = (ObjectNode) parser.getSchemas().get(methodDef.getResponseSchemaRef());
                if (schemaNode.has("properties")) {
                    // 判断是否是分页请求
                    ObjectNode propertyNode = (ObjectNode) schemaNode.get("properties");
                    propertyNode.fieldNames().forEachRemaining(f -> {
                        if (pageableResponseProperties.contains(f)) {
                            def.setPageable(true);
                        } else {
                            def.setPageableResponseHasExtraProperties(true);
                        }
                        if (propertyNode.get(f).get("type").asText().equals("array")) {
                            def.setPageableListProperty(f);
                        }
                    });
                }
            }
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

    private String respFOSuffix = "RespFO";

    private String controllerSuffix = "Controller";

    private ClassName responseWrapperClassName;

    private ClassName responseHelperClassName;

    private ClassName controllerRespFOClassName;

    private String foIgnoreSessionProperty = "employeeId";

    private Set<String> pageableRequestProperties = Sets.newHashSet("pageIndex", "pageSize");

    private Set<String> pageableResponseProperties = Sets.newHashSet("currentPageIndex", "pageSize", "totalPageCount", "totalCount");

    private ClassName pageableRequestClassName;

    private ClassName pageableResponseClassName;

    private ClassName limitFOClassName;

    private ClassName limitRespFOClassName;

    public ClassName getResponseWrapperClassName() {
        if (responseWrapperClassName == null) {
            responseWrapperClassName = ClassName.get(getPackagePathPrefix() + ".api.response", "NSixResponseWrapper");
        }
        return responseWrapperClassName;
    }

    public ClassName getResponseHelperClassName() {
        if (responseHelperClassName == null) {
            responseHelperClassName = ClassName.get(getPackagePathPrefix() + ".component.nsix", "NSixResponseHelper");
        }
        return responseHelperClassName;
    }

    public ClassName getControllerRespFOClassName() {
        if (controllerRespFOClassName == null) {
            controllerRespFOClassName = ClassName.get("net.sunshow.cms.module.common.fo", "RestResponseFO");
        }
        return controllerRespFOClassName;
    }

    public ClassName getPageableRequestClassName() {
        if (pageableRequestClassName == null) {
            pageableRequestClassName = ClassName.get(getPackagePathPrefix() + ".api.request", "PageableRequest");
        }
        return pageableRequestClassName;
    }

    public ClassName getPageableResponseClassName() {
        if (pageableResponseClassName == null) {
            pageableResponseClassName = ClassName.get(getPackagePathPrefix() + ".api.response", "PageableResponse");
        }
        return pageableResponseClassName;
    }

    public ClassName getLimitFOClassName() {
        if (limitFOClassName == null) {
            limitFOClassName = ClassName.get(getPackagePathPrefix() + ".fo", "LimitFO");
        }
        return limitFOClassName;
    }

    public ClassName getLimitRespFOClassName() {
        if (limitRespFOClassName == null) {
            limitRespFOClassName = ClassName.get(getPackagePathPrefix() + ".fo", "LimitRespFO");
        }
        return limitRespFOClassName;
    }

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

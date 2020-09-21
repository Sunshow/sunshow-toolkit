package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.ClassName;
import lombok.Getter;
import lombok.Setter;
import net.sunshow.code.generator.util.GenerateUtils;

@Setter
@Getter
public class QTemplate {

    public static final ClassName ClassNameString = ClassName.get("java.lang", "String");
    public static final ClassName ClassNameRuntimeException = ClassName.get("java.lang", "RuntimeException");
    public static final ClassName ClassNameLocalDateTime = ClassName.get("java.time", "LocalDateTime");
    public static final ClassName ClassNameSupplier = ClassName.get("java.util.function", "Supplier");

    public static final ClassName ClassNameJpaTable = ClassName.get("javax.persistence", "Table");
    public static final ClassName ClassNameJpaEntity = ClassName.get("javax.persistence", "Entity");
    public static final ClassName ClassNameJpaId = ClassName.get("javax.persistence", "Id");
    public static final ClassName ClassNameJpaColumn = ClassName.get("javax.persistence", "Column");
    public static final ClassName ClassNameJpaGeneratedValue = ClassName.get("javax.persistence", "GeneratedValue");
    public static final ClassName ClassNameJpaGenerationType = ClassName.get("javax.persistence", "GenerationType");
    public static final ClassName ClassNameJpaPrePersist = ClassName.get("javax.persistence", "PrePersist");
    public static final ClassName ClassNameJpaPreUpdate = ClassName.get("javax.persistence", "PreUpdate");
    public static final ClassName ClassNameHibernateWhere = ClassName.get("org.hibernate.annotations", "Where");

    public static final ClassName ClassNameJpaPageable = ClassName.get("org.springframework.data.domain", "Pageable");
    public static final ClassName ClassNameJpaPage = ClassName.get("org.springframework.data.domain", "Page");
    public static final ClassName ClassNameJpaSpecification = ClassName.get("org.springframework.data.jpa.domain", "Specification");

    public static final ClassName ClassNameSpringAutowired = ClassName.get("org.springframework.beans.factory.annotation", "Autowired");
    public static final ClassName ClassNameSpringService = ClassName.get("org.springframework.stereotype", "Service");
    public static final ClassName ClassNameSpringTransactional = ClassName.get("org.springframework.transaction.annotation", "Transactional");
    public static final ClassName ClassNameSpringController = ClassName.get("org.springframework.stereotype", "Controller");
    public static final ClassName ClassNameSpringRestController = ClassName.get("org.springframework.web.bind.annotation", "RestController");
    public static final ClassName ClassNameSpringRequestMapping = ClassName.get("org.springframework.web.bind.annotation", "RequestMapping");
    public static final ClassName ClassNameSpringGetMapping = ClassName.get("org.springframework.web.bind.annotation", "GetMapping");
    public static final ClassName ClassNameSpringPostMapping = ClassName.get("org.springframework.web.bind.annotation", "PostMapping");
    public static final ClassName ClassNameSpringRequestParam = ClassName.get("org.springframework.web.bind.annotation", "RequestParam");
    public static final ClassName ClassNameSpringResponseBody = ClassName.get("org.springframework.web.bind.annotation", "ResponseBody");
    public static final ClassName ClassNameSpringRequestBody = ClassName.get("org.springframework.web.bind.annotation", "RequestBody");
    public static final ClassName ClassNameSpringModelMap = ClassName.get("org.springframework.ui", "ModelMap");

    public static final ClassName ClassNameJavaxValid = ClassName.get("javax.validation", "Valid");
    public static final ClassName ClassNameJavaxNotNull = ClassName.get("javax.validation.constraints", "NotNull");
    public static final ClassName ClassNameJavaxNotBlank = ClassName.get("javax.validation.constraints", "NotBlank");

    public static final ClassName ClassNameHibernateDynamicInsert = ClassName.get("org.hibernate.annotations", "DynamicInsert");
    public static final ClassName ClassNameHibernateDynamicUpdate = ClassName.get("org.hibernate.annotations", "DynamicUpdate");

    public static final ClassName ClassNameAbstractQBean = ClassName.get("net.sunshow.toolkit.core.qbean.api.bean", "AbstractQBean");
    public static final ClassName ClassNameQBean = ClassName.get("net.sunshow.toolkit.core.qbean.api.annotation", "QBean");
    public static final ClassName ClassNameQBeanCreator = ClassName.get("net.sunshow.toolkit.core.qbean.api.annotation", "QBeanCreator");
    public static final ClassName ClassNameQBeanUpdater = ClassName.get("net.sunshow.toolkit.core.qbean.api.annotation", "QBeanUpdater");
    public static final ClassName ClassNameQBeanID = ClassName.get("net.sunshow.toolkit.core.qbean.api.annotation", "QBeanID");
    public static final ClassName ClassNameQBeanCreatorIgnore = ClassName.get("net.sunshow.toolkit.core.qbean.api.annotation", "QBeanCreatorIgnore");
    public static final ClassName ClassNameQBeanUpdaterIgnore = ClassName.get("net.sunshow.toolkit.core.qbean.api.annotation", "QBeanUpdaterIgnore");
    public static final ClassName ClassNameBaseEntity = ClassName.get("net.sunshow.toolkit.core.qbean.helper.entity", "BaseEntity");
    public static final ClassName ClassNameBaseRepository = ClassName.get("net.sunshow.toolkit.core.qbean.helper.repository", "BaseRepository");
    public static final ClassName ClassNameAbstractQServiceImpl = ClassName.get("net.sunshow.toolkit.core.qbean.helper.service.impl", "AbstractQServiceImpl");
    public static final ClassName ClassNameQResponse = ClassName.get("net.sunshow.toolkit.core.qbean.api.response", "QResponse");
    public static final ClassName ClassNameQRequest = ClassName.get("net.sunshow.toolkit.core.qbean.api.request", "QRequest");
    public static final ClassName ClassNameQPage = ClassName.get("net.sunshow.toolkit.core.qbean.api.request", "QPage");
    public static final ClassName ClassNameQSort = ClassName.get("net.sunshow.toolkit.core.qbean.api.request", "QSort");
    public static final ClassName ClassNameQField = ClassName.get("net.sunshow.toolkit.core.qbean.api.annotation", "QField");
    public static final ClassName ClassNameQFieldControl = ClassName.get("net.sunshow.toolkit.core.qbean.api.enums", "Control");
    public static final ClassName ClassNameQOperator = ClassName.get("net.sunshow.toolkit.core.qbean.api.enums", "Operator");

    public static final ClassName ClassNameBeanMapper = ClassName.get("net.sunshow.toolkit.core.qbean.helper.component.mapper", "BeanMapper");
    public static final ClassName ClassNameQBeanCreatorHelper = ClassName.get("net.sunshow.toolkit.core.qbean.helper.component.request", "QBeanCreatorHelper");
    public static final ClassName ClassNameQBeanUpdaterHelper = ClassName.get("net.sunshow.toolkit.core.qbean.helper.component.request", "QBeanUpdaterHelper");
    public static final ClassName ClassNameQBeanSearchHelper = ClassName.get("net.sunshow.toolkit.core.qbean.helper.component.request", "QBeanSearchHelper");
    public static final ClassName ClassNameQSearchModelHelper = ClassName.get("net.sunshow.cms.module.admin.component.qbean", "QSearchModelHelper");

    public static final ClassName ClassNamePageFO = ClassName.get("net.sunshow.cms.module.common.fo", "PageFO");
    public static final ClassName ClassNameSortFO = ClassName.get("net.sunshow.cms.module.common.fo", "SortFO");
    public static final ClassName ClassNameResponseFO = ClassName.get("net.sunshow.cms.module.common.fo", "ResponseFO");

    public static final ClassName ClassNameShiroRequiresPermissions = ClassName.get("org.apache.shiro.authz.annotation", "RequiresPermissions");

    public static final ClassName ClassNameLombokData = ClassName.get("lombok", "Data");
    public static final ClassName ClassNameLombokSetter = ClassName.get("lombok", "Setter");
    public static final ClassName ClassNameLombokGetter = ClassName.get("lombok", "Getter");
    public static final ClassName ClassNameLombokNoArgsConstructor = ClassName.get("lombok", "NoArgsConstructor");
    public static final ClassName ClassNameLombokAllArgsConstructor = ClassName.get("lombok", "AllArgsConstructor");
    public static final ClassName ClassNameLombokRequiredArgsConstructor = ClassName.get("lombok", "RequiredArgsConstructor");
    public static final ClassName ClassNameLombokSlf4j = ClassName.get("lombok.extern.slf4j", "Slf4j");

    public static final String FieldNameDeletedTime = "deletedTime";
    public static final String FieldNameCreatedTime = "createdTime";
    public static final String FieldNameUpdatedTime = "updatedTime";

    private String indent = "    ";

    // 基础包路径, 生成代码的上级包路径, 在此路径下分包输出
    private String packagePathPrefix;

    // 数据表前缀, 例如: "tb_"
    private String tableNamePrefix = "";

    // 模块名称, 生成代码都放在各自包下的子模块包下
    private String moduleName;

    // QBean 名称, 读取对应的类来解析生成, 并作为其他包的前缀
    private String beanName;

    // 主键 id 名称
    private String idName = "id";

    // 主键 id 类型
    private ClassName idClassName = ClassName.get(Long.class);

    // 输出路径
    private String outputPath = "";

    // Bean 相关配置
    // 是否生成 creator
    private boolean beanCreator = true;
    // 是否生成 updater
    private boolean beanUpdater = true;

    // 是否支持软删除, 只在 Entity 生成
    private boolean softDelete = true;

    // fo 的模块目录
    private String foModuleName;

    // controller 相关
    private String controllerModuleName;

    private String controllerName;

    private String requestMappingPrefix;

    private boolean controllerDelete = true;

    // 是否使用 shiro
    private boolean shiro = true;

    private String shiroResource;

    private String templatePrefix;

    // bean 包路径
    public String getBeanPackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, "vo", moduleName);
    }

    // bean 类型
    public ClassName getBeanClassName() {
        return ClassName.get(getBeanPackagePath(), beanName);
    }

    public ClassName getBeanCreatorClassName() {
        return ClassName.get(getBeanPackagePath(), beanName + "Creator");
    }

    public ClassName getBeanUpdaterClassName() {
        return ClassName.get(getBeanPackagePath(), beanName + "Updater");
    }

    // Exception 相关配置
    // 名称
    public String getExceptionName() {
        return beanName + "Exception";
    }

    // 默认异常消息
    public String getExceptionDefaultMessage() {
        return String.format("操作 %s 出错", beanName);
    }

    // 包路径
    public String getExceptionPackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, "exception", moduleName);
    }

    // exception 类型
    public ClassName getExceptionClassName() {
        return ClassName.get(getExceptionPackagePath(), getExceptionName());
    }

    // Entity 相关配置
    public String getEntityName() {
        return beanName + "PO";
    }

    // entity 对应的数据库表名
    public String getEntityTableName() {
        return tableNamePrefix + GenerateUtils.upperCamelToLowerUnderScore(beanName);
    }

    // 包路径
    public String getEntityPackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, "po", moduleName);
    }

    // entity 类型
    public ClassName getEntityClassName() {
        return ClassName.get(getEntityPackagePath(), getEntityName());
    }

    // Repository 相关配置
    public String getRepositoryName() {
        return beanName + "DAO";
    }

    // 包路径
    public String getRepositoryPackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, "dao", moduleName);
    }

    // repository 类型
    public ClassName getRepositoryClassName() {
        return ClassName.get(getRepositoryPackagePath(), getRepositoryName());
    }

    // service 相关配置
    public String getServiceName() {
        return beanName + "Service";
    }

    // 包路径
    public String getServicePackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, "service", moduleName);
    }

    // service 类型
    public ClassName getServiceClassName() {
        return ClassName.get(getServicePackagePath(), getServiceName());
    }

    public String getServiceImplName() {
        return beanName + "ServiceImpl";
    }

    // 包路径
    public String getServiceImplPackagePath() {
        return GenerateUtils.combinePackagePath(getServicePackagePath(), "impl", moduleName);
    }

    public String getFoModuleName() {
        if (foModuleName == null) {
            return moduleName;
        }
        return foModuleName;
    }

    // FO 相关配置
    public String getCreateFOName() {
        return beanName + "CreateFO";
    }

    // 包路径
    public String getCreateFOPackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, "fo", getFoModuleName());
    }

    // 类型
    public ClassName getCreateFOClassName() {
        return ClassName.get(getCreateFOPackagePath(), getCreateFOName());
    }

    // FO 相关配置
    public String getUpdateFOName() {
        return beanName + "UpdateFO";
    }

    // 包路径
    public String getUpdateFOPackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, "fo", getFoModuleName());
    }

    // 类型
    public ClassName getUpdateFOClassName() {
        return ClassName.get(getUpdateFOPackagePath(), getUpdateFOName());
    }

    // FO 相关配置
    public String getSearchFOName() {
        return beanName + "SearchFO";
    }

    // 包路径
    public String getSearchFOPackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, "fo", getFoModuleName());
    }

    // 类型
    public ClassName getSearchFOClassName() {
        return ClassName.get(getSearchFOPackagePath(), getSearchFOName());
    }

    // Controller 相关配置
    public String getControllerModuleName() {
        if (controllerModuleName == null) {
            return moduleName;
        }
        return controllerModuleName;
    }

    public String getControllerName() {
        if (controllerName == null) {
            return beanName + "Controller";
        }
        return controllerName;
    }

    // 包路径
    public String getControllerPackagePath() {
        return GenerateUtils.combinePackagePath(packagePathPrefix, "controller", getControllerModuleName());
    }
}

package net.sunshow.code.generator.template.qbean;

import com.squareup.javapoet.ClassName;
import lombok.Getter;
import lombok.Setter;
import net.sunshow.code.generator.util.GenerateUtils;

@Setter
@Getter
public class QBeanTemplate {

    public static final ClassName ClassNameLocalDateTime = ClassName.get("java.time", "LocalDateTime");

    public static final ClassName ClassNameJpaTable = ClassName.get("javax.persistence", "Table");
    public static final ClassName ClassNameJpaEntity = ClassName.get("javax.persistence", "Entity");
    public static final ClassName ClassNameJpaId = ClassName.get("javax.persistence", "Id");
    public static final ClassName ClassNameJpaColumn = ClassName.get("javax.persistence", "Column");
    public static final ClassName ClassNameJpaGeneratedValue = ClassName.get("javax.persistence", "GeneratedValue");
    public static final ClassName ClassNameJpaGenerationType = ClassName.get("javax.persistence", "GenerationType");

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

    public static final ClassName ClassNameLombokSetter = ClassName.get("lombok", "Setter");
    public static final ClassName ClassNameLombokGetter = ClassName.get("lombok", "Getter");

    public static final String FieldNameCreatedTime = "createdTime";
    public static final String FieldNameUpdatedTime = "updatedTime";

    private boolean lombok = true;

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
    public ClassName exceptionClassName() {
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

}

package net.sunshow.code.generator.template.qbean;

import com.google.common.base.MoreObjects;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaSource;
import net.sunshow.code.generator.util.GenerateUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class QMySQLTableGenerator {

    private final static Map<String, String> javaTypeToDBMap = new HashMap<>();
    private final static Map<String, String> javaTypeToDBDefaultValueMap = new HashMap<>();

    static {
        javaTypeToDBMap.put("Long", "BIGINT NOT NULL");
        javaTypeToDBMap.put("String", "VARCHAR(128) NOT NULL");
        javaTypeToDBMap.put("LocalDateTime", "TIMESTAMP NULL");
        javaTypeToDBMap.put("Convert", "TINYINT NOT NULL");
        javaTypeToDBMap.put("created_time", "TIMESTAMP NOT NULL");
        javaTypeToDBMap.put("updated_time", "TIMESTAMP NOT NULL");

        javaTypeToDBDefaultValueMap.put("Long", "DEFAULT 0");
        javaTypeToDBDefaultValueMap.put("String", "DEFAULT ''");
        javaTypeToDBDefaultValueMap.put("Convert", "DEFAULT 0");
        javaTypeToDBDefaultValueMap.put("created_time", "DEFAULT CURRENT_TIMESTAMP");
        javaTypeToDBDefaultValueMap.put("updated_time", "DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
    }

    public static void generate(QTemplate template) throws Exception {
        // 根据 QBean 属性生成代码
        JavaSource src = new JavaProjectBuilder().addSource(new FileReader(
                String.format("%s/%s.java", GenerateUtils.packageNameToPath(new File(template.getOutputPath()).toPath(), template.getEntityPackagePath()), template.getEntityName())));
        JavaClass jcl = src.getClasses().get(0);

        String tableName = jcl.getAnnotations()
                .stream()
                .filter(an -> GenerateUtils.classTypeWildEquals(an.getType().getName(), "Table"))
                .filter(an -> an.getPropertyMap().containsKey("name"))
                .map(an -> StringUtils.replace(an.getProperty("name").toString(), "\"", ""))
                .findAny()
                .orElseGet(() -> GenerateUtils.lowerCamelToLowerUnderScore(jcl.getName()));
        String tableComment = jcl.getComment();

        // 开始拼装输出的SQL
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("CREATE TABLE `%s`\n", tableName))
                .append("(\n");

        JavaField idField = jcl.getFields()
                .stream()
                .filter(field -> field.getAnnotations().stream().anyMatch(an -> GenerateUtils.classTypeWildEquals(an.getType().getName(), "Id")))
                .findFirst()
                .orElseThrow(RuntimeException::new);

        String idName = idField.getName();
        boolean autoincrement = idField.getAnnotations()
                .stream()
                .filter(an -> GenerateUtils.classTypeWildEquals(an.getType().getName(), "GeneratedValue"))
                .filter(an -> an.getPropertyMap().containsKey("strategy"))
                .map(an -> an.getProperty("strategy").toString())
                .anyMatch(s -> s.equals("GenerationType.IDENTITY"));

        // 拼装主键
        sql.append(String.format("`%s` %s %s COMMENT '%s',\n",
                idName, javaTypeToDBMap.get(idField.getType().getName()),
                autoincrement ? "AUTO_INCREMENT" : "",
                MoreObjects.firstNonNull(idField.getComment(), "")));

        for (JavaField field : jcl.getFields()) {
            // 只处理 private 非 static
            if (!field.isPrivate() || field.isStatic()) {
                continue;
            }

            String fieldName = field.getName();

            if (fieldName.equals(template.getIdName())) {
                continue;
            }

            String columnName = field.getAnnotations()
                    .stream()
                    .filter(an -> GenerateUtils.classTypeWildEquals(an.getType().getName(), "Column"))
                    .filter(an -> an.getPropertyMap().containsKey("name"))
                    .findAny()
                    .map(an -> StringUtils.replace(an.getProperty("name").toString(), "\"", ""))
                    .orElseGet(field::getName);

            String columnType = field.getType().getName();
            boolean hasConverter = field.getAnnotations()
                    .stream()
                    .anyMatch(an -> GenerateUtils.classTypeWildEquals(an.getType().getName(), "Convert"));
            if (hasConverter) {
                // 如果指定了 Convert 没办法知道具体类型 使用固定值
                columnType = "Convert";
            } else if (fieldName.equals(QTemplate.FieldNameCreatedTime) ||
                    fieldName.equals(QTemplate.FieldNameUpdatedTime)) {
                // 创建时间和更新时间特殊处理
                columnType = GenerateUtils.lowerCamelToLowerUnderScore(field.getName());
            } else if (!javaTypeToDBMap.containsKey(columnType)) {
                columnType = "Convert";
            }

            sql.append(String.format("`%s` %s %s COMMENT '%s',\n",
                    columnName, javaTypeToDBMap.get(columnType),
                    MoreObjects.firstNonNull(javaTypeToDBDefaultValueMap.get(columnType), ""),
                    MoreObjects.firstNonNull(field.getComment(), "")
            ));

        }

        sql.append(String.format("PRIMARY KEY (`%s`)\n", template.getIdName()))
                .append(String.format(") ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT ='%s';", tableComment));

        System.out.println(sql.toString());
    }

}

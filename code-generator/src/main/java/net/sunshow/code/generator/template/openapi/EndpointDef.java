package net.sunshow.code.generator.template.openapi;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.sunshow.code.generator.util.GenerateUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class EndpointDef {

    private String endpoint;

    private List<EndpointMethodDef> methodDefList;

    private String module;

    private String subModule;

    private String apiName;

    private String namePrefix;

    public String getNamePrefixCanonical() {
        // 生成请求体和响应体时 为了避免没有 subModule 时命名过于简单, 拼入 module
        if (StringUtils.isEmpty(subModule)) {
            return GenerateUtils.lowerCamelToUpperCamel(module) + namePrefix;
        }
        return namePrefix;
    }
}

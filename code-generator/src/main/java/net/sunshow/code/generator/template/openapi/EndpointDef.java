package net.sunshow.code.generator.template.openapi;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EndpointDef {

    private String endpoint;

    private List<EndpointMethodDef> methodDefList;

    private String module;

    private String subModule;

    private String name;

    /**
     * 是否分页请求
     */
    private boolean pageable;

    /**
     * 分页请求包含分页数据之外的属性
     */
    private boolean pageableRequestHasExtraProperties;

    /**
     * 分页响应包含分页数据之外的属性
     */
    private boolean pageableResponseHasExtraProperties;
}

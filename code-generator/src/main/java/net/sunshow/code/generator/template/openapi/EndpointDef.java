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

    private String apiName;

    private String namePrefix;

}

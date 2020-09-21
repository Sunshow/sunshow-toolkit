package net.sunshow.code.generator.template.openapi;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EndpointMethodDef {

    private HttpMethod httpMethod;

    private List<String> tagList;

    private String requestSchemaRef;

    private String responseSchemaRef;
    
}

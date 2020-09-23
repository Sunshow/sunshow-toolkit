package net.sunshow.code.generator.template.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.sunshow.code.generator.util.GenerateUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OpenApiParser {

    private ObjectMapper objectMapper;

    private ObjectNode paths;

    private ObjectNode schemas;

    public OpenApiParser(String url) {
        objectMapper = new ObjectMapper();

        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(new URL(url));
            paths = (ObjectNode) root.get("paths");
            schemas = (ObjectNode) root.get("components").get("schemas");
        } catch (Exception e) {
            throw new RuntimeException("解析OpenApi出错", e);
        }
    }

    public EndpointDef parse(String endpoint, String module, String subModule) {
        EndpointDef def = new EndpointDef();
        def.setEndpoint(endpoint);
        def.setModule(module);
        def.setSubModule(subModule);

        String[] sections = StringUtils.split(endpoint, "/");

        int sectionIndex = 0;
        List<String> moduleList = new ArrayList<>();
        // 要检测模块名
        if (sections[sectionIndex].equals(module)) {
            moduleList.add(GenerateUtils.lowerCamelToUpperCamel(module));
        } else {
            throw new RuntimeException("未检测到模块, module=" + module);
        }
        sectionIndex++;
        // 要检测子模块名
        if (StringUtils.isNotEmpty(subModule)) {
            if (sections[1].equals(subModule)) {
                moduleList.add(GenerateUtils.lowerCamelToUpperCamel(subModule));
            } else {
                throw new RuntimeException("未检测到子模块, subModule=" + subModule);
            }
            sectionIndex++;
        }

        List<String> nameList = new ArrayList<>();
        for (int i = sectionIndex; i < sections.length; i++) {
            String s = sections[i];
            // 后面每段顺序压入
            nameList.add(GenerateUtils.lowerCamelToUpperCamel(GenerateUtils.lowerUnderScoreToUpperCamel(s)));
        }

        def.setName(StringUtils.join(nameList, ""));

        List<EndpointMethodDef> methodDefList = new ArrayList<>();

        ObjectNode node = (ObjectNode) paths.get(endpoint);
        // 每个属性是一种支持的 http method
        node.fieldNames().forEachRemaining(field -> {
            HttpMethod method = HttpMethod.valueOf(field.toUpperCase());

            EndpointMethodDef methodDef = new EndpointMethodDef();
            methodDef.setHttpMethod(method);

            // tags
            if (node.get(field).has("tags")) {
                List<String> tags = new ArrayList<>();
                node.get(field).get("tags").forEach(tag -> tags.add(tag.asText()));
                methodDef.setTagList(tags);
            }

            // 获取请求体
            if (node.get(field).has("requestBody")) {
                String requestSchemaRef = node.get(field).get("requestBody").get("content").iterator().next().get("schema").get("$ref").asText();
                requestSchemaRef = StringUtils.substringAfter(requestSchemaRef, "#/components/schemas/");
                methodDef.setRequestSchemaRef(requestSchemaRef);
                System.out.println(requestSchemaRef);
            }

            // 获取响应体
            String responseSchemaRef = node.get(field).get("responses").get("200").get("content").iterator().next().get("schema").get("$ref").asText();
            responseSchemaRef = StringUtils.substringAfter(responseSchemaRef, "#/components/schemas/");
            methodDef.setResponseSchemaRef(responseSchemaRef);
            System.out.println(responseSchemaRef);

            methodDefList.add(methodDef);
        });

        def.setMethodDefList(methodDefList);
        return def;
    }

    public ObjectNode getPaths() {
        return paths;
    }

    public ObjectNode getSchemas() {
        return schemas;
    }
}

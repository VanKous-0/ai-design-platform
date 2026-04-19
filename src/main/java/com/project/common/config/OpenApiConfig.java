package com.project.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    public static final String AUTHORIZATION = "Authorization";
    private static final String LOGIN_PATH = "/api/auth/login";

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme authorizationHeaderScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .info(new Info()
                        .title("AI Design Platform Backend API")
                        .description("面向建筑设计全周期的生成式 AI 辅助决策服务平台后端接口")
                        .version("0.0.1"))
                .components(new Components().addSecuritySchemes(AUTHORIZATION, authorizationHeaderScheme))
                .addSecurityItem(new SecurityRequirement().addList(AUTHORIZATION));
    }

    @Bean
    public OpenApiCustomizer authOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            for (Map.Entry<String, PathItem> pathEntry : openApi.getPaths().entrySet()) {
                PathItem pathItem = pathEntry.getValue();
                if (pathItem == null) {
                    continue;
                }

                for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry : pathItem.readOperationsMap().entrySet()) {
                    Operation operation = operationEntry.getValue();
                    if (operation == null) {
                        continue;
                    }

                    if (LOGIN_PATH.equals(pathEntry.getKey()) && PathItem.HttpMethod.POST.equals(operationEntry.getKey())) {
                        operation.setSecurity(new ArrayList<>());
                    } else if (isPublicStageOperation(pathEntry.getKey(), operationEntry.getKey())
                            || isPublicToolOperation(pathEntry.getKey(), operationEntry.getKey())
                            || isPublicPromptOperation(pathEntry.getKey(), operationEntry.getKey())
                            || isPublicCaseOperation(pathEntry.getKey(), operationEntry.getKey())
                            || isPublicReviewOperation(pathEntry.getKey(), operationEntry.getKey())
                            || isPublicSiteOperation(pathEntry.getKey(), operationEntry.getKey())) {
                        operation.setSecurity(new ArrayList<>());
                    } else {
                        operation.setSecurity(List.of(new SecurityRequirement().addList(AUTHORIZATION)));
                    }
                }
            }
        };
    }

    private boolean isPublicStageOperation(String path, PathItem.HttpMethod method) {
        return PathItem.HttpMethod.GET.equals(method)
                && ("/api/stages".equals(path)
                || path.matches("^/api/stages/\\{[^/]+}$")
                || path.matches("^/api/stages/\\{[^/]+}/steps$"));
    }

    private boolean isPublicToolOperation(String path, PathItem.HttpMethod method) {
        return PathItem.HttpMethod.GET.equals(method)
                && ("/api/tools".equals(path)
                || "/api/tools/recommend".equals(path)
                || path.matches("^/api/tools/\\{[^/]+}$")
                || path.matches("^/api/tools/\\{[^/]+}/evaluations$"));
    }

    private boolean isPublicPromptOperation(String path, PathItem.HttpMethod method) {
        return (PathItem.HttpMethod.GET.equals(method)
                && ("/api/prompts".equals(path)
                || "/api/prompts/search".equals(path)
                || "/api/prompts/recommend".equals(path)
                || path.matches("^/api/prompts/\\{[^/]+}$")))
                || (PathItem.HttpMethod.POST.equals(method)
                && path.matches("^/api/prompts/\\{[^/]+}/copy$"));
    }

    private boolean isPublicCaseOperation(String path, PathItem.HttpMethod method) {
        return PathItem.HttpMethod.GET.equals(method)
                && ("/api/cases".equals(path)
                || "/api/cases/recommend".equals(path)
                || path.matches("^/api/cases/\\{[^/]+}$")
                || path.matches("^/api/cases/\\{[^/]+}/assets$"));
    }

    private boolean isPublicReviewOperation(String path, PathItem.HttpMethod method) {
        return PathItem.HttpMethod.GET.equals(method)
                && ("/api/reviews".equals(path)
                || "/api/reviews/recommend".equals(path)
                || path.matches("^/api/reviews/\\{[^/]+}$")
                || path.matches("^/api/reviews/\\{[^/]+}/assets$"));
    }

    private boolean isPublicSiteOperation(String path, PathItem.HttpMethod method) {
        return PathItem.HttpMethod.GET.equals(method)
                && ("/api/site/home".equals(path)
                || "/api/site/contents".equals(path)
                || path.matches("^/api/site/contents/\\{[^/]+}$")
                || "/api/awards".equals(path)
                || path.matches("^/api/awards/\\{[^/]+}$"));
    }
}

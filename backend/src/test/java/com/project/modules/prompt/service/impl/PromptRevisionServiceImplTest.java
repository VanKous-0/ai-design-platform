package com.project.modules.prompt.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.modules.prompt.entity.PromptParameter;
import com.project.modules.prompt.entity.PromptRevision;
import com.project.modules.prompt.entity.PromptTemplate;
import com.project.modules.prompt.mapper.PromptParameterMapper;
import com.project.modules.prompt.mapper.PromptRevisionMapper;
import com.project.modules.prompt.mapper.PromptTemplateMapper;
import com.project.modules.prompt.vo.PromptRenderVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptRevisionServiceImplTest {

    @Mock
    private PromptRevisionMapper revisionMapper;
    @Mock
    private PromptParameterMapper parameterMapper;
    @Mock
    private PromptTemplateMapper templateMapper;

    private PromptRevisionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PromptRevisionServiceImpl(
                revisionMapper,
                parameterMapper,
                templateMapper,
                new ObjectMapper()
        );
    }

    @Test
    void revisionsKeepContentAndParameterSchemaReproducible() throws Exception {
        PromptTemplate prompt = prompt("Create a {style} design");
        PromptParameter style = parameter("style", true);
        AtomicLong ids = new AtomicLong(100);
        when(parameterMapper.selectList(any())).thenReturn(List.of(style));
        when(revisionMapper.selectMaxRevisionNo(1L)).thenReturn(0, 1);
        doAnswer(invocation -> {
            PromptRevision revision = invocation.getArgument(0);
            revision.setId(ids.getAndIncrement());
            return 1;
        }).when(revisionMapper).insert(any(PromptRevision.class));

        PromptRevision v1 = service.createRevision(prompt, 9L);
        prompt.setContent("Create a {style} design for {siteArea}");
        PromptParameter siteArea = parameter("siteArea", true);
        when(parameterMapper.selectList(any())).thenReturn(List.of(style, siteArea));
        PromptRevision v2 = service.createRevision(prompt, 9L);

        assertEquals(1, v1.getRevisionNo());
        assertEquals(2, v2.getRevisionNo());
        assertNotEquals(v1.getId(), v2.getId());
        assertEquals("Create a {style} design", v1.getContent());
        assertEquals(1, new ObjectMapper().readTree(v1.getParameterSchemaJson()).size());
        assertEquals(2, new ObjectMapper().readTree(v2.getParameterSchemaJson()).size());
        assertEquals(v2.getId(), prompt.getCurrentRevisionId());

        when(revisionMapper.selectOne(any())).thenReturn(v1);
        PromptRenderVO historicalRender = service.render(prompt, v1.getId(), Map.of("style", "minimal"));

        assertEquals(v1.getId(), historicalRender.getPromptRevisionId());
        assertEquals(1, historicalRender.getRevisionNo());
        assertEquals("Create a minimal design", historicalRender.getRenderedContent());
    }

    private PromptTemplate prompt(String content) {
        PromptTemplate prompt = new PromptTemplate();
        prompt.setId(1L);
        prompt.setTitle("Design intent");
        prompt.setContent(content);
        return prompt;
    }

    private PromptParameter parameter(String key, boolean required) {
        PromptParameter parameter = new PromptParameter();
        parameter.setParamKey(key);
        parameter.setParamName(key);
        parameter.setParamType("text");
        parameter.setRequired(required ? 1 : 0);
        parameter.setSortOrder(0);
        return parameter;
    }
}

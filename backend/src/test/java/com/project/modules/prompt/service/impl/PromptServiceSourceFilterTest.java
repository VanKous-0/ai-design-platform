package com.project.modules.prompt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.project.modules.prompt.entity.PromptTemplate;
import com.project.modules.prompt.mapper.PromptParameterMapper;
import com.project.modules.prompt.mapper.PromptPreferenceHintMapper;
import com.project.modules.prompt.mapper.PromptTemplateMapper;
import com.project.modules.prompt.mapper.PromptToolRelMapper;
import com.project.modules.prompt.mapper.WorkflowNodePromptRelMapper;
import com.project.modules.tool.mapper.AiToolMapper;
import com.project.modules.workflow.mapper.WorkflowStageMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateNodeMapper;
import com.project.modules.prompt.service.PromptRevisionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptServiceSourceFilterTest {

    @Mock
    private PromptTemplateMapper promptTemplateMapper;
    @Mock
    private PromptToolRelMapper promptToolRelMapper;
    @Mock
    private PromptParameterMapper promptParameterMapper;
    @Mock
    private PromptPreferenceHintMapper promptPreferenceHintMapper;
    @Mock
    private WorkflowNodePromptRelMapper workflowNodePromptRelMapper;
    @Mock
    private WorkflowTemplateNodeMapper workflowTemplateNodeMapper;
    @Mock
    private WorkflowStageMapper workflowStageMapper;
    @Mock
    private AiToolMapper aiToolMapper;
    @Mock
    private PromptRevisionService promptRevisionService;

    private PromptServiceImpl service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "prompt-test"),
                PromptTemplate.class
        );
        service = new PromptServiceImpl(
                promptTemplateMapper,
                promptToolRelMapper,
                promptParameterMapper,
                promptPreferenceHintMapper,
                workflowNodePromptRelMapper,
                workflowTemplateNodeMapper,
                workflowStageMapper,
                aiToolMapper,
                promptRevisionService
        );
    }

    @Test
    void sourceTypeFilterIsAddedAndDemoContentIsExcluded() {
        when(promptTemplateMapper.selectList(any())).thenReturn(Collections.emptyList());

        service.listPrompts(null, null, null, "original");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaQueryWrapper<PromptTemplate>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(promptTemplateMapper).selectList(captor.capture());
        LambdaQueryWrapper<PromptTemplate> wrapper = captor.getValue();

        assertTrue(wrapper.getCustomSqlSegment().contains("source_type"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue("ORIGINAL"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue("DEMO"));
    }
}

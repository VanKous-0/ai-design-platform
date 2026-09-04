package com.project.modules.workflow.runtime.service.impl;

import com.project.common.exception.BusinessException;
import com.project.modules.workflow.runtime.dto.WorkflowInstanceCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepCompleteRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepIterationCreateRequest;
import com.project.modules.workflow.runtime.entity.WorkflowInstance;
import com.project.modules.workflow.runtime.entity.WorkflowStepIteration;
import com.project.modules.workflow.runtime.entity.WorkflowStepRecord;
import com.project.modules.workflow.runtime.entity.WorkflowTemplate;
import com.project.modules.workflow.runtime.entity.WorkflowTemplateNode;
import com.project.modules.workflow.runtime.mapper.WorkflowInstanceMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowStepIterationMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowStepRecordMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateNodeMapper;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceDetailVO;
import com.project.modules.workflow.runtime.vo.WorkflowProgressVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepCompleteVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepIterationVO;
import com.project.modules.tool.entity.AiTool;
import com.project.modules.tool.mapper.AiToolMapper;
import com.project.modules.prompt.service.PromptRevisionService;
import com.project.modules.prompt.entity.PromptRevision;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowInstanceServiceImplTest {

    @Mock
    private WorkflowTemplateMapper templateMapper;

    @Mock
    private WorkflowTemplateNodeMapper nodeMapper;

    @Mock
    private WorkflowInstanceMapper instanceMapper;

    @Mock
    private WorkflowStepRecordMapper stepRecordMapper;

    @Mock
    private WorkflowStepIterationMapper stepIterationMapper;

    @Mock
    private AiToolMapper aiToolMapper;

    @Mock
    private PromptRevisionService promptRevisionService;

    private WorkflowInstanceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkflowInstanceServiceImpl(
                templateMapper,
                nodeMapper,
                instanceMapper,
                stepRecordMapper,
                stepIterationMapper,
                aiToolMapper,
                promptRevisionService,
                new ObjectMapper()
        );
    }

    @Test
    void createsInstanceAtFirstEnabledNode() {
        WorkflowTemplate template = template(10L, "景观设计流程");
        WorkflowTemplateNode firstNode = node(101L, 10L, "场地分析", 1);
        WorkflowInstanceCreateRequest request = new WorkflowInstanceCreateRequest();
        request.setTemplateId(10L);

        when(templateMapper.selectOne(any())).thenReturn(template);
        when(nodeMapper.selectList(any())).thenReturn(List.of(firstNode));
        doAnswer(invocation -> {
            WorkflowInstance instance = invocation.getArgument(0);
            instance.setId(1000L);
            return 1;
        }).when(instanceMapper).insert(any(WorkflowInstance.class));
        when(templateMapper.selectById(10L)).thenReturn(template);
        when(nodeMapper.selectById(101L)).thenReturn(firstNode);
        when(stepRecordMapper.selectList(any())).thenReturn(List.of());

        WorkflowInstanceDetailVO result = service.createInstance(7L, request);

        assertEquals(1000L, result.getId());
        assertEquals(101L, result.getCurrentNodeId());
        assertEquals("RUNNING", result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getProgress());
    }

    @Test
    void calculatesProgressFromCompletedRecords() {
        WorkflowInstance instance = runningInstance();
        WorkflowTemplateNode firstNode = node(101L, 10L, "场地分析", 1);
        WorkflowTemplateNode secondNode = node(102L, 10L, "方案生成", 2);

        when(instanceMapper.selectOne(any())).thenReturn(instance);
        when(nodeMapper.selectList(any())).thenReturn(List.of(firstNode, secondNode));
        when(stepRecordMapper.selectCount(any())).thenReturn(1L);
        when(nodeMapper.selectById(101L)).thenReturn(firstNode);

        WorkflowProgressVO result = service.getProgress(7L, 1000L);

        assertEquals(2, result.getTotalNodeCount());
        assertEquals(1, result.getCompletedNodeCount());
        assertEquals(new BigDecimal("50.00"), result.getProgress());
    }

    @Test
    void rejectsAccessToAnotherUsersInstance() {
        when(instanceMapper.selectOne(any())).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.getMyInstance(7L, 1000L));
    }

    @Test
    void completesCurrentStepAndAdvancesToNextNode() {
        WorkflowInstance instance = runningInstance();
        WorkflowTemplateNode firstNode = node(101L, 10L, "场地分析", 1);
        WorkflowTemplateNode secondNode = node(102L, 10L, "方案生成", 2);
        WorkflowStepCompleteRequest request = new WorkflowStepCompleteRequest();
        request.setOutputContent("分析结果");

        when(instanceMapper.selectOne(any())).thenReturn(instance);
        when(nodeMapper.selectList(any())).thenReturn(List.of(firstNode, secondNode));
        when(stepRecordMapper.selectOne(any())).thenReturn(null);
        when(stepRecordMapper.selectCount(any())).thenReturn(1L);

        WorkflowStepCompleteVO result = service.completeStep(7L, 1000L, 101L, request);

        assertEquals(new BigDecimal("50.00"), result.getProgress());
        assertEquals(102L, result.getNextStep().getNextNodeId());
        assertEquals(102L, instance.getCurrentNodeId());
        verify(instanceMapper).updateById(instance);
        verify(stepRecordMapper).insert(any(WorkflowStepRecord.class));
    }

    @Test
    void createsScoredIterationAndCalculatesAverage() {
        WorkflowInstance instance = runningInstance();
        WorkflowTemplateNode node = node(101L, 10L, "场地分析", 1);
        AiTool tool = new AiTool();
        tool.setId(3L);
        tool.setName("External AI");
        tool.setStatus(1);
        WorkflowStepIterationCreateRequest request = new WorkflowStepIterationCreateRequest();
        request.setToolId(3L);
        request.setPromptContent("Analyze the site");
        request.setOutputContent("Analysis result");
        request.setEffectScore(8);
        request.setAccuracyScore(9);
        request.setControllabilityScore(7);
        request.setUsabilityScore(10);
        request.setSelected(true);

        when(instanceMapper.selectOne(any())).thenReturn(instance);
        when(nodeMapper.selectOne(any())).thenReturn(node);
        when(aiToolMapper.selectOne(any())).thenReturn(tool);
        when(stepIterationMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            WorkflowStepIteration iteration = invocation.getArgument(0);
            iteration.setId(2000L);
            return 1;
        }).when(stepIterationMapper).insert(any(WorkflowStepIteration.class));

        WorkflowStepIterationVO result = service.createStepIteration(7L, 1000L, 101L, request);

        assertEquals(1, result.getIterationNo());
        assertEquals(new BigDecimal("8.50"), result.getAverageScore());
        assertEquals(true, result.getSelected());
        assertEquals("External AI", result.getToolName());
        verify(stepIterationMapper).insert(any(WorkflowStepIteration.class));
    }

    @Test
    void rejectsIterationForNodeOutsideTemplate() {
        WorkflowStepIterationCreateRequest request = new WorkflowStepIterationCreateRequest();
        when(instanceMapper.selectOne(any())).thenReturn(runningInstance());
        when(nodeMapper.selectOne(any())).thenReturn(null);

        assertThrows(
                BusinessException.class,
                () -> service.createStepIteration(7L, 1000L, 999L, request)
        );
    }

    @Test
    void rejectsIterationWithoutOutputOrResultUrl() {
        WorkflowStepIterationCreateRequest request = new WorkflowStepIterationCreateRequest();
        when(instanceMapper.selectOne(any())).thenReturn(runningInstance());
        when(nodeMapper.selectOne(any())).thenReturn(node(101L, 10L, "场地分析", 1));

        assertThrows(
                BusinessException.class,
                () -> service.createStepIteration(7L, 1000L, 101L, request)
        );
    }

    @Test
    void savesExactPromptRevisionAndRenderedSnapshots() {
        WorkflowInstance instance = runningInstance();
        WorkflowTemplateNode node = node(101L, 10L, "场地分析", 1);
        WorkflowStepIterationCreateRequest request = new WorkflowStepIterationCreateRequest();
        request.setPromptId(20L);
        request.setPromptRevisionId(200L);
        request.setPromptContent("Design in modern minimal style");
        request.setProfileContextSnapshot("{\"style\":\"modern minimal\"}");
        request.setOutputContent("Analysis result");
        PromptRevision revision = new PromptRevision();
        revision.setId(200L);
        revision.setPromptId(20L);

        when(instanceMapper.selectOne(any())).thenReturn(instance);
        when(nodeMapper.selectOne(any())).thenReturn(node);
        when(promptRevisionService.requireRevision(20L, 200L)).thenReturn(revision);
        when(stepIterationMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            WorkflowStepIteration iteration = invocation.getArgument(0);
            iteration.setId(2000L);
            return 1;
        }).when(stepIterationMapper).insert(any(WorkflowStepIteration.class));

        WorkflowStepIterationVO result = service.createStepIteration(7L, 1000L, 101L, request);

        assertEquals(20L, result.getPromptId());
        assertEquals(200L, result.getPromptRevisionId());
        assertEquals("Design in modern minimal style", result.getPromptContent());
        assertEquals("{\"style\":\"modern minimal\"}", result.getProfileContextSnapshot());
    }

    @Test
    void rejectsPartialPromptReferenceAndInvalidProfileSnapshot() {
        WorkflowStepIterationCreateRequest partialReference = new WorkflowStepIterationCreateRequest();
        partialReference.setPromptId(20L);
        partialReference.setOutputContent("Analysis result");
        when(instanceMapper.selectOne(any())).thenReturn(runningInstance());
        when(nodeMapper.selectOne(any())).thenReturn(node(101L, 10L, "场地分析", 1));

        assertThrows(
                BusinessException.class,
                () -> service.createStepIteration(7L, 1000L, 101L, partialReference)
        );

        WorkflowStepIterationCreateRequest invalidSnapshot = new WorkflowStepIterationCreateRequest();
        invalidSnapshot.setOutputContent("Analysis result");
        invalidSnapshot.setProfileContextSnapshot("[]");

        assertThrows(
                BusinessException.class,
                () -> service.createStepIteration(7L, 1000L, 101L, invalidSnapshot)
        );
    }

    private WorkflowInstance runningInstance() {
        WorkflowInstance instance = new WorkflowInstance();
        instance.setId(1000L);
        instance.setTemplateId(10L);
        instance.setUserId(7L);
        instance.setCurrentNodeId(101L);
        instance.setStatus("RUNNING");
        instance.setProgress(BigDecimal.ZERO);
        return instance;
    }

    private WorkflowTemplate template(Long id, String name) {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setId(id);
        template.setName(name);
        template.setStatus(1);
        return template;
    }

    private WorkflowTemplateNode node(Long id, Long templateId, String name, int order) {
        WorkflowTemplateNode node = new WorkflowTemplateNode();
        node.setId(id);
        node.setTemplateId(templateId);
        node.setNodeName(name);
        node.setSortOrder(order);
        node.setStatus(1);
        return node;
    }
}

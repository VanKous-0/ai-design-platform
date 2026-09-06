package com.project.modules.workflow.runtime.service.impl;

import com.project.modules.workflow.runtime.dto.WorkflowInstanceCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepCompleteRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepIterationCreateRequest;
import com.project.modules.workflow.runtime.entity.WorkflowInstance;
import com.project.modules.workflow.runtime.entity.WorkflowTemplate;
import com.project.modules.workflow.runtime.entity.WorkflowTemplateNode;
import com.project.modules.workflow.runtime.mapper.WorkflowInstanceMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowNodeRuntimeMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowStepRecordMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateNodeMapper;
import com.project.modules.workflow.runtime.service.WorkflowIterationService;
import com.project.modules.workflow.runtime.service.WorkflowRuntimeAccessService;
import com.project.modules.workflow.runtime.service.WorkflowTransitionService;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceDetailVO;
import com.project.modules.workflow.runtime.vo.WorkflowNextStepVO;
import com.project.modules.workflow.runtime.vo.WorkflowProgressVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepCompleteVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepIterationVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowInstanceServiceImplTest {

    @Mock private WorkflowTemplateMapper templateMapper;
    @Mock private WorkflowTemplateNodeMapper nodeMapper;
    @Mock private WorkflowInstanceMapper instanceMapper;
    @Mock private WorkflowStepRecordMapper stepRecordMapper;
    @Mock private WorkflowRuntimeAccessService accessService;
    @Mock private WorkflowTransitionService transitionService;
    @Mock private WorkflowIterationService iterationService;
    @Mock private WorkflowNodeRuntimeMapper nodeRuntimeMapper;

    private WorkflowInstanceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkflowInstanceServiceImpl(
                templateMapper, nodeMapper, instanceMapper, stepRecordMapper, nodeRuntimeMapper,
                accessService, transitionService, iterationService
        );
    }

    @Test
    void createsInstanceAndInitializesNodeRuntimeRows() {
        WorkflowTemplate template = template(10L, "景观设计流程");
        WorkflowTemplateNode firstNode = node(101L, 10L, "场地分析", 1);
        WorkflowInstanceCreateRequest request = new WorkflowInstanceCreateRequest();
        request.setTemplateId(10L);
        when(accessService.requireEnabledTemplate(10L)).thenReturn(template);
        when(accessService.listEnabledNodes(10L)).thenReturn(List.of(firstNode));
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
        assertEquals(0, result.getProgress().compareTo(BigDecimal.ZERO));
        verify(nodeRuntimeMapper).insertIfAbsent(1000L, 101L);
    }

    @Test
    void calculatesProgressFromCanonicalStepRecords() {
        WorkflowInstance instance = runningInstance();
        WorkflowTemplateNode first = node(101L, 10L, "场地分析", 1);
        WorkflowTemplateNode second = node(102L, 10L, "方案生成", 2);
        when(accessService.requireOwnedInstance(7L, 1000L)).thenReturn(instance);
        when(accessService.listEnabledNodes(10L)).thenReturn(List.of(first, second));
        when(stepRecordMapper.selectCount(any())).thenReturn(1L);
        when(nodeMapper.selectById(101L)).thenReturn(first);

        WorkflowProgressVO result = service.getProgress(7L, 1000L);

        assertEquals(2, result.getTotalNodeCount());
        assertEquals(1, result.getCompletedNodeCount());
        assertEquals(new BigDecimal("50.00"), result.getProgress());
    }

    @Test
    void delegatesTransitionWithIdempotencyKey() {
        WorkflowStepCompleteRequest request = new WorkflowStepCompleteRequest();
        WorkflowStepCompleteVO expected = WorkflowStepCompleteVO.builder()
                .instanceId(1000L).completedNodeId(101L).progress(new BigDecimal("50.00"))
                .nextStep(WorkflowNextStepVO.builder().nextNodeId(102L).whetherFinished(false).build())
                .build();
        when(transitionService.completeStep(7L, 1000L, 101L, request, "completion-1"))
                .thenReturn(expected);

        assertEquals(expected, service.completeStep(7L, 1000L, 101L, request, "completion-1"));
    }

    @Test
    void delegatesIterationCreationWithIdempotencyKey() {
        WorkflowStepIterationCreateRequest request = new WorkflowStepIterationCreateRequest();
        WorkflowStepIterationVO expected = WorkflowStepIterationVO.builder()
                .id(2000L).instanceId(1000L).nodeId(101L).iterationNo(1).build();
        when(iterationService.create(7L, 1000L, 101L, request, "iteration-1"))
                .thenReturn(expected);

        assertEquals(expected, service.createStepIteration(7L, 1000L, 101L, request, "iteration-1"));
    }

    private WorkflowInstance runningInstance() {
        WorkflowInstance instance = new WorkflowInstance();
        instance.setId(1000L);
        instance.setTemplateId(10L);
        instance.setUserId(7L);
        instance.setCurrentNodeId(101L);
        instance.setStatus("RUNNING");
        instance.setProgress(BigDecimal.ZERO);
        instance.setLockVersion(0L);
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

package com.project.modules.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.common.exception.BusinessException;
import com.project.modules.workflow.dto.WorkflowStageCreateRequest;
import com.project.modules.workflow.dto.WorkflowStageUpdateRequest;
import com.project.modules.workflow.dto.WorkflowStepCreateRequest;
import com.project.modules.workflow.dto.WorkflowStepUpdateRequest;
import com.project.modules.workflow.entity.WorkflowStage;
import com.project.modules.workflow.entity.WorkflowStep;
import com.project.modules.workflow.mapper.WorkflowStageMapper;
import com.project.modules.workflow.mapper.WorkflowStepMapper;
import com.project.modules.workflow.service.WorkflowService;
import com.project.modules.workflow.vo.WorkflowStageVO;
import com.project.modules.workflow.vo.WorkflowStepVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkflowServiceImpl implements WorkflowService {

    private static final int STATUS_ENABLED = 1;
    private static final int DEFAULT_STATUS = 1;
    private static final int DEFAULT_SORT_ORDER = 0;

    private final WorkflowStageMapper workflowStageMapper;
    private final WorkflowStepMapper workflowStepMapper;

    public WorkflowServiceImpl(WorkflowStageMapper workflowStageMapper, WorkflowStepMapper workflowStepMapper) {
        this.workflowStageMapper = workflowStageMapper;
        this.workflowStepMapper = workflowStepMapper;
    }

    @Override
    public List<WorkflowStageVO> listEnabledStages() {
        return workflowStageMapper.selectList(new LambdaQueryWrapper<WorkflowStage>()
                        .eq(WorkflowStage::getStatus, STATUS_ENABLED)
                        .orderByAsc(WorkflowStage::getSortOrder)
                        .orderByAsc(WorkflowStage::getId))
                .stream()
                .map(this::toStageVO)
                .toList();
    }

    @Override
    public WorkflowStageVO getEnabledStage(Long id) {
        WorkflowStage stage = workflowStageMapper.selectOne(new LambdaQueryWrapper<WorkflowStage>()
                .eq(WorkflowStage::getId, id)
                .eq(WorkflowStage::getStatus, STATUS_ENABLED)
                .last("limit 1"));
        if (stage == null) {
            throw new BusinessException("设计阶段不存在或未启用");
        }
        return toStageVO(stage);
    }

    @Override
    public List<WorkflowStepVO> listEnabledStepsByStageId(Long stageId) {
        getEnabledStage(stageId);
        return workflowStepMapper.selectList(new LambdaQueryWrapper<WorkflowStep>()
                        .eq(WorkflowStep::getStageId, stageId)
                        .eq(WorkflowStep::getStatus, STATUS_ENABLED)
                        .orderByAsc(WorkflowStep::getSortOrder)
                        .orderByAsc(WorkflowStep::getId))
                .stream()
                .map(this::toStepVO)
                .toList();
    }

    @Override
    public WorkflowStageVO createStage(WorkflowStageCreateRequest request) {
        ensureStageCodeUnique(request.getCode(), null);

        LocalDateTime now = LocalDateTime.now();
        WorkflowStage stage = new WorkflowStage();
        stage.setName(request.getName());
        stage.setCode(request.getCode());
        stage.setDescription(request.getDescription());
        stage.setSortOrder(defaultIfNull(request.getSortOrder(), DEFAULT_SORT_ORDER));
        stage.setStatus(defaultIfNull(request.getStatus(), DEFAULT_STATUS));
        stage.setCreateTime(now);
        stage.setUpdateTime(now);
        stage.setIsDeleted(0);
        workflowStageMapper.insert(stage);
        return toStageVO(stage);
    }

    @Override
    public WorkflowStageVO updateStage(Long id, WorkflowStageUpdateRequest request) {
        WorkflowStage stage = getStageEntity(id);
        ensureStageCodeUnique(request.getCode(), id);

        stage.setName(request.getName());
        stage.setCode(request.getCode());
        stage.setDescription(request.getDescription());
        stage.setSortOrder(defaultIfNull(request.getSortOrder(), DEFAULT_SORT_ORDER));
        stage.setStatus(defaultIfNull(request.getStatus(), DEFAULT_STATUS));
        stage.setUpdateTime(LocalDateTime.now());
        workflowStageMapper.updateById(stage);
        return toStageVO(stage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStage(Long id) {
        getStageEntity(id);
        workflowStageMapper.deleteById(id);
        workflowStepMapper.delete(new LambdaQueryWrapper<WorkflowStep>()
                .eq(WorkflowStep::getStageId, id));
    }

    @Override
    public WorkflowStepVO createStep(WorkflowStepCreateRequest request) {
        getStageEntity(request.getStageId());

        LocalDateTime now = LocalDateTime.now();
        WorkflowStep step = new WorkflowStep();
        step.setStageId(request.getStageId());
        step.setTitle(request.getTitle());
        step.setContent(request.getContent());
        step.setInputDesc(request.getInputDesc());
        step.setOutputDesc(request.getOutputDesc());
        step.setTips(request.getTips());
        step.setSortOrder(defaultIfNull(request.getSortOrder(), DEFAULT_SORT_ORDER));
        step.setStatus(defaultIfNull(request.getStatus(), DEFAULT_STATUS));
        step.setCreateTime(now);
        step.setUpdateTime(now);
        step.setIsDeleted(0);
        workflowStepMapper.insert(step);
        return toStepVO(step);
    }

    @Override
    public WorkflowStepVO updateStep(Long id, WorkflowStepUpdateRequest request) {
        WorkflowStep step = getStepEntity(id);
        getStageEntity(request.getStageId());

        step.setStageId(request.getStageId());
        step.setTitle(request.getTitle());
        step.setContent(request.getContent());
        step.setInputDesc(request.getInputDesc());
        step.setOutputDesc(request.getOutputDesc());
        step.setTips(request.getTips());
        step.setSortOrder(defaultIfNull(request.getSortOrder(), DEFAULT_SORT_ORDER));
        step.setStatus(defaultIfNull(request.getStatus(), DEFAULT_STATUS));
        step.setUpdateTime(LocalDateTime.now());
        workflowStepMapper.updateById(step);
        return toStepVO(step);
    }

    @Override
    public void deleteStep(Long id) {
        getStepEntity(id);
        workflowStepMapper.deleteById(id);
    }

    private WorkflowStage getStageEntity(Long id) {
        WorkflowStage stage = workflowStageMapper.selectById(id);
        if (stage == null) {
            throw new BusinessException("设计阶段不存在");
        }
        return stage;
    }

    private WorkflowStep getStepEntity(Long id) {
        WorkflowStep step = workflowStepMapper.selectById(id);
        if (step == null) {
            throw new BusinessException("工作流步骤不存在");
        }
        return step;
    }

    private void ensureStageCodeUnique(String code, Long excludeId) {
        WorkflowStage existing = workflowStageMapper.selectOne(new LambdaQueryWrapper<WorkflowStage>()
                .eq(WorkflowStage::getCode, code)
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new BusinessException("阶段编码已存在");
        }
    }

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private WorkflowStageVO toStageVO(WorkflowStage stage) {
        return WorkflowStageVO.builder()
                .id(stage.getId())
                .name(stage.getName())
                .code(stage.getCode())
                .description(stage.getDescription())
                .sortOrder(stage.getSortOrder())
                .status(stage.getStatus())
                .createTime(stage.getCreateTime())
                .updateTime(stage.getUpdateTime())
                .build();
    }

    private WorkflowStepVO toStepVO(WorkflowStep step) {
        return WorkflowStepVO.builder()
                .id(step.getId())
                .stageId(step.getStageId())
                .title(step.getTitle())
                .content(step.getContent())
                .inputDesc(step.getInputDesc())
                .outputDesc(step.getOutputDesc())
                .tips(step.getTips())
                .sortOrder(step.getSortOrder())
                .status(step.getStatus())
                .createTime(step.getCreateTime())
                .updateTime(step.getUpdateTime())
                .build();
    }
}

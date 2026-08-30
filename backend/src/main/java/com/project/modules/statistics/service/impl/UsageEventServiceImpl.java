package com.project.modules.statistics.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.exception.BusinessException;
import com.project.modules.statistics.dto.UsageEventCreateRequest;
import com.project.modules.statistics.entity.UsageEvent;
import com.project.modules.statistics.mapper.UsageEventMapper;
import com.project.modules.statistics.service.UsageEventService;
import com.project.modules.statistics.support.ExperimentEventCatalog;
import com.project.modules.statistics.vo.UsageEventVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UsageEventServiceImpl implements UsageEventService {

    private final UsageEventMapper usageEventMapper;
    private final ObjectMapper objectMapper;

    public UsageEventServiceImpl(UsageEventMapper usageEventMapper, ObjectMapper objectMapper) {
        this.usageEventMapper = usageEventMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public UsageEventVO createEvent(Long userId, UsageEventCreateRequest request) {
        String eventType = request.getEventType().trim().toLowerCase();
        String targetType = trimToNull(request.getTargetType());
        if (targetType != null) {
            targetType = targetType.toLowerCase();
        }
        ExperimentEventCatalog.validate(eventType, targetType, request.getTargetId());
        validateExtraJson(request.getExtraJson());

        LocalDateTime now = LocalDateTime.now();
        UsageEvent event = new UsageEvent();
        event.setUserId(userId);
        event.setAnonymousId(trimToNull(request.getAnonymousId()));
        event.setEventType(eventType);
        event.setTargetType(targetType);
        event.setTargetId(request.getTargetId());
        event.setPageUrl(trimToNull(request.getPageUrl()));
        event.setStayDuration(request.getStayDuration());
        event.setInputSummary(trimToNull(request.getInputSummary()));
        event.setExtraJson(trimToNull(request.getExtraJson()));
        event.setCreateTime(now);
        event.setUpdateTime(now);
        event.setIsDeleted(0);
        usageEventMapper.insert(event);
        return toVO(event);
    }

    private void validateExtraJson(String extraJson) {
        if (!StringUtils.hasText(extraJson)) {
            return;
        }
        try {
            if (!objectMapper.readTree(extraJson).isObject()) {
                throw new BusinessException("extraJson必须是JSON对象");
            }
        } catch (JsonProcessingException ex) {
            throw new BusinessException("extraJson不是合法JSON");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private UsageEventVO toVO(UsageEvent event) {
        return UsageEventVO.builder()
                .id(event.getId())
                .userId(event.getUserId())
                .anonymousId(event.getAnonymousId())
                .eventType(event.getEventType())
                .targetType(event.getTargetType())
                .targetId(event.getTargetId())
                .pageUrl(event.getPageUrl())
                .stayDuration(event.getStayDuration())
                .inputSummary(event.getInputSummary())
                .extraJson(event.getExtraJson())
                .createTime(event.getCreateTime())
                .build();
    }
}

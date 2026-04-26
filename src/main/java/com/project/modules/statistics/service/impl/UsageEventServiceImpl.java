package com.project.modules.statistics.service.impl;

import com.project.modules.statistics.dto.UsageEventCreateRequest;
import com.project.modules.statistics.entity.UsageEvent;
import com.project.modules.statistics.mapper.UsageEventMapper;
import com.project.modules.statistics.service.UsageEventService;
import com.project.modules.statistics.vo.UsageEventVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UsageEventServiceImpl implements UsageEventService {

    private static final int INPUT_SUMMARY_MAX_LENGTH = 500;

    private final UsageEventMapper usageEventMapper;

    public UsageEventServiceImpl(UsageEventMapper usageEventMapper) {
        this.usageEventMapper = usageEventMapper;
    }

    @Override
    public UsageEventVO createEvent(Long userId, UsageEventCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        UsageEvent event = new UsageEvent();
        event.setUserId(userId);
        event.setAnonymousId(request.getAnonymousId());
        event.setEventType(request.getEventType());
        event.setTargetType(request.getTargetType());
        event.setTargetId(request.getTargetId());
        event.setPageUrl(request.getPageUrl());
        event.setStayDuration(request.getStayDuration());
        event.setInputSummary(truncate(request.getInputSummary(), INPUT_SUMMARY_MAX_LENGTH));
        event.setExtraJson(request.getExtraJson());
        event.setCreateTime(now);
        event.setUpdateTime(now);
        event.setIsDeleted(0);
        usageEventMapper.insert(event);
        return toVO(event);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
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

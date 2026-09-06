package com.project.modules.statistics.service;

import com.project.modules.statistics.dto.UsageEventCreateRequest;
import com.project.modules.statistics.vo.UsageEventVO;

public interface UsageEventService {

    default UsageEventVO createEvent(Long userId, UsageEventCreateRequest request) {
        return createEvent(userId, request, null);
    }

    UsageEventVO createEvent(Long userId, UsageEventCreateRequest request, String idempotencyKey);
}

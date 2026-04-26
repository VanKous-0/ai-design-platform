package com.project.modules.statistics.service;

import com.project.modules.statistics.dto.UsageEventCreateRequest;
import com.project.modules.statistics.vo.UsageEventVO;

public interface UsageEventService {

    UsageEventVO createEvent(Long userId, UsageEventCreateRequest request);
}

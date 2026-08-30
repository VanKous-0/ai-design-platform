package com.project.modules.statistics.controller;

import com.project.common.result.Result;
import com.project.modules.statistics.dto.UsageEventCreateRequest;
import com.project.modules.statistics.service.UsageEventService;
import com.project.modules.statistics.vo.UsageEventVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage-events")
public class UsageEventController {

    private final UsageEventService usageEventService;

    public UsageEventController(UsageEventService usageEventService) {
        this.usageEventService = usageEventService;
    }

    @PostMapping
    public Result<UsageEventVO> createEvent(
            Authentication authentication,
            @Valid @RequestBody UsageEventCreateRequest request
    ) {
        return Result.success(usageEventService.createEvent(currentUserId(authentication), request));
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            return null;
        }
        return userId;
    }
}

package com.project.modules.achievement.service.impl;

import com.project.common.exception.BusinessException;
import com.project.modules.achievement.dto.ProjectAchievementSaveRequest;
import com.project.modules.achievement.entity.ProjectAchievement;
import com.project.modules.achievement.mapper.ProjectAchievementMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAchievementServiceImplTest {

    @Mock
    private ProjectAchievementMapper mapper;

    @Test
    void createNormalizesSupportedAchievementType() {
        when(mapper.selectOne(any())).thenReturn(null);
        ProjectAchievementServiceImpl service = new ProjectAchievementServiceImpl(mapper);
        ProjectAchievementSaveRequest request = request("design_work");

        service.createAchievement(request);

        ArgumentCaptor<ProjectAchievement> captor = ArgumentCaptor.forClass(ProjectAchievement.class);
        verify(mapper).insert(captor.capture());
        assertEquals("DESIGN_WORK", captor.getValue().getAchievementType());
        assertEquals(1, captor.getValue().getStatus());
    }

    @Test
    void rejectsUnsupportedAchievementType() {
        when(mapper.selectOne(any())).thenReturn(null);
        ProjectAchievementServiceImpl service = new ProjectAchievementServiceImpl(mapper);

        assertThrows(BusinessException.class, () -> service.createAchievement(request("PAPER")));
    }

    private ProjectAchievementSaveRequest request(String type) {
        ProjectAchievementSaveRequest request = new ProjectAchievementSaveRequest();
        request.setCode("ACH_TEST");
        request.setAchievementType(type);
        request.setTitle("测试成果");
        return request;
    }
}

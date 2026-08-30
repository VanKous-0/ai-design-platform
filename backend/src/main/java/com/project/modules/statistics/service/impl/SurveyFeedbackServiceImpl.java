package com.project.modules.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.modules.statistics.dto.SurveyFeedbackCreateRequest;
import com.project.modules.statistics.entity.SurveyFeedback;
import com.project.modules.statistics.mapper.SurveyFeedbackMapper;
import com.project.modules.statistics.service.SurveyFeedbackService;
import com.project.modules.statistics.vo.SurveyFeedbackVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SurveyFeedbackServiceImpl implements SurveyFeedbackService {

    private final SurveyFeedbackMapper surveyFeedbackMapper;

    public SurveyFeedbackServiceImpl(SurveyFeedbackMapper surveyFeedbackMapper) {
        this.surveyFeedbackMapper = surveyFeedbackMapper;
    }

    @Override
    public SurveyFeedbackVO createFeedback(Long userId, SurveyFeedbackCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        SurveyFeedback feedback = new SurveyFeedback();
        feedback.setUserId(userId);
        feedback.setAnonymousId(request.getAnonymousId());
        feedback.setScene(request.getScene());
        feedback.setScore(request.getScore());
        feedback.setContent(request.getContent());
        feedback.setContact(request.getContact());
        feedback.setCreateTime(now);
        feedback.setUpdateTime(now);
        feedback.setIsDeleted(0);
        surveyFeedbackMapper.insert(feedback);
        return toVO(feedback);
    }

    @Override
    public List<SurveyFeedbackVO> listFeedback() {
        return surveyFeedbackMapper.selectList(new LambdaQueryWrapper<SurveyFeedback>()
                        .orderByDesc(SurveyFeedback::getCreateTime)
                        .orderByDesc(SurveyFeedback::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    private SurveyFeedbackVO toVO(SurveyFeedback feedback) {
        return SurveyFeedbackVO.builder()
                .id(feedback.getId())
                .userId(feedback.getUserId())
                .anonymousId(feedback.getAnonymousId())
                .scene(feedback.getScene())
                .score(feedback.getScore())
                .content(feedback.getContent())
                .contact(feedback.getContact())
                .createTime(feedback.getCreateTime())
                .updateTime(feedback.getUpdateTime())
                .build();
    }
}

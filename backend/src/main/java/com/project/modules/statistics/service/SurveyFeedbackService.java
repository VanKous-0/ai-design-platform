package com.project.modules.statistics.service;

import com.project.modules.statistics.dto.SurveyFeedbackCreateRequest;
import com.project.modules.statistics.vo.SurveyFeedbackVO;

import java.util.List;

public interface SurveyFeedbackService {

    SurveyFeedbackVO createFeedback(Long userId, SurveyFeedbackCreateRequest request);

    List<SurveyFeedbackVO> listFeedback();
}

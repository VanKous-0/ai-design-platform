package com.project.modules.prompt.service;

import com.project.modules.prompt.dto.PromptCreateRequest;
import com.project.modules.prompt.dto.PromptToolSetRequest;
import com.project.modules.prompt.dto.PromptUpdateRequest;
import com.project.modules.prompt.vo.PromptDetailVO;
import com.project.modules.prompt.vo.PromptListVO;

import java.util.List;

public interface PromptService {

    List<PromptListVO> listPrompts(Long stageId, String category, String keyword);

    PromptDetailVO getPromptDetail(Long id);

    List<PromptListVO> searchPrompts(String keyword);

    List<PromptListVO> recommendPrompts(Long stageId, Long toolId);

    void copyPrompt(Long id);

    PromptDetailVO createPrompt(PromptCreateRequest request);

    PromptDetailVO updatePrompt(Long id, PromptUpdateRequest request);

    void deletePrompt(Long id);

    List<Long> setPromptTools(Long promptId, PromptToolSetRequest request);
}

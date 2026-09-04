package com.project.modules.prompt.service;

import com.project.common.result.PageResult;
import com.project.modules.prompt.dto.PromptCreateRequest;
import com.project.modules.prompt.dto.PromptParameterCreateRequest;
import com.project.modules.prompt.dto.PromptParameterUpdateRequest;
import com.project.modules.prompt.dto.PromptRenderRequest;
import com.project.modules.prompt.dto.PromptToolSetRequest;
import com.project.modules.prompt.dto.PromptUpdateRequest;
import com.project.modules.prompt.vo.PromptDetailVO;
import com.project.modules.prompt.vo.PromptListVO;
import com.project.modules.prompt.vo.PromptParameterVO;
import com.project.modules.prompt.vo.PromptRenderVO;
import com.project.modules.prompt.vo.PromptRevisionVO;

import java.util.List;

public interface PromptService {

    List<PromptListVO> listPrompts(Long stageId, String category, String keyword, String sourceType);

    PageResult<PromptListVO> pagePrompts(
            Long stageId,
            String category,
            String keyword,
            String sourceType,
            Long pageNum,
            Long pageSize
    );

    PromptDetailVO getPromptDetail(Long id);

    List<PromptListVO> searchPrompts(String keyword);

    List<PromptListVO> recommendPrompts(Long stageId, Long toolId);

    List<PromptListVO> listPromptsByNode(Long nodeId);

    List<PromptParameterVO> listParameters(Long promptId);

    PromptRenderVO renderPrompt(Long promptId, PromptRenderRequest request);

    List<PromptRevisionVO> listRevisions(Long promptId);

    PromptRevisionVO getRevision(Long promptId, Long revisionId);

    void copyPrompt(Long id);

    PromptDetailVO createPrompt(PromptCreateRequest request, Long createdBy);

    PromptDetailVO updatePrompt(Long id, PromptUpdateRequest request, Long createdBy);

    void deletePrompt(Long id);

    List<Long> setPromptTools(Long promptId, PromptToolSetRequest request);

    PromptParameterVO createParameter(Long promptId, PromptParameterCreateRequest request, Long createdBy);

    PromptParameterVO updateParameter(Long id, PromptParameterUpdateRequest request, Long createdBy);

    void deleteParameter(Long id, Long createdBy);
}

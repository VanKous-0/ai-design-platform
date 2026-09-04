package com.project.modules.prompt.service;

import com.project.modules.prompt.entity.PromptRevision;
import com.project.modules.prompt.entity.PromptTemplate;
import com.project.modules.prompt.vo.PromptRenderVO;
import com.project.modules.prompt.vo.PromptRevisionVO;

import java.util.List;
import java.util.Map;

public interface PromptRevisionService {

    PromptRevision createRevision(PromptTemplate prompt, Long createdBy);

    PromptRevision requireRevision(Long promptId, Long revisionId);

    PromptRenderVO render(PromptTemplate prompt, Long revisionId, Map<String, String> values);

    List<PromptRevisionVO> listRevisions(Long promptId);

    PromptRevisionVO getRevision(Long promptId, Long revisionId);
}

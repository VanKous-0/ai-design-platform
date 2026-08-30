package com.project.modules.caseproject.controller;

import com.project.common.result.Result;
import com.project.modules.caseproject.service.CaseProjectService;
import com.project.modules.caseproject.vo.CaseAssetVO;
import com.project.modules.caseproject.vo.CaseProjectDetailVO;
import com.project.modules.caseproject.vo.CaseProjectListVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
public class CaseProjectController {

    private final CaseProjectService caseProjectService;

    public CaseProjectController(CaseProjectService caseProjectService) {
        this.caseProjectService = caseProjectService;
    }

    @GetMapping
    public Result<?> listCases(
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) Long toolId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize
    ) {
        if (pageNum != null || pageSize != null) {
            return Result.success(caseProjectService.pageCases(stageId, toolId, keyword, pageNum, pageSize));
        }
        return Result.success(caseProjectService.listCases(stageId, toolId, keyword));
    }

    @GetMapping("/{id}")
    public Result<CaseProjectDetailVO> getCase(@PathVariable Long id) {
        return Result.success(caseProjectService.getCaseDetail(id));
    }

    @GetMapping("/{id}/assets")
    public Result<List<CaseAssetVO>> listAssets(@PathVariable Long id) {
        return Result.success(caseProjectService.listAssets(id));
    }

    @GetMapping("/recommend")
    public Result<List<CaseProjectListVO>> recommendCases(
            @RequestParam Long stageId,
            @RequestParam(required = false) Long toolId
    ) {
        return Result.success(caseProjectService.recommendCases(stageId, toolId));
    }
}

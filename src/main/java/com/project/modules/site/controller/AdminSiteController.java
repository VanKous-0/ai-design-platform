package com.project.modules.site.controller;

import com.project.common.result.Result;
import com.project.modules.site.dto.AwardRecordCreateRequest;
import com.project.modules.site.dto.AwardRecordUpdateRequest;
import com.project.modules.site.dto.SiteContentCreateRequest;
import com.project.modules.site.dto.SiteContentUpdateRequest;
import com.project.modules.site.service.SiteService;
import com.project.modules.site.vo.AwardRecordVO;
import com.project.modules.site.vo.SiteContentVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSiteController {

    private final SiteService siteService;

    public AdminSiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @PostMapping("/site/contents")
    public Result<SiteContentVO> createContent(@Valid @RequestBody SiteContentCreateRequest request) {
        return Result.success(siteService.createContent(request));
    }

    @PutMapping("/site/contents/{id}")
    public Result<SiteContentVO> updateContent(
            @PathVariable Long id,
            @Valid @RequestBody SiteContentUpdateRequest request
    ) {
        return Result.success(siteService.updateContent(id, request));
    }

    @DeleteMapping("/site/contents/{id}")
    public Result<Void> deleteContent(@PathVariable Long id) {
        siteService.deleteContent(id);
        return Result.success();
    }

    @PostMapping("/awards")
    public Result<AwardRecordVO> createAward(@Valid @RequestBody AwardRecordCreateRequest request) {
        return Result.success(siteService.createAward(request));
    }

    @PutMapping("/awards/{id}")
    public Result<AwardRecordVO> updateAward(
            @PathVariable Long id,
            @Valid @RequestBody AwardRecordUpdateRequest request
    ) {
        return Result.success(siteService.updateAward(id, request));
    }

    @DeleteMapping("/awards/{id}")
    public Result<Void> deleteAward(@PathVariable Long id) {
        siteService.deleteAward(id);
        return Result.success();
    }
}

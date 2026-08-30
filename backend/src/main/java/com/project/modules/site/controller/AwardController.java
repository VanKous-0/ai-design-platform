package com.project.modules.site.controller;

import com.project.common.result.Result;
import com.project.modules.site.service.SiteService;
import com.project.modules.site.vo.AwardRecordVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/awards")
public class AwardController {

    private final SiteService siteService;

    public AwardController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public Result<List<AwardRecordVO>> listAwards() {
        return Result.success(siteService.listAwards());
    }

    @GetMapping("/{id}")
    public Result<AwardRecordVO> getAward(@PathVariable Long id) {
        return Result.success(siteService.getAwardDetail(id));
    }
}

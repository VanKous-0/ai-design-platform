package com.project.modules.site.controller;

import com.project.common.result.Result;
import com.project.modules.site.service.SiteService;
import com.project.modules.site.vo.HomeVO;
import com.project.modules.site.vo.SiteContentVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/site")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping("/home")
    public Result<HomeVO> getHome() {
        return Result.success(siteService.getHome());
    }

    @GetMapping("/contents")
    public Result<List<SiteContentVO>> listContents(@RequestParam(required = false) String sectionKey) {
        return Result.success(siteService.listContents(sectionKey));
    }

    @GetMapping("/contents/{id}")
    public Result<SiteContentVO> getContent(@PathVariable Long id) {
        return Result.success(siteService.getContentDetail(id));
    }
}

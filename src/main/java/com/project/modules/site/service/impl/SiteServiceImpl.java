package com.project.modules.site.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.common.exception.BusinessException;
import com.project.modules.site.dto.AwardRecordCreateRequest;
import com.project.modules.site.dto.AwardRecordUpdateRequest;
import com.project.modules.site.dto.SiteContentCreateRequest;
import com.project.modules.site.dto.SiteContentUpdateRequest;
import com.project.modules.site.entity.AwardRecord;
import com.project.modules.site.entity.SiteContent;
import com.project.modules.site.mapper.AwardRecordMapper;
import com.project.modules.site.mapper.SiteContentMapper;
import com.project.modules.site.service.SiteService;
import com.project.modules.site.vo.AwardRecordVO;
import com.project.modules.site.vo.HomeVO;
import com.project.modules.site.vo.SiteContentVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SiteServiceImpl implements SiteService {

    private static final int STATUS_ENABLED = 1;
    private static final int DEFAULT_STATUS = 1;
    private static final int DEFAULT_SORT_ORDER = 0;

    private static final String HERO = "hero";
    private static final String INTRO = "intro";
    private static final String WORKFLOW_ENTRY = "workflow_entry";
    private static final String TOOL_RECOMMEND_ENTRY = "tool_recommend_entry";
    private static final String PROMPT_ENTRY = "prompt_entry";
    private static final String CASE_ENTRY = "case_entry";
    private static final String REVIEW_ENTRY = "review_entry";
    private static final String CONTACT = "contact";

    private final SiteContentMapper siteContentMapper;
    private final AwardRecordMapper awardRecordMapper;

    public SiteServiceImpl(SiteContentMapper siteContentMapper, AwardRecordMapper awardRecordMapper) {
        this.siteContentMapper = siteContentMapper;
        this.awardRecordMapper = awardRecordMapper;
    }

    @Override
    public HomeVO getHome() {
        List<SiteContentVO> contents = listContents(null);
        Map<String, List<SiteContentVO>> sections = contents.stream()
                .collect(Collectors.groupingBy(SiteContentVO::getSectionKey, LinkedHashMap::new, Collectors.toList()));

        return HomeVO.builder()
                .hero(sections.getOrDefault(HERO, List.of()))
                .intro(sections.getOrDefault(INTRO, List.of()))
                .workflowEntry(sections.getOrDefault(WORKFLOW_ENTRY, List.of()))
                .toolRecommendEntry(sections.getOrDefault(TOOL_RECOMMEND_ENTRY, List.of()))
                .promptEntry(sections.getOrDefault(PROMPT_ENTRY, List.of()))
                .caseEntry(sections.getOrDefault(CASE_ENTRY, List.of()))
                .reviewEntry(sections.getOrDefault(REVIEW_ENTRY, List.of()))
                .contact(sections.getOrDefault(CONTACT, List.of()))
                .sections(sections)
                .awards(listAwards())
                .build();
    }

    @Override
    public List<SiteContentVO> listContents(String sectionKey) {
        LambdaQueryWrapper<SiteContent> queryWrapper = enabledContentQuery()
                .orderByAsc(SiteContent::getSortOrder)
                .orderByAsc(SiteContent::getId);
        if (StringUtils.hasText(sectionKey)) {
            queryWrapper.eq(SiteContent::getSectionKey, sectionKey.trim());
        }
        return siteContentMapper.selectList(queryWrapper)
                .stream()
                .map(this::toContentVO)
                .toList();
    }

    @Override
    public SiteContentVO getContentDetail(Long id) {
        return toContentVO(getEnabledContentEntity(id));
    }

    @Override
    public SiteContentVO createContent(SiteContentCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        SiteContent content = new SiteContent();
        fillContent(content, request.getSectionKey(), request.getTitle(), request.getSubtitle(), request.getContent(),
                request.getImageUrl(), request.getLinkUrl(), request.getExtraJson(), request.getSortOrder(), request.getStatus());
        content.setCreateTime(now);
        content.setUpdateTime(now);
        content.setIsDeleted(0);
        siteContentMapper.insert(content);
        return toContentVO(content);
    }

    @Override
    public SiteContentVO updateContent(Long id, SiteContentUpdateRequest request) {
        SiteContent content = getContentEntity(id);
        fillContent(content, request.getSectionKey(), request.getTitle(), request.getSubtitle(), request.getContent(),
                request.getImageUrl(), request.getLinkUrl(), request.getExtraJson(), request.getSortOrder(), request.getStatus());
        content.setUpdateTime(LocalDateTime.now());
        siteContentMapper.updateById(content);
        return toContentVO(content);
    }

    @Override
    public void deleteContent(Long id) {
        getContentEntity(id);
        siteContentMapper.deleteById(id);
    }

    @Override
    public List<AwardRecordVO> listAwards() {
        return awardRecordMapper.selectList(enabledAwardQuery()
                        .orderByAsc(AwardRecord::getSortOrder)
                        .orderByAsc(AwardRecord::getId))
                .stream()
                .map(this::toAwardVO)
                .toList();
    }

    @Override
    public AwardRecordVO getAwardDetail(Long id) {
        return toAwardVO(getEnabledAwardEntity(id));
    }

    @Override
    public AwardRecordVO createAward(AwardRecordCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        AwardRecord award = new AwardRecord();
        fillAward(award, request.getTitle(), request.getAwardLevel(), request.getIssuer(), request.getAwardDate(),
                request.getSummary(), request.getImageUrl(), request.getLinkUrl(), request.getSortOrder(), request.getStatus());
        award.setCreateTime(now);
        award.setUpdateTime(now);
        award.setIsDeleted(0);
        awardRecordMapper.insert(award);
        return toAwardVO(award);
    }

    @Override
    public AwardRecordVO updateAward(Long id, AwardRecordUpdateRequest request) {
        AwardRecord award = getAwardEntity(id);
        fillAward(award, request.getTitle(), request.getAwardLevel(), request.getIssuer(), request.getAwardDate(),
                request.getSummary(), request.getImageUrl(), request.getLinkUrl(), request.getSortOrder(), request.getStatus());
        award.setUpdateTime(LocalDateTime.now());
        awardRecordMapper.updateById(award);
        return toAwardVO(award);
    }

    @Override
    public void deleteAward(Long id) {
        getAwardEntity(id);
        awardRecordMapper.deleteById(id);
    }

    private LambdaQueryWrapper<SiteContent> enabledContentQuery() {
        return new LambdaQueryWrapper<SiteContent>()
                .eq(SiteContent::getStatus, STATUS_ENABLED);
    }

    private LambdaQueryWrapper<AwardRecord> enabledAwardQuery() {
        return new LambdaQueryWrapper<AwardRecord>()
                .eq(AwardRecord::getStatus, STATUS_ENABLED);
    }

    private void fillContent(
            SiteContent content,
            String sectionKey,
            String title,
            String subtitle,
            String body,
            String imageUrl,
            String linkUrl,
            String extraJson,
            Integer sortOrder,
            Integer status
    ) {
        content.setSectionKey(sectionKey);
        content.setTitle(title);
        content.setSubtitle(subtitle);
        content.setContent(body);
        content.setImageUrl(imageUrl);
        content.setLinkUrl(linkUrl);
        content.setExtraJson(extraJson);
        content.setSortOrder(defaultIfNull(sortOrder, DEFAULT_SORT_ORDER));
        content.setStatus(defaultIfNull(status, DEFAULT_STATUS));
    }

    private void fillAward(
            AwardRecord award,
            String title,
            String awardLevel,
            String issuer,
            java.time.LocalDate awardDate,
            String summary,
            String imageUrl,
            String linkUrl,
            Integer sortOrder,
            Integer status
    ) {
        award.setTitle(title);
        award.setAwardLevel(awardLevel);
        award.setIssuer(issuer);
        award.setAwardDate(awardDate);
        award.setSummary(summary);
        award.setImageUrl(imageUrl);
        award.setLinkUrl(linkUrl);
        award.setSortOrder(defaultIfNull(sortOrder, DEFAULT_SORT_ORDER));
        award.setStatus(defaultIfNull(status, DEFAULT_STATUS));
    }

    private SiteContent getContentEntity(Long id) {
        SiteContent content = siteContentMapper.selectById(id);
        if (content == null) {
            throw new BusinessException("site content not found");
        }
        return content;
    }

    private SiteContent getEnabledContentEntity(Long id) {
        SiteContent content = siteContentMapper.selectOne(enabledContentQuery()
                .eq(SiteContent::getId, id)
                .last("limit 1"));
        if (content == null) {
            throw new BusinessException("site content not found or disabled");
        }
        return content;
    }

    private AwardRecord getAwardEntity(Long id) {
        AwardRecord award = awardRecordMapper.selectById(id);
        if (award == null) {
            throw new BusinessException("award record not found");
        }
        return award;
    }

    private AwardRecord getEnabledAwardEntity(Long id) {
        AwardRecord award = awardRecordMapper.selectOne(enabledAwardQuery()
                .eq(AwardRecord::getId, id)
                .last("limit 1"));
        if (award == null) {
            throw new BusinessException("award record not found or disabled");
        }
        return award;
    }

    private SiteContentVO toContentVO(SiteContent content) {
        return SiteContentVO.builder()
                .id(content.getId())
                .sectionKey(content.getSectionKey())
                .title(content.getTitle())
                .subtitle(content.getSubtitle())
                .content(content.getContent())
                .imageUrl(content.getImageUrl())
                .linkUrl(content.getLinkUrl())
                .extraJson(content.getExtraJson())
                .sortOrder(content.getSortOrder())
                .status(content.getStatus())
                .createTime(content.getCreateTime())
                .updateTime(content.getUpdateTime())
                .build();
    }

    private AwardRecordVO toAwardVO(AwardRecord award) {
        return AwardRecordVO.builder()
                .id(award.getId())
                .title(award.getTitle())
                .awardLevel(award.getAwardLevel())
                .issuer(award.getIssuer())
                .awardDate(award.getAwardDate())
                .summary(award.getSummary())
                .imageUrl(award.getImageUrl())
                .linkUrl(award.getLinkUrl())
                .sortOrder(award.getSortOrder())
                .status(award.getStatus())
                .createTime(award.getCreateTime())
                .updateTime(award.getUpdateTime())
                .build();
    }

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}

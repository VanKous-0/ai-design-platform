package com.project.common.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.common.exception.BusinessException;

public final class PageSupport {

    private static final long MAX_PAGE_SIZE = 100;

    private PageSupport() {
    }

    public static <T> Page<T> page(Long pageNum, Long pageSize) {
        long actualPageNum = pageNum == null ? 1L : pageNum;
        long actualPageSize = pageSize == null ? 20L : pageSize;
        if (actualPageNum < 1) {
            throw new BusinessException("pageNum不能小于1");
        }
        if (actualPageSize < 1 || actualPageSize > MAX_PAGE_SIZE) {
            throw new BusinessException("pageSize必须在1到100之间");
        }
        return new Page<>(actualPageNum, actualPageSize);
    }
}

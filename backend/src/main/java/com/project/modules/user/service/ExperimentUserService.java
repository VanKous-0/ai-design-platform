package com.project.modules.user.service;

import com.project.common.result.PageResult;
import com.project.modules.user.dto.ExperimentUserBatchCreateRequest;
import com.project.modules.user.vo.ExperimentUserCredentialVO;
import com.project.modules.user.vo.ExperimentUserVO;

import java.util.List;

public interface ExperimentUserService {

    List<ExperimentUserCredentialVO> createBatch(ExperimentUserBatchCreateRequest request);

    PageResult<ExperimentUserVO> pageUsers(
            String experimentBatch,
            String experimentGroup,
            String keyword,
            Long pageNum,
            Long pageSize
    );

    ExperimentUserVO updateStatus(Long userId, Integer status);

    void resetPassword(Long userId, String newPassword);
}

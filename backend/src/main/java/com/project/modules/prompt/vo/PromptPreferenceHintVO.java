package com.project.modules.prompt.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromptPreferenceHintVO {

    private Long id;

    private String preferenceKey;

    private String preferenceValue;
}

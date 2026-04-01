package com.zxy.aiplanner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI知识点梳理历史表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ai_synthesis_history")
public class TAiSynthesisHistory extends BaseEntity {

    private Long userId;
    private Long examArchiveId;
    private String synthesisTitle;
    private String promptText;
    private String aiResponseText;
    private String modelName;
    private Integer synthesisStatus;
    private Integer tokenUsage;
}

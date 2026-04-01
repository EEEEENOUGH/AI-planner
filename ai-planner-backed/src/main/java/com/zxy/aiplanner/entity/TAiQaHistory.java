package com.zxy.aiplanner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI答疑历史表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ai_qa_history")
public class TAiQaHistory extends BaseEntity {

    private Long userId;
    private Long examArchiveId;
    private String questionTitle;
    private String questionText;
    private String promptText;
    private String aiAnswerText;
    private String modelName;
    private Integer answerStatus;
    private Integer tokenUsage;
}

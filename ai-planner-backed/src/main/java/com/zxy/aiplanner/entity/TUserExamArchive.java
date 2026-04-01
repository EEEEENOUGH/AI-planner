package com.zxy.aiplanner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 用户考研备考档案表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_exam_archive")
public class TUserExamArchive extends BaseEntity {

    private Long userId;
    private String archiveName;
    private String targetInstitution;
    private String targetMajor;
    private String examSubjects;
    private Integer dailyStudyDuration;
    private String subjectMastery;
    private LocalDate examDate;
    private Integer archiveStatus;
}

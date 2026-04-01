package com.zxy.aiplanner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxy.aiplanner.entity.TUserExamArchive;

import java.time.LocalDate;

/**
 * 针对表【t_user_exam_archive】的数据库操作 Service
 */
public interface TUserExamArchiveService extends IService<TUserExamArchive> {

    /**
     * 获取用户当前启用的备考档案（最新一条）
     *
     * @param userId 用户ID
     * @return 档案实体，不存在则返回 null
     */
    TUserExamArchive getActiveArchive(Long userId);

    /**
     * 保存或更新备考档案（存在则更新，不存在则新增）
     *
     * @param userId           用户ID
     * @param archiveName      档案名称（为空时新建默认"我的考研档案"）
     * @param targetInstitution 目标院校
     * @param targetMajor      目标专业
     * @param examDate         考试日期
     * @param examSubjects     考试科目
     * @param subjectMastery   科目掌握情况
     * @param dailyStudyDuration 每日复习时长（小时）
     * @return 保存后的档案实体
     */
    TUserExamArchive saveOrUpdate(Long userId,
                                  String archiveName,
                                  String targetInstitution,
                                  String targetMajor,
                                  LocalDate examDate,
                                  String examSubjects,
                                  String subjectMastery,
                                  Integer dailyStudyDuration);
}

package com.zxy.aiplanner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxy.aiplanner.entity.TUserExamArchive;
import com.zxy.aiplanner.mapper.TUserExamArchiveMapper;
import com.zxy.aiplanner.service.TUserExamArchiveService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 针对表【t_user_exam_archive】的数据库操作 Service 实现
 */
@Service
public class TUserExamArchiveServiceImpl
        extends ServiceImpl<TUserExamArchiveMapper, TUserExamArchive>
        implements TUserExamArchiveService {

    @Override
    public TUserExamArchive getActiveArchive(Long userId) {
        return lambdaQuery()
                .eq(TUserExamArchive::getUserId, userId)
                .eq(TUserExamArchive::getArchiveStatus, 1)
                .orderByDesc(TUserExamArchive::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public TUserExamArchive saveOrUpdate(Long userId,
                                         String archiveName,
                                         String targetInstitution,
                                         String targetMajor,
                                         LocalDate examDate,
                                         String examSubjects,
                                         String subjectMastery,
                                         Integer dailyStudyDuration) {
        TUserExamArchive existing = getActiveArchive(userId);
        if (existing != null) {
            existing.setTargetInstitution(targetInstitution);
            existing.setTargetMajor(targetMajor);
            existing.setExamDate(examDate);
            existing.setExamSubjects(examSubjects == null ? "" : examSubjects);
            existing.setSubjectMastery(subjectMastery == null ? "" : subjectMastery);
            existing.setDailyStudyDuration(dailyStudyDuration == null ? 6 : dailyStudyDuration);
            if (archiveName != null && !archiveName.isBlank()) {
                existing.setArchiveName(archiveName);
            }
            updateById(existing);
            return existing;
        } else {
            TUserExamArchive newArchive = new TUserExamArchive();
            newArchive.setUserId(userId);
            newArchive.setArchiveName(
                    archiveName != null && !archiveName.isBlank() ? archiveName : "我的考研档案");
            newArchive.setTargetInstitution(targetInstitution);
            newArchive.setTargetMajor(targetMajor);
            newArchive.setExamDate(examDate);
            newArchive.setExamSubjects(examSubjects == null ? "" : examSubjects);
            newArchive.setSubjectMastery(subjectMastery == null ? "" : subjectMastery);
            newArchive.setDailyStudyDuration(dailyStudyDuration == null ? 6 : dailyStudyDuration);
            newArchive.setArchiveStatus(1);
            save(newArchive);
            return newArchive;
        }
    }
}

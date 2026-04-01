package com.zxy.aiplanner.controller;

import com.zxy.aiplanner.annotation.OperateLog;
import com.zxy.aiplanner.common.Result;
import com.zxy.aiplanner.constant.OperationTypeConstants;
import com.zxy.aiplanner.entity.TUserExamArchive;
import com.zxy.aiplanner.exception.BusinessException;
import com.zxy.aiplanner.service.TUserExamArchiveService;
import com.zxy.aiplanner.utils.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 用户备考档案接口
 * Controller 只负责：鉴权、参数校验、调用 Service、返回结果
 */
@Validated
@RestController
@RequestMapping("/api/v1/archive")
public class UserExamArchiveController {

    private final TUserExamArchiveService archiveService;

    public UserExamArchiveController(TUserExamArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    // ----------------------------- VO / DTO -----------------------------

    public record ArchiveVO(
            String archiveName,
            String targetInstitution,
            String targetMajor,
            String examDate,
            String examSubject,
            String subjectMastery,
            Integer dailyStudyDuration,
            long daysUntilExam
    ) {}

    public record ArchiveSaveDTO(
            @NotBlank(message = "目标院校不能为空")
            String targetInstitution,

            @NotBlank(message = "目标专业不能为空")
            String targetMajor,

            @NotNull(message = "考试日期不能为空")
            String examDate,

            String examSubject,
            String subjectMastery,
            Integer dailyStudyDuration,
            String archiveName
    ) {}

    // ----------------------------- 接口 -----------------------------

    /**
     * 查询当前用户的备考档案
     * GET /api/v1/archive/me
     */
    @OperateLog(module = "备考档案", type = OperationTypeConstants.ARCHIVE_QUERY)
    @GetMapping("/me")
    public Result<ArchiveVO> getMyArchive() {
        Long userId = requireUserId();
        TUserExamArchive archive = archiveService.getActiveArchive(userId);
        return Result.success(archive == null ? null : toVO(archive));
    }

    /**
     * 保存或更新当前用户的备考档案
     * POST /api/v1/archive/save
     */
    @OperateLog(module = "备考档案", type = OperationTypeConstants.ARCHIVE_SAVE)
    @PostMapping("/save")
    public Result<ArchiveVO> saveArchive(@RequestBody @Valid ArchiveSaveDTO dto) {
        Long userId = requireUserId();

        LocalDate examDate;
        try {
            examDate = LocalDate.parse(dto.examDate());
        } catch (Exception e) {
            throw new BusinessException(400, "考试日期格式不正确，请使用 yyyy-MM-dd 格式");
        }
        if (examDate.isBefore(LocalDate.now())) {
            throw new BusinessException(400, "考试日期不能早于今天");
        }

        TUserExamArchive archive = archiveService.saveOrUpdate(
                userId,
                dto.archiveName(),
                dto.targetInstitution(),
                dto.targetMajor(),
                examDate,
                dto.examSubject(),
                dto.subjectMastery(),
                dto.dailyStudyDuration()
        );
        return Result.success(toVO(archive));
    }

    // ----------------------------- 私有工具方法 -----------------------------

    private Long requireUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new BusinessException(401, "未登录或token已过期");
        return userId;
    }

    private ArchiveVO toVO(TUserExamArchive archive) {
        long daysUntilExam = 0;
        if (archive.getExamDate() != null) {
            daysUntilExam = ChronoUnit.DAYS.between(LocalDate.now(), archive.getExamDate());
            if (daysUntilExam < 0) daysUntilExam = 0;
        }
        return new ArchiveVO(
                archive.getArchiveName(),
                archive.getTargetInstitution(),
                archive.getTargetMajor(),
                archive.getExamDate() == null ? null : archive.getExamDate().toString(),
                archive.getExamSubjects(),
                archive.getSubjectMastery(),
                archive.getDailyStudyDuration(),
                daysUntilExam
        );
    }
}

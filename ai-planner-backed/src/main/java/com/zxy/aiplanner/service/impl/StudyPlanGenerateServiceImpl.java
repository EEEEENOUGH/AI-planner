package com.zxy.aiplanner.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxy.aiplanner.controller.StudyPlanController.PlanWithStagesVO;
import com.zxy.aiplanner.controller.StudyPlanController.StageVO;
import com.zxy.aiplanner.entity.TStudyPlanStage;
import com.zxy.aiplanner.entity.TStudyPlanTotal;
import com.zxy.aiplanner.entity.TUserExamArchive;
import com.zxy.aiplanner.exception.BusinessException;
import com.zxy.aiplanner.service.DeepSeekService;
import com.zxy.aiplanner.service.StudyPlanGenerateService;
import com.zxy.aiplanner.service.TStudyPlanStageService;
import com.zxy.aiplanner.service.TStudyPlanTotalService;
import com.zxy.aiplanner.service.TUserExamArchiveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 生成阶段规划业务 Service 实现
 */
@Service
public class StudyPlanGenerateServiceImpl implements StudyPlanGenerateService {

    private final TUserExamArchiveService archiveService;
    private final TStudyPlanTotalService planTotalService;
    private final TStudyPlanStageService planStageService;
    private final DeepSeekService deepSeekService;
    private final ObjectMapper objectMapper;

    public StudyPlanGenerateServiceImpl(TUserExamArchiveService archiveService,
                                         TStudyPlanTotalService planTotalService,
                                         TStudyPlanStageService planStageService,
                                         DeepSeekService deepSeekService,
                                         ObjectMapper objectMapper) {
        this.archiveService = archiveService;
        this.planTotalService = planTotalService;
        this.planStageService = planStageService;
        this.deepSeekService = deepSeekService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanWithStagesVO generateAndSave(Long userId) {
        // 1. 读取备考档案
        TUserExamArchive archive = archiveService.getActiveArchive(userId);
        if (archive == null) throw new BusinessException(400, "请先填写备考档案再生成规划");

        // 2. 构建 Prompt
        String prompt = buildPlanPrompt(archive);

        // 3. 调用 DeepSeek
        String aiRaw = deepSeekService.chatSingle(prompt);
        if (aiRaw == null || aiRaw.isBlank()) throw new BusinessException(502, "AI 规划生成失败，请稍后重试");

        // 4. 提取 JSON
        String jsonStr = extractJson(aiRaw);

        // 5. 解析并落库
        TStudyPlanTotal plan = parseAndSave(userId, archive, jsonStr);

        // 6. 查询阶段列表
        List<TStudyPlanStage> stages = planStageService.getStagesByPlanId(plan.getId());

        return toPlanVO(plan, stages);
    }

    // ==================== 私有方法 ====================

    private String buildPlanPrompt(TUserExamArchive archive) {
        long daysLeft = archive.getExamDate() != null
                ? ChronoUnit.DAYS.between(LocalDate.now(), archive.getExamDate()) : 300;
        return "你是一个专业的考研备考规划师。请根据以下考生信息，生成一份结构化的多阶段复习规划。\n\n"
                + "【考生信息】\n"
                + "- 目标院校：" + archive.getTargetInstitution() + "\n"
                + "- 目标专业：" + archive.getTargetMajor() + "\n"
                + "- 考试日期：" + (archive.getExamDate() != null ? archive.getExamDate() : "未填写") + "\n"
                + "- 距离考试：" + daysLeft + " 天\n"
                + "- 考试科目：" + (archive.getExamSubjects() != null ? archive.getExamSubjects() : "未填写") + "\n"
                + "- 每日复习时长：" + archive.getDailyStudyDuration() + " 小时\n"
                + "- 科目掌握情况：" + (archive.getSubjectMastery() != null ? archive.getSubjectMastery() : "未填写") + "\n\n"
                + "【输出要求】\n"
                + "1. 根据剩余天数合理划分 2~4 个阶段（基础/强化/突破/冲刺）。\n"
                + "2. 每阶段明确：阶段名、开始日期、结束日期、核心任务（200字以内）。\n"
                + "3. 日期格式严格使用 yyyy-MM-dd，第一个阶段开始日期为今天。\n"
                + "4. 只返回如下纯 JSON，不含任何 markdown 标记或额外文字：\n"
                + "{\n"
                + "  \"planName\": \"计划名称\",\n"
                + "  \"overallGoal\": \"总体目标\",\n"
                + "  \"stages\": [\n"
                + "    {\"stageType\":1,\"stageName\":\"基础阶段\",\"stageStartDate\":\"yyyy-MM-dd\","
                + "\"stageEndDate\":\"yyyy-MM-dd\",\"stageCoreTask\":\"...\"}\n"
                + "  ]\n"
                + "}\n"
                + "stageType：1=基础，2=强化，3=突破，4=冲刺，按顺序填写。";
    }

    private String extractJson(String raw) {
        String cleaned = raw.trim()
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start == -1 || end == -1 || start > end) {
            throw new BusinessException(502, "AI 返回格式异常，请重试");
        }
        return cleaned.substring(start, end + 1);
    }

    private TStudyPlanTotal parseAndSave(Long userId, TUserExamArchive archive, String jsonStr) {
        try {
            JsonNode root = objectMapper.readTree(jsonStr);

            // 停用旧计划
            planTotalService.disableAllPlans(userId);

            JsonNode stagesNode = root.path("stages");
            if (!stagesNode.isArray() || stagesNode.isEmpty()) {
                throw new BusinessException(502, "AI 未返回有效阶段数据，请重试");
            }

            LocalDate overallStart = null;
            LocalDate overallEnd = null;
            List<TStudyPlanStage> stageList = new ArrayList<>();

            for (JsonNode sn : stagesNode) {
                LocalDate startDate = parseDate(sn.path("stageStartDate").asText());
                LocalDate endDate = parseDate(sn.path("stageEndDate").asText());
                if (startDate == null || endDate == null) continue;
                if (overallStart == null || startDate.isBefore(overallStart)) overallStart = startDate;
                if (overallEnd == null || endDate.isAfter(overallEnd)) overallEnd = endDate;

                TStudyPlanStage stage = new TStudyPlanStage();
                stage.setStageType(sn.path("stageType").asInt(1));
                stage.setStageName(sn.path("stageName").asText("阶段" + stage.getStageType()));
                stage.setStageStartDate(startDate);
                stage.setStageEndDate(endDate);
                stage.setStagePlannedDays((int) ChronoUnit.DAYS.between(startDate, endDate) + 1);
                stage.setStageCoreTask(sn.path("stageCoreTask").asText(""));
                stage.setCreateSource(0);
                stage.setModifySource(0);
                stage.setStageStatus(1);
                stageList.add(stage);
            }

            if (overallStart == null) overallStart = LocalDate.now();
            if (overallEnd == null) overallEnd = archive.getExamDate() != null
                    ? archive.getExamDate() : LocalDate.now().plusMonths(6);

            TStudyPlanTotal plan = new TStudyPlanTotal();
            plan.setUserId(userId);
            plan.setExamArchiveId(archive.getId());
            plan.setPlanName(root.path("planName").asText(archive.getArchiveName() + "规划"));
            plan.setOverallStartDate(overallStart);
            plan.setOverallEndDate(overallEnd);
            plan.setDailyTargetMinutes(archive.getDailyStudyDuration() * 60);
            plan.setOverallGoal(root.path("overallGoal").asText(""));
            plan.setCreateSource(0);
            plan.setModifySource(0);
            plan.setPlanStatus(1);
            planTotalService.save(plan);

            for (TStudyPlanStage stage : stageList) {
                stage.setTotalPlanId(plan.getId());
            }
            planStageService.saveBatch(stageList);
            return plan;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(502, "AI 规划解析失败：" + e.getMessage());
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private PlanWithStagesVO toPlanVO(TStudyPlanTotal plan, List<TStudyPlanStage> stages) {
        return new PlanWithStagesVO(
                plan.getId(),
                plan.getPlanName(),
                plan.getOverallStartDate() != null ? plan.getOverallStartDate().toString() : null,
                plan.getOverallEndDate() != null ? plan.getOverallEndDate().toString() : null,
                plan.getDailyTargetMinutes(),
                plan.getOverallGoal(),
                plan.getCreateSource(),
                plan.getModifySource(),
                stages.stream().map(this::toStageVO).toList()
        );
    }

    private StageVO toStageVO(TStudyPlanStage stage) {
        return new StageVO(
                stage.getId(),
                stage.getStageType(),
                stage.getStageName(),
                stage.getStageStartDate() != null ? stage.getStageStartDate().toString() : null,
                stage.getStageEndDate() != null ? stage.getStageEndDate().toString() : null,
                stage.getStagePlannedDays(),
                stage.getStageCoreTask(),
                stage.getCreateSource(),
                stage.getModifySource(),
                stage.getStageStatus()
        );
    }
}

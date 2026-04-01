package com.zxy.aiplanner.controller;

import com.zxy.aiplanner.annotation.OperateLog;
import com.zxy.aiplanner.common.Result;
import com.zxy.aiplanner.entity.TStudyPlanTotal;
import com.zxy.aiplanner.exception.BusinessException;
import com.zxy.aiplanner.service.TStudyPlanTotalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 复习总计划接口骨架
 */
@RestController
@RequestMapping("/api/v1/plan")
public class TStudyPlanTotalController {

    private final TStudyPlanTotalService studyPlanTotalService;

    @Autowired
    public TStudyPlanTotalController(TStudyPlanTotalService studyPlanTotalService) {
        this.studyPlanTotalService = studyPlanTotalService;
    }

    @OperateLog(module = "复习总计划", type = 3)
    @GetMapping("/{id}")
    public Result<TStudyPlanTotal> getStudyPlanTotalById(@PathVariable("id") Long id) {
        TStudyPlanTotal studyPlanTotal = studyPlanTotalService.getById(id);
        if (studyPlanTotal == null) {
            throw new BusinessException(404, "复习总计划不存在");
        }
        return Result.success(studyPlanTotal);
    }
}

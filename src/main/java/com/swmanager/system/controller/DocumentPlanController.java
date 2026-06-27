package com.swmanager.system.controller;

import com.swmanager.system.dto.workplan.PlanData;
import com.swmanager.system.dto.workplan.PlanManpowerRow;
import com.swmanager.system.dto.workplan.PlanScheduleRow;
import com.swmanager.system.dto.workplan.PlanTargetRow;
import com.swmanager.system.repository.PjtManpowerPlanRepository;
import com.swmanager.system.repository.PjtScheduleRepository;
import com.swmanager.system.repository.PjtTargetRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.response.ApiResult;
import com.swmanager.system.security.DocumentAccessSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 사업수행계획서(plan, P10~P13) 컨트롤러 — DocumentController 에서 분리 (S4 Phase 3).
 *
 * <p>기획서/개발계획 docs/{product-specs,exec-plans}/refactor-document-controller-split-phase3.md.
 * plan 전용 repository 3종(PjtTarget/PjtManpowerPlan/PjtSchedule)을 동반 이동. 권한은
 * DocumentAccessSupport(admin→EDIT) 기준. savePlanData 의 @Transactional + rollback-only 보존.
 */
@Slf4j
@Controller
@RequestMapping("/document")
public class DocumentPlanController {

    @Autowired private SwProjectRepository swProjectRepository;
    @Autowired private PjtTargetRepository pjtTargetRepository;
    @Autowired private PjtManpowerPlanRepository pjtManpowerPlanRepository;
    @Autowired private PjtScheduleRepository pjtScheduleRepository;
    @Autowired private DocumentAccessSupport access;

    /** 사업수행계획서 데이터 조회 */
    @ResponseBody
    @GetMapping("/api/plan/{projId}")
    public ResponseEntity<?> getPlanData(@PathVariable Long projId) {
        if (!access.hasDocRead()) return ResponseEntity.status(403).build();  // [harden-read-api] VIEW 이상(allowlist, admin 포함)
        var pOpt = swProjectRepository.findById(projId);
        if (pOpt.isEmpty()) return ResponseEntity.notFound().build();
        var p = pOpt.get();

        List<PlanTargetRow> targets = pjtTargetRepository.findByProjIdOrderBySortOrderAsc(projId)
                .stream().map(PlanTargetRow::from).toList();
        List<PlanManpowerRow> manpowerPlans = pjtManpowerPlanRepository.findByProjIdOrderBySortOrderAsc(projId)
                .stream().map(PlanManpowerRow::from).toList();
        List<PlanScheduleRow> schedules = pjtScheduleRepository.findByProjIdOrderBySortOrderAsc(projId)
                .stream().map(PlanScheduleRow::from).toList();

        return ResponseEntity.ok(new PlanData(
                p.getProjPurpose(), p.getSupportType(), p.getScopeText(), p.getInspectMethod(),
                targets, manpowerPlans, schedules));
    }

    /** 사업수행계획서 데이터 저장 (overwrite) — 감사 P1-2 조치: EDIT 권한 체크 (2026-04-18) */
    @ResponseBody
    @PostMapping("/api/plan/{projId}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResult> savePlanData(
            @PathVariable Long projId,
            @RequestBody Map<String, Object> body) {
        if (!"EDIT".equals(access.getAuth())) {
            return ResponseEntity.status(403).body(ApiResult.fail("FORBIDDEN", "수정 권한이 없습니다"));
        }
        try {
            var pOpt = swProjectRepository.findById(projId);
            if (pOpt.isEmpty()) return ResponseEntity.notFound().build();
            var p = pOpt.get();

            // sw_pjt 4 columns
            if (body.containsKey("projPurpose"))   p.setProjPurpose((String) body.get("projPurpose"));
            if (body.containsKey("supportType"))   p.setSupportType((String) body.get("supportType"));
            if (body.containsKey("scopeText"))     p.setScopeText((String) body.get("scopeText"));
            if (body.containsKey("inspectMethod")) p.setInspectMethod((String) body.get("inspectMethod"));
            swProjectRepository.save(p);

            // targets - delete & insert
            if (body.containsKey("targets")) {
                pjtTargetRepository.deleteByProjId(projId);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> arr = (List<Map<String, Object>>) body.get("targets");
                int order = 0;
                for (Map<String, Object> m : arr) {
                    var t = new com.swmanager.system.domain.workplan.PjtTarget();
                    t.setProjId(projId);
                    t.setProductName((String) m.get("productName"));
                    Object q = m.get("qty");
                    t.setQty(q == null ? 1 : ((Number) q).intValue());
                    t.setSortOrder(order++);
                    pjtTargetRepository.save(t);
                }
            }

            // manpower plans
            if (body.containsKey("manpowerPlans")) {
                pjtManpowerPlanRepository.deleteByProjId(projId);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> arr = (List<Map<String, Object>>) body.get("manpowerPlans");
                int order = 0;
                for (Map<String, Object> m : arr) {
                    var mp = new com.swmanager.system.domain.workplan.PjtManpowerPlan();
                    mp.setProjId(projId);
                    mp.setStepName((String) m.get("stepName"));
                    String sd = (String) m.get("startDt");
                    String ed = (String) m.get("endDt");
                    if (sd != null && !sd.isEmpty()) mp.setStartDt(java.time.LocalDate.parse(sd));
                    if (ed != null && !ed.isEmpty()) mp.setEndDt(java.time.LocalDate.parse(ed));
                    mp.setGradeSpecial(asInt(m.get("gradeSpecial")));
                    mp.setGradeHigh(asInt(m.get("gradeHigh")));
                    mp.setGradeMid(asInt(m.get("gradeMid")));
                    mp.setGradeLow(asInt(m.get("gradeLow")));
                    mp.setFuncHigh(asInt(m.get("funcHigh")));
                    mp.setFuncMid(asInt(m.get("funcMid")));
                    mp.setFuncLow(asInt(m.get("funcLow")));
                    mp.setRemark((String) m.get("remark"));
                    mp.setSortOrder(order++);
                    pjtManpowerPlanRepository.save(mp);
                }
            }

            // schedules
            if (body.containsKey("schedules")) {
                pjtScheduleRepository.deleteByProjId(projId);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> arr = (List<Map<String, Object>>) body.get("schedules");
                int order = 0;
                for (Map<String, Object> m : arr) {
                    var s = new com.swmanager.system.domain.workplan.PjtSchedule();
                    s.setProjId(projId);
                    s.setProcessName((String) m.get("processName"));
                    s.setM01(asBool(m.get("m01"))); s.setM02(asBool(m.get("m02")));
                    s.setM03(asBool(m.get("m03"))); s.setM04(asBool(m.get("m04")));
                    s.setM05(asBool(m.get("m05"))); s.setM06(asBool(m.get("m06")));
                    s.setM07(asBool(m.get("m07"))); s.setM08(asBool(m.get("m08")));
                    s.setM09(asBool(m.get("m09"))); s.setM10(asBool(m.get("m10")));
                    s.setM11(asBool(m.get("m11"))); s.setM12(asBool(m.get("m12")));
                    s.setRemark((String) m.get("remark"));
                    s.setSortOrder(order++);
                    pjtScheduleRepository.save(s);
                }
            }

            return ResponseEntity.ok(ApiResult.ok());
        } catch (Exception e) {
            log.error("사업수행계획서 저장 실패: {}", e.getMessage(), e);
            // [codex] @Transactional 내 catch+정상 return 은 롤백을 막아 부분 저장/삭제(targets 삭제 후
            //         manpowerPlans/schedules 중 예외)가 commit 됨 → 명시적 rollback-only 로 정합성 보장.
            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
            // 응답 형태는 현행 보존: HTTP 200 + {success:false,error:<msg>}
            return ResponseEntity.ok(ApiResult.failMessage(e.getMessage()));
        }
    }

    private Integer asInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return 0; }
    }
    private Boolean asBool(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        return "true".equalsIgnoreCase(o.toString()) || "1".equals(o.toString());
    }
}

package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.zip.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

/**
 * HWPX 문서 생성 서비스
 * - 템플릿 HWPX 파일(ZIP 아카이브)을 로드하고 플레이스홀더 변수를 실제 데이터로 치환
 * - Contents/section0.xml 및 Preview/PrvText.txt 내 {{변수}} 치환
 */
@Slf4j
@Service
public class HwpxExportService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private com.swmanager.system.i18n.MessageResolver messages;

    @Autowired(required = false)
    private com.swmanager.system.repository.PjtTargetRepository pjtTargetRepository;

    @Autowired(required = false)
    private com.swmanager.system.repository.PjtManpowerPlanRepository pjtManpowerPlanRepository;

    @Autowired(required = false)
    private com.swmanager.system.repository.PjtScheduleRepository pjtScheduleRepository;

    @Autowired(required = false)
    private com.swmanager.system.repository.UserRepository userRepository;

    private static final String[] KOREAN_NUMS = {"영", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구"};
    private static final String[] KOREAN_UNITS = {"", "만", "억", "조"};

    /**
     * 숫자를 한글 금액 문자열로 변환
     * 예: 8778000 -> "팔백칠십칠만팔천"
     * 금/원 접두사/접미사는 포함하지 않음 (호출부에서 추가)
     */
    public static String convertToKoreanAmount(Long amount) {
        if (amount == null || amount == 0) return "영";

        List<String> parts = new ArrayList<>();
        int unitIdx = 0;
        long remaining = amount;

        while (remaining > 0) {
            int part = (int) (remaining % 10000);
            if (part > 0) {
                StringBuilder sb = new StringBuilder();
                int thousands = part / 1000;
                if (thousands > 0) {
                    if (thousands > 1) {
                        sb.append(KOREAN_NUMS[thousands]);
                    }
                    sb.append("천");
                }
                int hundreds = (part % 1000) / 100;
                if (hundreds > 0) {
                    if (hundreds > 1) {
                        sb.append(KOREAN_NUMS[hundreds]);
                    }
                    sb.append("백");
                }
                int tens = (part % 100) / 10;
                if (tens > 0) {
                    if (tens > 1) {
                        sb.append(KOREAN_NUMS[tens]);
                    }
                    sb.append("십");
                }
                int ones = part % 10;
                if (ones > 0) {
                    sb.append(KOREAN_NUMS[ones]);
                }
                sb.append(KOREAN_UNITS[unitIdx]);
                parts.add(0, sb.toString());
            }
            remaining /= 10000;
            unitIdx++;
        }
        return String.join("", parts);
    }

    /**
     * 수신처 두목(Head) 생성
     * 가평군 -> "가평군수 귀하"
     * 강릉시 -> "강릉시장 귀하"
     * 동대문구 -> "동대문구청장 귀하"
     */
    public static String buildRecipientHead(String cityNm, String distNm) {
        String target = (distNm != null && !distNm.isEmpty()) ? distNm : cityNm;
        if (target == null || target.isEmpty()) return "";

        String title;
        if (target.endsWith("군")) {
            title = target + "수";
        } else if (target.endsWith("시")) {
            title = target + "장";
        } else if (target.endsWith("구")) {
            title = target + "청장";
        } else {
            title = target + "장";
        }
        return title + " 귀하";
    }

    /**
     * 수신처 기관명 생성
     * 가평군 -> "가평군청"
     * 강릉시 -> "강릉시청"
     * orgNm이 있으면 우선 사용
     */
    public static String buildRecipientOrg(String cityNm, String distNm, String orgNm) {
        if (orgNm != null && !orgNm.isEmpty()) {
            return orgNm;
        }

        String target = (distNm != null && !distNm.isEmpty()) ? distNm : cityNm;
        if (target == null || target.isEmpty()) return "";

        if (target.endsWith("군") || target.endsWith("시")) {
            return target + "청";
        } else if (target.endsWith("구")) {
            return target + "청";
        }
        return target;
    }

    /**
     * 날짜 포맷: 2025-12-31 -> "2025. 12. 31."
     */
    public static String formatDate(LocalDate dt) {
        if (dt == null) return "";
        return String.format("%d. %d. %d.", dt.getYear(), dt.getMonthValue(), dt.getDayOfMonth());
    }

    /**
     * 날짜 한글 포맷: 2025-12-31 -> "2025년 12월 31일"
     */
    public static String formatDateKorean(LocalDate dt) {
        if (dt == null) return "";
        return String.format("%d년 %d월 %d일", dt.getYear(), dt.getMonthValue(), dt.getDayOfMonth());
    }

    /** 양쪽정렬 셀에서 정렬 깨짐 방지용 0-패딩 포맷 (2026년 01월 09일) */
    public static String formatDateKoreanPadded(LocalDate dt) {
        if (dt == null) return "";
        return String.format("%d년 %02d월 %02d일", dt.getYear(), dt.getMonthValue(), dt.getDayOfMonth());
    }

    /**
     * HWPX 문서 생성
     *
     * @param docId        문서 ID (DB)
     * @param templateType 서브 문서 유형:
     *                     "letter" = 공문, "inspector" = 기성검사원, "completion_body" = 준공계 본문
     * @return byte[] HWPX 파일 바이트 배열
     */
    public byte[] generateHwpx(Integer docId, String templateType) {
        Document doc = documentService.getDocumentById(docId);

        // 템플릿 파일 경로 결정 (commence_body는 사업 기간(개월)에 따라 분기)
        int months = 12;
        SwProject p = doc.getProject();
        if (p != null && p.getStartDt() != null && p.getEndDt() != null) {
            int sm = p.getStartDt().getYear() * 12 + p.getStartDt().getMonthValue();
            int em = p.getEndDt().getYear() * 12 + p.getEndDt().getMonthValue();
            months = em - sm + 1;
        }
        String templatePath = resolveTemplatePath(doc.getDocType(), templateType, months);

        // 치환 맵 생성
        Map<String, String> replacements = buildReplacements(doc, templateType);

        // 템플릿 처리
        return processTemplate(templatePath, replacements, doc);
    }

    /**
     * docType + templateType에 따른 템플릿 파일 경로 결정
     */
    private String resolveTemplatePath(DocumentType docType, String templateType, int months) {
        if ("letter".equals(templateType)) {
            if (docType == null) {
                throw new IllegalArgumentException(messages.get("error.document.type_empty"));
            }
            return switch (docType) {
                case COMMENCE -> "templates/hwpx/letter_commence.hwpx";
                case INTERIM -> "templates/hwpx/letter_interim.hwpx";
                case COMPLETION -> "templates/hwpx/letter_completion.hwpx";
                default -> throw new IllegalArgumentException(messages.get("error.document.type_unsupported", docType));
            };
        }
        if ("inspector".equals(templateType)) {
            return "templates/hwpx/interim_inspector.hwpx";
        }
        if ("commence_body".equals(templateType)) {
            // 단일 12m 템플릿 + {{월N_bf}} 동적 음영으로 통합
            return "templates/hwpx/commence_body.hwpx";
        }
        if ("completion_body".equals(templateType)) {
            // sysNmEn 기반으로 KRAS/UPIS 분기
            return "templates/hwpx/completion_kras.hwpx";
        }
        if ("completion_body_upis".equals(templateType)) {
            return "templates/hwpx/completion_upis.hwpx";
        }
        if ("completion_full".equals(templateType)) {
            return "templates/hwpx/completion_full_v1.hwpx";
        }
        throw new IllegalArgumentException(messages.get("error.document.type_unsupported", templateType));
    }

    /**
     * 문서 데이터로부터 치환 맵 생성
     * 각 템플릿 HWPX 파일의 {{플레이스홀더}}에 정확히 대응
     */
    private Map<String, String> buildReplacements(Document doc, String templateType) {
        Map<String, String> map = new LinkedHashMap<>();

        SwProject proj = doc.getProject();

        String projNm = (proj != null && proj.getProjNm() != null) ? proj.getProjNm() : "";
        String cityNm = (proj != null && proj.getCityNm() != null) ? proj.getCityNm() : "";
        String distNm = (proj != null && proj.getDistNm() != null) ? proj.getDistNm() : "";
        String orgNm = (proj != null && proj.getOrgNm() != null) ? proj.getOrgNm() : "";
        String docNo = doc.getDocNo() != null ? doc.getDocNo() : "";

        if ("letter".equals(templateType)) {
            Map<String, Object> letterData = getSectionData(doc, "letter");

            map.put("{{수신}}", buildRecipientOrg(cityNm, distNm, orgNm));
            map.put("{{담당}}", getStr(letterData, "manager", ""));
            map.put("{{연락처}}", getStr(letterData, "tel", ""));
            map.put("{{시행일자}}", getStr(letterData, "date", ""));
            map.put("{{용역명}}", projNm);

            // 문서번호: commence/completion은 _앞/_뒤 분리, interim은 단일
            DocumentType docType = doc.getDocType();
            if (DocumentType.COMMENCE == docType || DocumentType.COMPLETION == docType) {
                map.put("{{문서번호_앞}}", docNo);
                map.put("{{문서번호_뒤}}", "");
            } else {
                // INTERIM - 단일 문서번호
                map.put("{{문서번호}}", docNo);
            }

            // 제목: 용역명 + 접미사 분리
            String titleSuffix = docType == null ? "" : switch (docType) {
                case COMMENCE -> "착수계 제출 건";
                case INTERIM -> "기성금 신청 건";
                case COMPLETION -> "준공계 제출 건";
                default -> "";
            };
            map.put("{{제목_용역명}}", projNm);
            map.put("{{제목_접미사}}", titleSuffix);

            // 계약일자
            if (proj != null && proj.getContDt() != null) {
                map.put("{{계약일자}}", formatDate(proj.getContDt()));
            } else {
                map.put("{{계약일자}}", "");
            }
        }

        if ("inspector".equals(templateType)) {
            map.put("{{용역명}}", projNm);
            map.put("{{수신}}", buildRecipientHead(cityNm, distNm));

            Long contAmt = (proj != null && proj.getContAmt() != null) ? proj.getContAmt() : 0L;
            map.put("{{계약금액}}", "금" + String.format("%,d", contAmt) + "원");
            map.put("{{계약금액한글}}", "금" + convertToKoreanAmount(contAmt) + "원");

            // 계약일자 분리: 년도, 월, 일 — 셀 안에 년/월/일 라벨 포함
            if (proj != null && proj.getContDt() != null) {
                map.put("{{계약년도}}", proj.getContDt().getYear() + "년");
                map.put("{{계약월}}", String.format("%02d", proj.getContDt().getMonthValue()) + "월");
                map.put("{{계약일}}", String.format("%02d", proj.getContDt().getDayOfMonth()) + "일");
            } else {
                map.put("{{계약년도}}", ""); map.put("{{계약월}}", ""); map.put("{{계약일}}", "");
            }
            if (proj != null && proj.getStartDt() != null) {
                map.put("{{착수년}}", proj.getStartDt().getYear() + "년");
                map.put("{{착수월}}", String.format("%02d", proj.getStartDt().getMonthValue()) + "월");
                map.put("{{착수일}}", String.format("%02d", proj.getStartDt().getDayOfMonth()) + "일");
            } else {
                map.put("{{착수년}}", ""); map.put("{{착수월}}", ""); map.put("{{착수일}}", "");
            }
            if (proj != null && proj.getEndDt() != null) {
                map.put("{{준공년}}", proj.getEndDt().getYear() + "년");
                map.put("{{준공월}}", String.format("%02d", proj.getEndDt().getMonthValue()) + "월");
                map.put("{{준공일}}", String.format("%02d", proj.getEndDt().getDayOfMonth()) + "일");
            } else {
                map.put("{{준공년}}", ""); map.put("{{준공월}}", ""); map.put("{{준공일}}", "");
            }

            // 기성 관련 - inspector 섹션 데이터에서 가져옴 (프론트 키: paymentAmount, paymentRate, interimDate)
            Map<String, Object> inspData = getSectionData(doc, "inspector");
            String interimAmtStr = getStr(inspData, "paymentAmount", "");
            Long interimAmt = 0L;
            try {
                if (!interimAmtStr.isEmpty()) {
                    interimAmt = Long.parseLong(interimAmtStr.replaceAll("[^0-9]", ""));
                }
            } catch (NumberFormatException e) { /* ignore */ }
            if (interimAmt == 0L) {
                // 기성금액 미입력 시 계약금액 사용
                interimAmt = contAmt;
            }
            map.put("{{기성금액}}", "금" + String.format("%,d", interimAmt) + "원");
            map.put("{{기성금액한글}}", "금" + convertToKoreanAmount(interimAmt) + "원");
            // 기성일자: 년/월/일 분리 입력 우선, 없으면 interimDate 사용. 전각공백으로 정렬
            String iy = getStr(inspData, "interimYear", "");
            String im = getStr(inspData, "interimMonth", "");
            String id = getStr(inspData, "interimDay", "");
            String interimDateStr;
            if (!iy.isEmpty() || !im.isEmpty() || !id.isEmpty()) {
                String pm = im.isEmpty() ? "" : String.format("%02d", Integer.parseInt(im));
                int idn = id.isEmpty() ? 0 : Integer.parseInt(id);
                String pd = idn > 0 ? String.format("%02d", idn) : "";
                interimDateStr = iy + "년    " + pm + "월    " + pd + "일";
            } else {
                String raw = getStr(inspData, "interimDate", "");
                java.util.regex.Matcher mD = java.util.regex.Pattern
                        .compile("(\\d{4})\\D+(\\d{1,2})\\D+(\\d{0,2})").matcher(raw);
                if (mD.find()) {
                    String pm = String.format("%02d", Integer.parseInt(mD.group(2)));
                    int idn = mD.group(3).isEmpty() ? 0 : Integer.parseInt(mD.group(3));
                    String pd = idn > 0 ? String.format("%02d", idn) : "";
                    interimDateStr = mD.group(1) + "년    " + pm + "월    " + pd + "일";
                } else {
                    interimDateStr = raw;
                }
            }
            map.put("{{기성일자}}", interimDateStr);
            map.put("{{기성율}}", getStr(inspData, "paymentRate", "0"));
        }

        if ("commence_body".equals(templateType)) {
            map.put("{{용역명}}", projNm);
            map.put("{{수신}}", buildRecipientHead(cityNm, distNm));

            Long contAmt = (proj != null && proj.getContAmt() != null) ? proj.getContAmt() : 0L;
            map.put("{{계약금액}}", "금" + String.format("%,d", contAmt) + "원(금" + convertToKoreanAmount(contAmt) + "원)");

            // 계약기간
            String contractPeriod = "";
            if (proj != null && proj.getStartDt() != null && proj.getEndDt() != null) {
                contractPeriod = formatDateKorean(proj.getStartDt()) + " ~ " + formatDateKorean(proj.getEndDt());
            }
            map.put("{{계약기간}}", contractPeriod);

            // 제출일자 (착수월 기준)
            Map<String, Object> commData = getSectionData(doc, "commence");
            String dateStr = getStr(commData, "date", "");
            if (dateStr.isEmpty()) {
                if (proj != null && proj.getStartDt() != null) {
                    dateStr = String.format("%d년  %d월    일", proj.getStartDt().getYear(), proj.getStartDt().getMonthValue());
                } else {
                    dateStr = String.format("%d년  %d월    일", LocalDate.now().getYear(), LocalDate.now().getMonthValue());
                }
            }
            map.put("{{제출일자}}", dateStr);

            // 공정명: 예정공정표 폼의 processName 우선, 없으면 사업명에서 파생
            String sysNm = (proj != null && proj.getSysNm() != null) ? proj.getSysNm() : "";
            String sysNmEn = (proj != null && proj.getSysNmEn() != null) ? proj.getSysNmEn() : "";
            Map<String, Object> schedData = getSectionData(doc, "schedule");
            String processName = getStr(schedData, "processName", "");
            if (processName.isEmpty()) {
                processName = projNm;
                if (processName != null && !processName.isEmpty()) {
                    processName = processName.replaceFirst("^\\d{4}년\\s+\\S+\\s+", "")
                                             .replaceFirst("\\s*용역\\s*$", "");
                }
                if (processName == null || processName.isEmpty()) {
                    processName = sysNm.isEmpty() ? projNm : sysNm + "용 GIS SW 유지관리";
                }
            }
            map.put("{{공정명}}", processName);
            // 공정명 2줄 분할: ')' 기준 (공백 유무 무관).
            //   예: "부동산종합공부시스템(KRAS)용 GIS SW 유지관리"
            //        → 1줄: "부동산종합공부시스템(KRAS)"
            //        → 2줄: "용 GIS SW 유지관리"
            //   괄호 없으면 {{공정명1}}=전체, {{공정명2}}=빈 문자열
            String pn1 = processName, pn2 = "";
            int closeIdx = processName.indexOf(")");
            if (closeIdx > 0 && closeIdx < processName.length() - 1) {
                pn1 = processName.substring(0, closeIdx + 1);
                // ')' 바로 뒤 공백이 있으면 제거 (양 끝 trim)
                pn2 = processName.substring(closeIdx + 1).trim();
            }
            map.put("{{공정명1}}", pn1);
            map.put("{{공정명2}}", pn2);

            // 용역기간 (포함 개월수): startDt월 ~ endDt월 사이의 달 수
            int months = 12;
            int startMonth = 1, endMonth = 12;
            if (proj != null && proj.getStartDt() != null && proj.getEndDt() != null) {
                int sm = proj.getStartDt().getYear()*12 + proj.getStartDt().getMonthValue();
                int em = proj.getEndDt().getYear()*12 + proj.getEndDt().getMonthValue();
                months = em - sm + 1;
                if (months <= 0) months = 12;
                startMonth = proj.getStartDt().getMonthValue();
                endMonth = proj.getEndDt().getMonthValue();
                // 다른 해에 걸치면 단순 1~12 처리(향후 확장)
                if (proj.getStartDt().getYear() != proj.getEndDt().getYear()) {
                    startMonth = 1; endMonth = 12;
                }
            }
            map.put("{{용역기간}}", "착수일로부터 " + months + "개월");
            map.put("{{계약개월수}}", String.valueOf(months));

            // 계획 및 일정 표 월별 음영 (44=회색, 144=흰색)
            // 우선순위: pjt_schedule (예정공정표 폼 체크박스) > 계약 시작/종료월 범위
            boolean[] monthMask = new boolean[13];
            boolean fromForm = false;
            try {
                if (pjtScheduleRepository != null && proj != null && proj.getProjId() != null) {
                    var schs = pjtScheduleRepository.findByProjIdOrderBySortOrderAsc(proj.getProjId());
                    if (!schs.isEmpty()) {
                        var s0 = schs.get(0);
                        Boolean[] mm2 = new Boolean[]{ s0.getM01(), s0.getM02(), s0.getM03(), s0.getM04(),
                                s0.getM05(), s0.getM06(), s0.getM07(), s0.getM08(),
                                s0.getM09(), s0.getM10(), s0.getM11(), s0.getM12() };
                        for (int i = 0; i < 12; i++) monthMask[i + 1] = Boolean.TRUE.equals(mm2[i]);
                        fromForm = true;
                    }
                }
            } catch (Exception e) { log.warn("PjtSchedule load fail: {}", e.getMessage()); }
            for (int i = 1; i <= 12; i++) {
                boolean on = fromForm ? monthMask[i] : (i >= startMonth && i <= endMonth);
                map.put("{{월" + i + "_bf}}", on ? "93" : "45");
            }
            System.err.println("[HWPX] month_bf fromForm="+fromForm+" startMonth="+startMonth+" endMonth="+endMonth+" mask="+java.util.Arrays.toString(monthMask));

            // P13 공정별 투입인력계획 (PjtManpowerPlan 첫 행 기준)
            String p13Step = projNm;
            String p13Period = "";
            if (proj != null && proj.getStartDt() != null && proj.getEndDt() != null) {
                p13Period = formatDateKorean(proj.getStartDt()) + " ~ " + formatDateKorean(proj.getEndDt());
            }
            int gSp=0, gHi=0, gMi=0, gLo=0, fHi=0, fMi=0, fLo=0;
            try {
                if (pjtManpowerPlanRepository != null && proj != null && proj.getProjId() != null) {
                    var mps = pjtManpowerPlanRepository.findByProjIdOrderBySortOrderAsc(proj.getProjId());
                    if (!mps.isEmpty()) {
                        var m0 = mps.get(0);
                        if (m0.getStepName() != null && !m0.getStepName().isEmpty()) p13Step = m0.getStepName();
                        if (m0.getStartDt() != null && m0.getEndDt() != null)
                            p13Period = formatDateKorean(m0.getStartDt()) + " ~ " + formatDateKorean(m0.getEndDt());
                        gSp = m0.getGradeSpecial() != null ? m0.getGradeSpecial() : 0;
                        gHi = m0.getGradeHigh()    != null ? m0.getGradeHigh()    : 0;
                        gMi = m0.getGradeMid()     != null ? m0.getGradeMid()     : 0;
                        gLo = m0.getGradeLow()     != null ? m0.getGradeLow()     : 0;
                        fHi = m0.getFuncHigh()     != null ? m0.getFuncHigh()     : 0;
                        fMi = m0.getFuncMid()      != null ? m0.getFuncMid()      : 0;
                        fLo = m0.getFuncLow()      != null ? m0.getFuncLow()      : 0;
                    }
                }
            } catch (Exception e) { log.warn("PjtManpowerPlan load fail: {}", e.getMessage()); }
            int p13Sum = gSp+gHi+gMi+gLo+fHi+fMi+fLo;
            java.util.function.IntFunction<String> fmt = v -> v == 0 ? "" : String.valueOf(v);
            map.put("{{P13_단계명}}", p13Step);
            map.put("{{P13_기간}}", p13Period);
            map.put("{{P13_특급}}", fmt.apply(gSp));
            map.put("{{P13_고급}}", fmt.apply(gHi));
            map.put("{{P13_중급}}", fmt.apply(gMi));
            map.put("{{P13_초급}}", fmt.apply(gLo));
            map.put("{{P13_기능고}}", fmt.apply(fHi));
            map.put("{{P13_기능중}}", fmt.apply(fMi));
            map.put("{{P13_기능초}}", fmt.apply(fLo));
            map.put("{{P13_계}}", fmt.apply(p13Sum));
            System.err.println("[HWPX] P13 step="+p13Step+" period="+p13Period+" 특="+gSp+" 고="+gHi+" 중="+gMi+" 초="+gLo+" 기능고="+fHi+" 기능중="+fMi+" 기능초="+fLo+" 계="+p13Sum);

            // 예정공정표: 시작~종료월은 노란색(52), 그 외는 흰색(51) - 12칸 모두 표시
            for (int i = 1; i <= 12; i++) {
                boolean inRange = (i >= startMonth && i <= endMonth);
                map.put("{{월" + i + "색}}", inRange ? "52" : "51");
            }

            // 제품명 (PjtTarget 모두 → 줄바꿈 마커로 결합, 후처리에서 다중 hp:p로 확장)
            String productName = sysNmEn.isEmpty() ? sysNm : sysNmEn;
            List<String> productList = new ArrayList<>();
            try {
                if (pjtTargetRepository != null && proj != null && proj.getProjId() != null) {
                    var targets = pjtTargetRepository.findByProjIdOrderBySortOrderAsc(proj.getProjId());
                    for (var t : targets) {
                        if (t.getProductName() != null && !t.getProductName().trim().isEmpty()) {
                            productList.add(t.getProductName().trim());
                        }
                    }
                    if (!productList.isEmpty()) productName = productList.get(0);
                }
            } catch (Exception e) { log.warn("PjtTarget load fail: {}", e.getMessage()); }
            if (productList.isEmpty()) productList.add(productName);
            // 마커: 후처리에서 줄바꿈으로 분리하여 다중 <hp:p>로 확장
            map.put("{{제품명}}", String.join("\u0001", productList));

            // 작성년월 (착수월 기준)
            if (proj != null && proj.getStartDt() != null) {
                map.put("{{작성년월}}", String.format("%d. %02d.", proj.getStartDt().getYear(), proj.getStartDt().getMonthValue()));
            } else {
                map.put("{{작성년월}}", String.format("%d. %02d.", LocalDate.now().getYear(), LocalDate.now().getMonthValue()));
            }

            // === 사업수행계획서 (P12) - sw_pjt 4 컬럼 ===
            // 용역목적
            String projPurpose = (proj != null && proj.getProjPurpose() != null && !proj.getProjPurpose().isEmpty())
                    ? proj.getProjPurpose()
                    : (processName + "의 최신 버전 유지와 원활한 서비스를 제공");
            map.put("{{용역목적}}", projPurpose);

            // 지원형태
            String supportType = (proj != null && proj.getSupportType() != null && !proj.getSupportType().isEmpty())
                    ? proj.getSupportType() : "비상주 지원";
            map.put("{{지원형태}}", "- " + supportType);

            // 용역범위 (줄바꿈으로 구분된 텍스트 → 1/2/3 분할)
            String[] scopeLines = new String[]{"GIS SW 최신 버전 유지", "GIS SW 원활한 서비스 제공", "긴급/장애처리"};
            if (proj != null && proj.getScopeText() != null && !proj.getScopeText().isEmpty()) {
                String[] parts = proj.getScopeText().split("\\r?\\n");
                for (int i = 0; i < scopeLines.length && i < parts.length; i++) {
                    if (parts[i] != null && !parts[i].trim().isEmpty()) {
                        scopeLines[i] = parts[i].trim();
                    }
                }
            }
            map.put("{{용역범위1}}", "- " + scopeLines[0]);
            map.put("{{용역범위2}}", "- " + scopeLines[1]);
            map.put("{{용역범위3}}", "- " + scopeLines[2]);

            // 점검방법 (sw_pjt.inspect_method 우선, 없으면 기본값)
            String inspectMethod = (proj != null && proj.getInspectMethod() != null && !proj.getInspectMethod().isEmpty())
                    ? proj.getInspectMethod() : "월 1회 유지보수 방문";
            map.put("{{점검방법}}", "- " + inspectMethod);

            // ── 착수일자 한글 (보안서약서용) ──
            if (proj != null && proj.getStartDt() != null) {
                map.put("{{착수일자_한글}}", formatDateKorean(proj.getStartDt()));
            } else {
                map.put("{{착수일자_한글}}", String.format("%d년 01월 01일", LocalDate.now().getYear()));
            }

            // ── 계약/착수/준공 년월일 분리 (P1 표) ──
            if (proj != null && proj.getContDt() != null) {
                map.put("{{계약년}}", proj.getContDt().getYear() + "년");
                map.put("{{계약월}}", String.format("%02d월", proj.getContDt().getMonthValue()));
                map.put("{{계약일}}", String.format("%02d일", proj.getContDt().getDayOfMonth()));
            } else {
                map.put("{{계약년}}", ""); map.put("{{계약월}}", ""); map.put("{{계약일}}", "");
            }
            if (proj != null && proj.getStartDt() != null) {
                map.put("{{착수년}}", proj.getStartDt().getYear() + "년");
                map.put("{{착수월}}", String.format("%02d월", proj.getStartDt().getMonthValue()));
                map.put("{{착수일}}", String.format("%02d일", proj.getStartDt().getDayOfMonth()));
            } else {
                map.put("{{착수년}}", ""); map.put("{{착수월}}", ""); map.put("{{착수일}}", "");
            }
            if (proj != null && proj.getEndDt() != null) {
                map.put("{{준공년}}", proj.getEndDt().getYear() + "년");
                map.put("{{준공월}}", String.format("%02d월", proj.getEndDt().getMonthValue()));
                map.put("{{준공일}}", String.format("%02d일", proj.getEndDt().getDayOfMonth()));
            } else {
                map.put("{{준공년}}", ""); map.put("{{준공월}}", ""); map.put("{{준공일}}", "");
            }

            // ── 현장대리인 정보 (site_rep 섹션에서 가져옴) ──
            Map<String, Object> siteRepData = getSectionData(doc, "site_rep");
            map.put("{{현대_주소}}", getStr(siteRepData, "addr", ""));
            map.put("{{현대_성명}}", getStr(siteRepData, "name", ""));
            String siteRepSsn = getStr(siteRepData, "ssn", "");
            map.put("{{현대_주민번호}}", siteRepSsn);
            // 생년월일: SSN 앞6자리에서 추출
            if (siteRepSsn.length() >= 6) {
                String yy = siteRepSsn.substring(0, 2);
                String mm = siteRepSsn.substring(2, 4);
                String dd = siteRepSsn.substring(4, 6);
                String century = (siteRepSsn.length() > 7 && siteRepSsn.charAt(7) >= '3') ? "20" : "19";
                map.put("{{현대_생년}}", century + yy);
                map.put("{{현대_생월}}", String.valueOf(Integer.parseInt(mm)));
                map.put("{{현대_생일}}", String.valueOf(Integer.parseInt(dd)));
            } else {
                map.put("{{현대_생년}}", "");
                map.put("{{현대_생월}}", "");
                map.put("{{현대_생일}}", "");
            }

            // ── 투입인력 정보 (plan_personnel 섹션) → {{인력N_*}} ──
            Map<String, Object> ppData = getSectionData(doc, "plan_personnel");
            System.err.println("[HWPX] plan_personnel raw = " + ppData);
            List<Map<String, Object>> personnel = new ArrayList<>();
            if (ppData.containsKey("list") && ppData.get("list") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> rl = (List<Object>) ppData.get("list");
                for (Object o : rl) if (o instanceof Map) personnel.add((Map<String, Object>) o);
            }
            for (int i = 0; i < 3; i++) {
                String k = String.valueOf(i + 1);
                String name = "", pos = "", grade = "", career = "", dept = "", tasks = "", mobile = "";
                if (i < personnel.size()) {
                    Map<String, Object> pp = personnel.get(i);
                    name   = getStr(pp, "name", "");
                    // "이사 박욱진" 형태면 마지막 토큰만
                    if (name.contains(" ")) {
                        String[] sp = name.trim().split("\\s+");
                        name = sp[sp.length - 1];
                    }
                    pos    = getStr(pp, "position", "");
                    grade  = getStr(pp, "techGrade", "");
                    career = getStr(pp, "career", "");
                    dept   = getStr(pp, "dept", "");
                    tasks  = getStr(pp, "tasks", "").replaceAll("\\s*,\\s*", "\u0002");
                    mobile = getStr(pp, "mobile", "");
                }
                map.put("{{인력" + k + "_성명}}", name);
                map.put("{{인력" + k + "_직급}}", pos);
                map.put("{{인력" + k + "_등급}}", grade);
                map.put("{{인력" + k + "_경력}}", career);
                map.put("{{인력" + k + "_부서}}", dept);
                map.put("{{인력" + k + "_업무}}", tasks);
                map.put("{{인력" + k + "_연락처}}", mobile);
                System.err.println("[HWPX] 인력"+k+" name="+name+" career="+career+" mobile="+mobile+" tasks="+tasks.replace("\u0002","|"));
            }

            // ── 과업참여자/보안각서/보안서약서/투입인력 (participants 섹션에서 가져옴) ──
            Map<String, Object> partData = getSectionData(doc, "participants");
            List<Map<String, Object>> participants = new ArrayList<>();
            if (partData.containsKey("list") && partData.get("list") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> rawList = (List<Object>) partData.get("list");
                for (Object item : rawList) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> m = (Map<String, Object>) item;
                        participants.add(m);
                    }
                }
            }

            // 최대 3명까지 (템플릿 행 수 제한)
            for (int i = 0; i < 3; i++) {
                String idx = String.valueOf(i + 1);
                if (i < participants.size()) {
                    Map<String, Object> p = participants.get(i);
                    String pName = getStr(p, "name", "");
                    // "이사 박욱진" → "박욱진" (직급 제거)
                    if (pName.contains(" ")) {
                        pName = pName.substring(pName.lastIndexOf(" ") + 1);
                    }
                    String pPosition = getStr(p, "position", "");
                    String pSsn = getStr(p, "ssn", "");
                    String pCert = getStr(p, "cert", "");
                    String pTechGrade = getStr(p, "techGrade", "");
                    String pTasks = getStr(p, "tasks", "");
                    // DB User 조회 보강 (fieldRole, certificate)
                    String pFieldRole = "";
                    if (userRepository != null && !pName.isEmpty()) {
                        var uOpt = userRepository.findFirstByUsername(pName);
                        if (uOpt.isPresent()) {
                            var u = uOpt.get();
                            if (u.getFieldRole() != null) pFieldRole = u.getFieldRole();
                            if (pCert.isEmpty() && u.getCertificate() != null) pCert = u.getCertificate();
                        }
                    }

                    // 보안각서
                    map.put("{{보안" + idx + "_성명}}", pName);
                    map.put("{{보안" + idx + "_등급}}", pTechGrade);
                    map.put("{{보안" + idx + "_업무}}", pTasks);
                    map.put("{{보안" + idx + "_주민번호}}", pSsn);

                    // 과업참여자 명단 (P5)
                    map.put("{{참여" + idx + "_성명}}", pName);
                    map.put("{{참여" + idx + "_주민번호}}", pSsn);
                    map.put("{{참여" + idx + "_직위}}", pPosition);
                    map.put("{{참여" + idx + "_자격}}", pCert.isEmpty() ? "학력기술자" : pCert);

                    // 보안서약서
                    map.put("{{서약" + idx + "_직위}}", pPosition);
                    map.put("{{서약" + idx + "_성명}}", pName);
                    // 서약서 라벨은 "책임기술자"/"참여기술자" (유지보수 접두사 제거)
                    String pSakRole = !pFieldRole.isEmpty() ? pFieldRole.replace("유지보수","")
                            : (i == 0 ? "책임기술자" : "참여기술자");
                    map.put("{{서약" + idx + "_분야}}", pSakRole);

                    // 투입인력 정보 ({{인력N_*}})는 plan_personnel 섹션이 담당하므로 여기서 덮어쓰지 않음
                    // 참여 분야 (DB users.field_role, 없으면 첫번째=책임 나머지=참여)
                    map.put("{{참여" + idx + "_분야}}", !pFieldRole.isEmpty() ? pFieldRole
                            : (i == 0 ? "유지보수책임기술자" : "유지보수참여기술자"));
                } else {
                    // 빈 행
                    map.put("{{보안" + idx + "_성명}}", "");
                    map.put("{{보안" + idx + "_등급}}", "");
                    map.put("{{보안" + idx + "_업무}}", "");
                    map.put("{{보안" + idx + "_주민번호}}", "");
                    map.put("{{참여" + idx + "_성명}}", "");
                    map.put("{{참여" + idx + "_주민번호}}", "");
                    map.put("{{참여" + idx + "_직위}}", "");
                    map.put("{{참여" + idx + "_자격}}", "");
                    map.put("{{서약" + idx + "_직위}}", "");
                    map.put("{{서약" + idx + "_성명}}", "");
                    map.put("{{서약" + idx + "_분야}}", "");
                    map.put("{{참여" + idx + "_분야}}", "");
                }
            }
        }

        if ("completion_body_upis".equals(templateType)) {
            map.put("{{용역명}}", projNm);
            map.put("{{수신}}", buildRecipientHead(cityNm, distNm));

            Long contAmt = (proj != null && proj.getContAmt() != null) ? proj.getContAmt() : 0L;
            map.put("{{계약금액}}", "금" + String.format("%,d", contAmt) + "원(금" + convertToKoreanAmount(contAmt) + "원)");

            // 계약일자
            if (proj != null && proj.getContDt() != null) {
                map.put("{{계약일자}}", formatDateKorean(proj.getContDt()));
            } else {
                map.put("{{계약일자}}", "");
            }

            // 계약기간
            if (proj != null && proj.getStartDt() != null && proj.getEndDt() != null) {
                map.put("{{계약기간}}", formatDateKorean(proj.getStartDt()) + " ~ " + formatDateKorean(proj.getEndDt()));
            } else {
                map.put("{{계약기간}}", "");
            }

            // 준공예정일
            if (proj != null && proj.getEndDt() != null) {
                map.put("{{준공예정일}}", formatDateKorean(proj.getEndDt()));
            } else {
                map.put("{{준공예정일}}", "");
            }

            // 준공일
            Map<String, Object> compUpisData = getSectionData(doc, "completion");
            String compUpisDateStr = getStr(compUpisData, "completionDate", "");
            if (!compUpisDateStr.isEmpty()) {
                try {
                    LocalDate compDate = LocalDate.parse(compUpisDateStr);
                    map.put("{{준공일}}", formatDateKorean(compDate));
                } catch (Exception e) {
                    map.put("{{준공일}}", compUpisDateStr);
                }
            } else {
                map.put("{{준공일}}", String.format("%d년 12월   일", LocalDate.now().getYear()));
            }
        }

        if ("completion_body".equals(templateType)) {
            // 용역명 분리 (HWPX XML에서 여러 태그에 걸쳐 배치)
            map.put("{{용역명_앞}}", projNm);
            map.put("{{용역명_뒤}}", "");
            map.put("{{용역명}}", projNm);
            map.put("{{수신}}", buildRecipientHead(cityNm, distNm));

            Long contAmt = (proj != null && proj.getContAmt() != null) ? proj.getContAmt() : 0L;
            map.put("{{계약금액}}", "금" + String.format("%,d", contAmt) + "원");
            map.put("{{계약금액한글}}", "(" + convertToKoreanAmount(contAmt) + "원)");

            // 계약일자
            if (proj != null && proj.getContDt() != null) {
                map.put("{{계약일자}}", formatDateKorean(proj.getContDt()));
            } else {
                map.put("{{계약일자}}", "");
            }

            // 준공예정일 (endDt)
            if (proj != null && proj.getEndDt() != null) {
                map.put("{{준공예정일}}", formatDateKorean(proj.getEndDt()));
                map.put("{{준공예정일2}}", formatDateKorean(proj.getEndDt()));
            } else {
                map.put("{{준공예정일}}", "");
                map.put("{{준공예정일2}}", "");
            }

            // 준공일 - completion 섹션에서 가져오거나 현재 날짜
            Map<String, Object> compData = getSectionData(doc, "completion");
            String compDateStr = getStr(compData, "completionDate", "");
            if (!compDateStr.isEmpty()) {
                try {
                    LocalDate compDate = LocalDate.parse(compDateStr);
                    map.put("{{준공일}}", formatDateKorean(compDate));
                } catch (Exception e) {
                    map.put("{{준공일}}", compDateStr);
                }
            } else {
                map.put("{{준공일}}", formatDateKorean(LocalDate.now()));
            }

            // 착수일
            if (proj != null && proj.getStartDt() != null) {
                map.put("{{착수일}}", formatDateKorean(proj.getStartDt()));
            } else {
                map.put("{{착수일}}", "");
            }

            // 점검년도
            int inspYear = (proj != null && proj.getYear() != null) ? proj.getYear() : LocalDate.now().getYear();
            map.put("{{점검년도}}", String.valueOf(inspYear));
        }

        if ("completion_full".equals(templateType)) {
            // 통합 준공계 템플릿 (v1) - 페이지 1·2 placeholder
            map.put("{{용역명}}", projNm);
            map.put("{{수신처}}", buildRecipientHead(cityNm, distNm));

            Long contAmt = (proj != null && proj.getContAmt() != null) ? proj.getContAmt() : 0L;
            map.put("{{계약금액}}", String.format("%,d", contAmt));
            map.put("{{계약금액한글}}", convertToKoreanAmount(contAmt));

            // 계약년월일
            if (proj != null && proj.getContDt() != null) {
                map.put("{{계약년월일}}", formatDateKoreanPadded(proj.getContDt()));
            } else {
                map.put("{{계약년월일}}", "");
            }

            // 준공예정일 / 실제준공일 / 제출일 (page1·2 동일)
            String scheduledDate = (proj != null && proj.getEndDt() != null) ? formatDateKoreanPadded(proj.getEndDt()) : "";
            Map<String, Object> compFullData = getSectionData(doc, "completion");
            String actualDateStr = getStr(compFullData, "actualDate", "");
            String submitDateStr = getStr(compFullData, "submitDate", "");
            String actualDateK;
            try {
                actualDateK = !actualDateStr.isEmpty() ? formatDateKoreanPadded(LocalDate.parse(actualDateStr)) : scheduledDate;
            } catch (Exception e) { actualDateK = scheduledDate; }
            String submitDateK;
            try {
                submitDateK = !submitDateStr.isEmpty() ? formatDateKoreanPadded(LocalDate.parse(submitDateStr)) : actualDateK;
            } catch (Exception e) { submitDateK = actualDateK; }

            map.put("{{준공예정일}}", scheduledDate);
            map.put("{{실제준공일}}", actualDateK);
            map.put("{{제출일}}", submitDateK);
            map.put("{{검사_준공예정일}}", scheduledDate);
            map.put("{{검사_실제준공일}}", actualDateK);
            map.put("{{검사_제출일}}", submitDateK);

            // 대상_착수기간 (page3) - project 기간 fallback
            String periodText;
            if (proj != null && proj.getStartDt() != null && proj.getEndDt() != null) {
                periodText = formatDateKorean(proj.getStartDt()) + " ~ " + formatDateKorean(proj.getEndDt());
            } else {
                periodText = "";
            }
            map.put("{{대상_착수기간}}", periodText);
        }

        return map;
    }

    /**
     * 행 마커 처리: <!--ROW_MAINT_START-->...END 와 <!--ROW_INSP_START-->...END 사이의
     * 템플릿 행 XML을 sections 데이터 기반으로 N개 복제
     */
    @SuppressWarnings("unchecked")
    private String applyRowMarkers(String xml, Document doc) {
        // === 유지보수 대상 ===
        String mStart = "<!--ROW_MAINT_START-->";
        String mEnd = "<!--ROW_MAINT_END-->";
        int ms = xml.indexOf(mStart);
        int me = xml.indexOf(mEnd);
        if (ms >= 0 && me > ms) {
            String tpl = xml.substring(ms + mStart.length(), me);
            Map<String, Object> targetSec = getSectionData(doc, "target");
            List<Map<String, Object>> items = (List<Map<String, Object>>) targetSec.get("targets");
            StringBuilder out = new StringBuilder();
            if (items != null) {
                for (Map<String, Object> it : items) {
                    String cat = getStr(it, "category", "");
                    if (cat == null || cat.isEmpty()) cat = "SW";
                    String name = getStr(it, "productName", "");
                    String spec = getStr(it, "specification", "");
                    String qty = getStr(it, "quantity", "");
                    String row = tpl
                        .replace("{{m.category}}", escapeXml(cat))
                        .replace("{{m.targetName}}", escapeXml(name))
                        .replace("{{m.spec}}", escapeXmlMultiline(spec))
                        .replace("{{m.qty}}", escapeXml(qty));
                    out.append(row);
                }
            }
            xml = xml.substring(0, ms) + out.toString() + xml.substring(me + mEnd.length());
        }

        // === 정기점검 ===
        String iStart = "<!--ROW_INSP_START-->";
        String iEnd = "<!--ROW_INSP_END-->";
        int is = xml.indexOf(iStart);
        int ie = xml.indexOf(iEnd);
        if (is >= 0 && ie > is) {
            String tpl = xml.substring(is + iStart.length(), ie);
            Map<String, Object> inspSec = getSectionData(doc, "inspect_summary");
            List<Map<String, Object>> insps = (List<Map<String, Object>>) inspSec.get("inspections");
            StringBuilder out = new StringBuilder();
            if (insps != null) {
                for (Map<String, Object> it : insps) {
                    String dt = getStr(it, "datetime", "");
                    String dateStr = "";
                    try {
                        if (dt.length() >= 10) {
                            LocalDate d = LocalDate.parse(dt.substring(0, 10));
                            dateStr = d.getYear() + "년 " + d.getMonthValue() + "월 " + d.getDayOfMonth() + "일";
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                    String target = getStr(it, "targetList", "");
                    String resultCode = getStr(it, "result", "");
                    String resultLabel;
                    if ("NORMAL".equals(resultCode)) resultLabel = "정상 (이상 없음)";
                    else if ("CHECK".equals(resultCode)) resultLabel = "점검";
                    else resultLabel = resultCode;
                    String row = tpl
                        .replace("{{i.date}}", escapeXml(dateStr))
                        .replace("{{i.target}}", escapeXmlMultiline(target))
                        .replace("{{i.result}}", escapeXml(resultLabel));
                    out.append(row);
                }
            }
            xml = xml.substring(0, is) + out.toString() + xml.substring(ie + iEnd.length());
        }
        // 멀티라인 sentinel → paragraph 분할
        xml = expandMultilineParagraphs(xml);
        // 마커가 들어 있던 두 표의 rowCnt / rowAddr 을 실제 <hp:tr> 수에 맞춰 재동기화
        xml = syncTableRowCounts(xml);
        return xml;
    }

    /**
     * <hp:tbl> 단위로 실제 <hp:tr> 수를 세어 rowCnt 속성을 갱신하고,
     * 각 행의 cellAddr rowAddr 값을 0..N-1 로 재할당한다. 행 마커 치환 후 호출.
     */
    private String syncTableRowCounts(String xml) {
        StringBuilder out = new StringBuilder();
        int cursor = 0;
        java.util.regex.Pattern tblOpen = java.util.regex.Pattern.compile("<hp:tbl\\b");
        java.util.regex.Matcher m = tblOpen.matcher(xml);
        while (m.find(cursor)) {
            int s = m.start();
            out.append(xml, cursor, s);
            // find matching </hp:tbl> with depth tracking
            int depth = 0, i = s, e = -1;
            while (i < xml.length()) {
                if (xml.startsWith("<hp:tbl", i) && (i + 7 < xml.length()) && (xml.charAt(i + 7) == ' ' || xml.charAt(i + 7) == '>')) {
                    depth++; i += 7;
                } else if (xml.startsWith("</hp:tbl>", i)) {
                    depth--;
                    if (depth == 0) { e = i + 9; break; }
                    i += 9;
                } else { i++; }
            }
            if (e < 0) { out.append(xml.substring(s)); cursor = xml.length(); break; }
            String tbl = xml.substring(s, e);

            // count top-level <hp:tr> within this table
            int rowCount = countTopLevelTags(tbl, "<hp:tr>", "</hp:tr>", "<hp:tbl", "</hp:tbl>");
            // update rowCnt attribute on opening tag
            int tagEnd = tbl.indexOf('>');
            String openTag = tbl.substring(0, tagEnd + 1);
            String rest = tbl.substring(tagEnd + 1);
            java.util.regex.Matcher rm = java.util.regex.Pattern.compile("rowCnt=\"(\\d+)\"").matcher(openTag);
            if (rm.find()) {
                openTag = openTag.substring(0, rm.start()) + "rowCnt=\"" + rowCount + "\"" + openTag.substring(rm.end());
            }
            // reassign rowAddr per top-level row
            rest = reassignRowAddrs(rest);
            out.append(openTag).append(rest);
            cursor = e;
        }
        out.append(xml.substring(cursor));
        return out.toString();
    }

    private int countTopLevelTags(String s, String open, String close, String nestOpen, String nestClose) {
        int count = 0, i = 0, depth = 0, nest = 0;
        // skip the opening <hp:tbl ...> itself: caller passes whole tbl, so track outer tbl depth
        while (i < s.length()) {
            if (s.startsWith(nestOpen, i) && (i + nestOpen.length() < s.length()) && (s.charAt(i + nestOpen.length()) == ' ' || s.charAt(i + nestOpen.length()) == '>')) {
                nest++; i += nestOpen.length();
            } else if (s.startsWith(nestClose, i)) {
                nest--; i += nestClose.length();
            } else if (nest == 1 && s.startsWith(open, i)) {
                // depth==0 means inside outermost tbl content
                if (depth == 0) count++;
                depth++; i += open.length();
            } else if (s.startsWith(close, i)) {
                depth--; i += close.length();
            } else { i++; }
        }
        return count;
    }

    private String reassignRowAddrs(String tblBody) {
        // Walk top-level <hp:tr>...</hp:tr> blocks; in each, replace rowAddr="N" with current index
        StringBuilder out = new StringBuilder();
        int i = 0, idx = 0, nest = 0;
        while (i < tblBody.length()) {
            if (tblBody.startsWith("<hp:tbl", i) && (i + 7 < tblBody.length()) && (tblBody.charAt(i + 7) == ' ' || tblBody.charAt(i + 7) == '>')) {
                // nested table: copy as-is until matching close
                int depth = 1, j = i + 7;
                while (j < tblBody.length()) {
                    if (tblBody.startsWith("<hp:tbl", j) && (j + 7 < tblBody.length()) && (tblBody.charAt(j + 7) == ' ' || tblBody.charAt(j + 7) == '>')) { depth++; j += 7; }
                    else if (tblBody.startsWith("</hp:tbl>", j)) { depth--; j += 9; if (depth == 0) break; }
                    else j++;
                }
                out.append(tblBody, i, j);
                i = j;
            } else if (nest == 0 && tblBody.startsWith("<hp:tr>", i)) {
                int depth = 1, j = i + 7;
                while (j < tblBody.length()) {
                    if (tblBody.startsWith("<hp:tr>", j)) { depth++; j += 7; }
                    else if (tblBody.startsWith("</hp:tr>", j)) { depth--; j += 8; if (depth == 0) break; }
                    else j++;
                }
                String row = tblBody.substring(i, j);
                final int rowIdx = idx;
                row = row.replaceAll("rowAddr=\"\\d+\"", "rowAddr=\"" + rowIdx + "\"");
                out.append(row);
                idx++;
                i = j;
            } else {
                out.append(tblBody.charAt(i));
                i++;
            }
        }
        return out.toString();
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** XML 이스케이프 + 줄바꿈을 sentinel 토큰으로 변환. expandMultilineParagraphs() 가 후처리. */
    private static final String NL_SENTINEL = "\u0001NL\u0001";
    private String escapeXmlMultiline(String s) {
        if (s == null) return "";
        String esc = escapeXml(s.replace("\r\n", "\n").replace("\r", "\n"));
        return esc.replace("\n", NL_SENTINEL);
    }

    /**
     * sentinel 이 들어 있는 <hp:p>...</hp:p> 블록을 찾아 줄 단위로 paragraph 를 복제한다.
     * 각 줄은 원본 paragraph 의 속성/스타일을 그대로 유지한다.
     */
    private String expandMultilineParagraphs(String xml) {
        while (true) {
            int idx = xml.indexOf(NL_SENTINEL);
            if (idx < 0) break;
            int ps = xml.lastIndexOf("<hp:p ", idx);
            if (ps < 0) { xml = xml.replace(NL_SENTINEL, " "); break; }
            int peEnd = xml.indexOf("</hp:p>", idx);
            if (peEnd < 0) { xml = xml.replace(NL_SENTINEL, " "); break; }
            peEnd += "</hp:p>".length();
            String para = xml.substring(ps, peEnd);
            // text 노드 추출
            int ts = para.indexOf("<hp:t>");
            int te = para.indexOf("</hp:t>", ts);
            if (ts < 0 || te < 0) { xml = xml.replace(NL_SENTINEL, " "); break; }
            String fullText = para.substring(ts + "<hp:t>".length(), te);
            String[] lines = fullText.split(java.util.regex.Pattern.quote(NL_SENTINEL), -1);
            StringBuilder rebuilt = new StringBuilder();
            for (String line : lines) {
                String clone = para.substring(0, ts + "<hp:t>".length()) + line + para.substring(te);
                rebuilt.append(clone);
            }
            xml = xml.substring(0, ps) + rebuilt.toString() + xml.substring(peEnd);
        }
        return xml;
    }

    /**
     * Document의 details에서 sectionKey에 해당하는 sectionData 조회
     */
    private Map<String, Object> getSectionData(Document doc, String sectionKey) {
        if (doc.getDetails() != null) {
            for (DocumentDetail detail : doc.getDetails()) {
                if (sectionKey.equals(detail.getSectionKey())) {
                    return detail.getSectionData();
                }
            }
        }
        return new HashMap<>();
    }

    /**
     * Map에서 문자열 값 안전하게 조회
     */
    private String getStr(Map<String, Object> map, String key, String defaultVal) {
        if (map == null) return defaultVal;
        Object v = map.get(key);
        return v != null ? v.toString() : defaultVal;
    }

    /**
     * 특정 placeholder 토큰을 포함하는 <hp:tc>...</hp:tc> 블록을 모두 제거하고
     * 부모 <hp:tbl>의 colCnt를 감소시킨다. (예정공정표 비활성 월 셀 제거용)
     */
    private String removeTcContaining(String xml, String token) {
        while (true) {
            int p = xml.indexOf(token);
            if (p < 0) break;
            int tcStart = xml.lastIndexOf("<hp:tc ", p);
            if (tcStart < 0) tcStart = xml.lastIndexOf("<hp:tc>", p);
            if (tcStart < 0) break;
            int tcEnd = xml.indexOf("</hp:tc>", p);
            if (tcEnd < 0) break;
            tcEnd += "</hp:tc>".length();
            int tblStart = xml.lastIndexOf("<hp:tbl ", tcStart);
            if (tblStart >= 0) {
                int tagEnd = xml.indexOf(">", tblStart);
                String tag = xml.substring(tblStart, tagEnd);
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("colCnt=\"(\\d+)\"").matcher(tag);
                if (m.find()) {
                    int cnt = Integer.parseInt(m.group(1));
                    String newTag = tag.substring(0, m.start()) + "colCnt=\"" + (cnt - 1) + "\"" + tag.substring(m.end());
                    xml = xml.substring(0, tblStart) + newTag + xml.substring(tagEnd);
                    int delta = newTag.length() - tag.length();
                    tcStart += delta;
                    tcEnd += delta;
                }
            }
            xml = xml.substring(0, tcStart) + xml.substring(tcEnd);
        }
        return xml;
    }

    /**
     * 핵심 템플릿 처리: ZIP 읽기 -> section0.xml 내 플레이스홀더 치환 -> 새 ZIP 쓰기
     */
    private byte[] processTemplate(String templatePath, Map<String, String> replacements) {
        return processTemplate(templatePath, replacements, null);
    }

    private byte[] processTemplate(String templatePath, Map<String, String> replacements, Document doc) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            if (!resource.exists()) {
                throw new RuntimeException(messages.get("error.template.file_not_found", templatePath));
            }

            byte[] templateBytes;
            try (InputStream is = resource.getInputStream()) {
                templateBytes = is.readAllBytes();
            }

            SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel();

            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(templateBytes));
                 ZipArchiveOutputStream zos = new ZipArchiveOutputStream(channel)) {

                // 한컴/EPUB 호환: 파일명 UTF-8 플래그(bit 11) 비활성화. mimetype은 STORED + flag=0 필수
                zos.setUseLanguageEncodingFlag(false);
                zos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NEVER);
                zos.setEncoding("UTF-8");

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    byte[] data = zis.readAllBytes();

                    // Contents/section0.xml 내 플레이스홀더 치환
                    if ("Contents/section0.xml".equals(entry.getName())) {
                        String xml = new String(data, StandardCharsets.UTF_8);
                        for (Map.Entry<String, String> r : replacements.entrySet()) {
                            xml = xml.replace(r.getKey(), r.getValue());
                        }
                        // 행 마커 처리: <!--ROW_MAINT_START-->...END / ROW_INSP_START...END
                        if (doc != null) {
                            xml = applyRowMarkers(xml, doc);
                        }
                        // 다중행 마커(\u0001): 마커가 들어있는 <hp:tbl> 전체를 찾아 해당 <hp:tr> 행을 복제하고 rowCnt/rowAddr 갱신
                        java.util.regex.Pattern tblP = java.util.regex.Pattern.compile(
                            "(<hp:tbl\\b[^>]*?rowCnt=\")(\\d+)(\"[^>]*>)((?:(?!</hp:tbl>).)*?)(</hp:tbl>)",
                            java.util.regex.Pattern.DOTALL);
                        java.util.regex.Matcher tm = tblP.matcher(xml);
                        StringBuffer tsb = new StringBuffer();
                        while (tm.find()) {
                            String tblHead = tm.group(1);
                            int rowCnt = Integer.parseInt(tm.group(2));
                            String tblHeadEnd = tm.group(3);
                            String tblInner = tm.group(4);
                            String tblClose = tm.group(5);
                            if (!tblInner.contains("\u0001")) {
                                tm.appendReplacement(tsb, java.util.regex.Matcher.quoteReplacement(tm.group()));
                                continue;
                            }
                            // 마커 보유 행 복제
                            java.util.regex.Pattern trP = java.util.regex.Pattern.compile(
                                "(<hp:tr\\b[^>]*>)((?:(?!</hp:tr>).)*?)(</hp:tr>)", java.util.regex.Pattern.DOTALL);
                            java.util.regex.Matcher trm = trP.matcher(tblInner);
                            StringBuffer rsb = new StringBuffer();
                            int addedRows = 0;
                            int curRowAddr = 0;
                            while (trm.find()) {
                                String trOpen = trm.group(1);
                                String trInner = trm.group(2);
                                String trClose = trm.group(3);
                                // 현재 행의 rowAddr (첫 cellAddr 기준)
                                java.util.regex.Matcher ra = java.util.regex.Pattern.compile("rowAddr=\"(\\d+)\"").matcher(trInner);
                                int origRowAddr = ra.find() ? Integer.parseInt(ra.group(1)) : curRowAddr;
                                if (!trInner.contains("\u0001")) {
                                    // rowAddr 갱신
                                    String fixed = trInner.replaceAll("rowAddr=\"\\d+\"", "rowAddr=\"" + curRowAddr + "\"");
                                    trm.appendReplacement(rsb, java.util.regex.Matcher.quoteReplacement(trOpen + fixed + trClose));
                                    curRowAddr++;
                                } else {
                                    java.util.regex.Matcher tt = java.util.regex.Pattern
                                        .compile("<hp:t>([^<]*\u0001[^<]*)</hp:t>").matcher(trInner);
                                    if (!tt.find()) {
                                        trm.appendReplacement(rsb, java.util.regex.Matcher.quoteReplacement(trm.group()));
                                        continue;
                                    }
                                    String[] parts = tt.group(1).split("\u0001");
                                    StringBuilder out = new StringBuilder();
                                    for (String part : parts) {
                                        String ri = trInner.replace("<hp:t>" + tt.group(1) + "</hp:t>", "<hp:t>" + part + "</hp:t>");
                                        ri = ri.replaceAll("rowAddr=\"\\d+\"", "rowAddr=\"" + curRowAddr + "\"");
                                        out.append(trOpen).append(ri).append(trClose);
                                        curRowAddr++;
                                    }
                                    addedRows += parts.length - 1;
                                    trm.appendReplacement(rsb, java.util.regex.Matcher.quoteReplacement(out.toString()));
                                }
                            }
                            trm.appendTail(rsb);
                            String newInner = rsb.toString();
                            int newRowCnt = rowCnt + addedRows;
                            tm.appendReplacement(tsb, java.util.regex.Matcher.quoteReplacement(
                                tblHead + newRowCnt + tblHeadEnd + newInner + tblClose));
                        }
                        tm.appendTail(tsb);
                        xml = tsb.toString();
                        // 셀 내 줄바꿈 마커(\u0002): hp:p 안의 hp:t 가 \u0002 포함 시 → 여러 hp:p 로 분리
                        java.util.regex.Pattern hpP = java.util.regex.Pattern.compile(
                            "(<hp:p\\b[^>]*>)((?:(?!</hp:p>).)*?<hp:t>)([^<]*\u0002[^<]*)(</hp:t>(?:(?!</hp:p>).)*?</hp:p>)",
                            java.util.regex.Pattern.DOTALL);
                        java.util.regex.Matcher pm = hpP.matcher(xml);
                        StringBuffer psb = new StringBuffer();
                        while (pm.find()) {
                            String pOpen = pm.group(1);
                            String pre   = pm.group(2);
                            String text  = pm.group(3);
                            String post  = pm.group(4);
                            String[] parts = text.split("\u0002");
                            StringBuilder out = new StringBuilder();
                            for (String part : parts) {
                                out.append(pOpen).append(pre).append(part).append(post);
                            }
                            pm.appendReplacement(psb, java.util.regex.Matcher.quoteReplacement(out.toString()));
                        }
                        pm.appendTail(psb);
                        xml = psb.toString();
                        // 텍스트 길이가 변경되면 HWP가 손상으로 인식하므로 linesegarray 초기화
                        xml = xml.replaceAll("(?s)<hp:linesegarray>.*?</hp:linesegarray>",
                                "<hp:linesegarray><hp:lineseg textpos=\"0\" vertpos=\"0\" vertsize=\"1000\" textheight=\"1000\" baseline=\"850\" spacing=\"600\" horzpos=\"0\" horzsize=\"40000\" flags=\"393216\"/></hp:linesegarray>");
                        data = xml.getBytes(StandardCharsets.UTF_8);
                    }

                    // Preview/PrvText.txt 내 플레이스홀더 치환
                    if ("Preview/PrvText.txt".equals(entry.getName())) {
                        String txt = new String(data, StandardCharsets.UTF_8);
                        for (Map.Entry<String, String> r : replacements.entrySet()) {
                            txt = txt.replace(r.getKey(), r.getValue());
                        }
                        data = txt.getBytes(StandardCharsets.UTF_8);
                    }

                    ZipArchiveEntry newEntry = new ZipArchiveEntry(entry.getName());
                    java.util.zip.CRC32 crc = new java.util.zip.CRC32();
                    crc.update(data);
                    newEntry.setCrc(crc.getValue());
                    newEntry.setSize(data.length);
                    if ("mimetype".equals(entry.getName())) {
                        newEntry.setMethod(ZipArchiveEntry.STORED);
                        newEntry.setCompressedSize(data.length);
                    } else {
                        newEntry.setMethod(ZipArchiveEntry.DEFLATED);
                    }
                    zos.putArchiveEntry(newEntry);
                    zos.write(data);
                    zos.closeArchiveEntry();
                }
                zos.finish();
            }

            return java.util.Arrays.copyOf(channel.array(), (int) channel.size());
        } catch (Exception e) {
            log.error("HWPX 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException(messages.get("error.export.hwpx_generation", e.getMessage()), e);
        }
    }
}

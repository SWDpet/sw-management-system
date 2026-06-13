// [ops-fault-support M2] 운영문서 관계자(엔지니어/요청자) 공통 스크립트.
// doc-fault.html / doc-support.html 공용. 폼은 window.OPS_IS_EDIT / OPS_DOC_ID 를 설정.
var opsSelectedReq = { kind: 'PERSON', id: null, label: '' };
var opsReqTimer = null;

function opsVal(id) { var e = document.getElementById(id); return e ? e.value.trim() : ''; }
function opsEsc(s) { return String(s == null ? '' : s).replace(/\\/g, '\\\\').replace(/'/g, "\\'"); }
function opsEscHtml(s) {
    return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) {
        return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c];
    });
}
function opsHideResults() { var ul = document.getElementById('reqResults'); if (ul) { ul.classList.remove('show'); ul.innerHTML = ''; } }

// 엔지니어 드롭다운 (SW지원팀 활성). 반환: fetch promise
function opsLoadEngineers() {
    var sel = document.getElementById('engineerId');
    if (!sel) return Promise.resolve();
    return fetch('/ops-doc/api/engineers', { credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (list) {
            list.forEach(function (e) {
                var o = document.createElement('option');
                o.value = e.id;
                o.textContent = e.name + (e.position ? ' (' + e.position + ')' : '');
                sel.appendChild(o);
            });
        });
}

function setReqKind(kind) {
    opsSelectedReq.kind = kind;
    var p = document.getElementById('segPerson'), c = document.getElementById('segContact'), st = document.getElementById('segStaff');
    if (p) p.classList.toggle('active', kind === 'PERSON');
    if (st) st.classList.toggle('active', kind === 'STAFF');
    if (c) c.classList.toggle('active', kind === 'CONTACT');
    opsClearRequester();
}

function reqSearchInput() {
    var kw = opsVal('reqSearch');
    if (opsReqTimer) clearTimeout(opsReqTimer);
    if (kw.length < 1) { opsHideResults(); return; }
    opsReqTimer = setTimeout(function () {
        var url = (opsSelectedReq.kind === 'CONTACT') ? '/ops-doc/api/partner-contact/search?kw='   // 업체담당자
            : (opsSelectedReq.kind === 'STAFF') ? '/ops-doc/api/staff/search?kw='                   // 직원(tb_staff)
            : '/ops-doc/api/requester/search?kw=';                                                   // 공무원(ps_info)
        fetch(url + encodeURIComponent(kw), { credentials: 'same-origin' })
            .then(function (r) { return r.json(); })
            .then(opsRenderReqResults);
    }, 250);
}

function opsRenderReqResults(list) {
    var ul = document.getElementById('reqResults');
    if (!ul) return;
    if (!list || list.length === 0) {
        ul.innerHTML = '<li style="color:var(--ops-text-secondary)">결과 없음</li>';
        ul.classList.add('show'); return;
    }
    ul.innerHTML = list.map(function (p) {
        var label = (p.name || '') + ' / ' + (p.org || '-') + (p.dept ? ' ' + p.dept : '') + (p.pos ? ' ' + p.pos : '');
        return '<li onclick="opsSelectRequester(' + p.id + ',\'' + opsEsc(label) + '\')">' + opsEscHtml(label) + '</li>';
    }).join('');
    ul.classList.add('show');
}

function opsSelectRequester(id, label) {
    opsSelectedReq.id = id; opsSelectedReq.label = label;
    opsHideResults();
    var s = document.getElementById('reqSearch'); if (s) s.value = '';
    document.getElementById('reqSelectedBox').innerHTML =
        '<span class="ops-req-selected">' + opsEscHtml(label) + '<span class="x" onclick="opsClearRequester()">&times;</span></span>';
}

function opsClearRequester() {
    opsSelectedReq.id = null; opsSelectedReq.label = '';
    var box = document.getElementById('reqSelectedBox'); if (box) box.innerHTML = '';
    opsHideResults();
}

function toggleReqForm() { document.getElementById('reqForm').classList.toggle('show'); }

function saveRequester() {
    var name = opsVal('rfName');
    if (!name) { alert('이름은 필수입니다.'); return; }
    var body = { name: name, org: opsVal('rfOrg'), dept: opsVal('rfDept'), pos: opsVal('rfPos'), tel: opsVal('rfTel') };
    fetch('/ops-doc/api/requester', {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, credentials: 'same-origin', body: JSON.stringify(body)
    }).then(function (r) { return r.json(); }).then(function (d) {
        if (d.success) {
            setReqKind('PERSON');
            opsSelectRequester(d.id, name + ' / ' + (body.org || '-'));
            document.getElementById('reqForm').classList.remove('show');
            ['rfName', 'rfOrg', 'rfDept', 'rfPos', 'rfTel'].forEach(function (i) { document.getElementById(i).value = ''; });
        } else alert((d.error && d.error.message) || '등록 실패');
    }).catch(function (e) { alert('오류: ' + e); });
}

function opsPrefillRelations(docId) {
    return fetch('/ops-doc/api/' + docId + '/relations', { credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (d) {
            if (d.engineer_id) { var sel = document.getElementById('engineerId'); if (sel) sel.value = d.engineer_id; }
            if (d.requester_kind) {
                setReqKind(d.requester_kind);
                opsSelectedReq.id = d.requester_id;
                opsSelectedReq.label = d.requester_label || '';
                document.getElementById('reqSelectedBox').innerHTML =
                    '<span class="ops-req-selected">' + opsEscHtml(d.requester_label || '') + '<span class="x" onclick="opsClearRequester()">&times;</span></span>';
            }
        });
}

// 저장 body 에 합칠 관계자 파트
function opsRelationsBody() {
    var ev = opsVal('engineerId');
    return { engineer_id: ev ? Number(ev) : null, requester_kind: opsSelectedReq.kind, requester_id: opsSelectedReq.id };
}

// 저장 전 클라이언트 검증
function opsValidateRelations() {
    if (!opsVal('engineerId')) { alert('담당 엔지니어를 선택하세요.'); return false; }
    if (!opsSelectedReq.id) { alert('요청자를 선택하세요.'); return false; }
    return true;
}

// [M3] KB 추천 실시간 바인딩 — 증상 입력 시 디바운스로 자동 갱신(시스템 필터 포함)
var opsKb = { docType: null, symptomElId: null, sysElId: null, applyFn: null, timer: null };
function opsKbSetup(docType, symptomElId, sysElId, applyFn) {
    opsKb = { docType: docType, symptomElId: symptomElId, sysElId: sysElId, applyFn: applyFn, timer: null };
    var el = document.getElementById(symptomElId);
    if (el) el.addEventListener('input', function () {
        if (opsKb.timer) clearTimeout(opsKb.timer);
        opsKb.timer = setTimeout(opsKbRun, 350);
    });
}
function opsKbQuery() {
    var s = document.getElementById(opsKb.symptomElId);
    var q = (s && s.value && s.value.trim()) ? s.value.trim() : '';
    if (!q) { var t = document.getElementById('title'); q = t ? t.value.trim() : ''; }
    return q;
}
function opsKbRun() {
    var sys = opsKb.sysElId ? (document.getElementById(opsKb.sysElId) || {}).value : '';
    opsKbRecommend(opsKb.docType, opsKbQuery(), sys, opsKb.applyFn);
}
// "추천 보기/접기" 토글
function opsKbToggle() {
    var box = document.getElementById('kbRecoBox');
    if (!box) return;
    if (box.classList.contains('collapsed') || !box.innerHTML.trim()) {
        box.classList.remove('collapsed');
        opsKbRun();
    } else {
        box.classList.add('collapsed');
    }
}

// [M3] KB 추천 fetch+render — docType, query(증상텍스트), sysType, applyFn(선택 카드 적용)
function opsKbRecommend(docType, query, sysType, applyFn) {
    var box = document.getElementById('kbRecoBox');
    if (!box) return;
    box.classList.remove('collapsed');
    box.innerHTML = '<div class="ops-reco-loading">추천 검색 중...</div>';
    var url = '/ops-doc/api/kb/recommend?docType=' + encodeURIComponent(docType)
        + '&sysType=' + encodeURIComponent(sysType || '')
        + '&symptom=' + encodeURIComponent(query || '');
    fetch(url, { credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (list) {
            if (!list || !list.length) { box.innerHTML = '<div class="ops-reco-loading">추천 결과 없음</div>'; return; }
            box.innerHTML = list.map(function (k, i) {
                return '<div class="ops-reco-card"><div class="ops-reco-head">'
                    + '<span class="group-chip">' + opsEscHtml(k.kb_code) + '</span>'
                    + '<span class="ops-reco-meta">사례 ' + (k.case_count || 0) + '건</span></div>'
                    + '<div class="ops-reco-title">' + opsEscHtml(k.symptom || '') + ' › ' + opsEscHtml(k.cause || '') + '</div>'
                    + (k.summary ? '<div class="ops-reco-sum">' + opsEscHtml(k.summary) + '</div>' : '')
                    + '<button type="button" class="ops-btn ops-btn-primary ops-reco-apply" data-i="' + i + '">적용</button></div>';
            }).join('');
            box.querySelectorAll('.ops-reco-apply').forEach(function (btn) {
                btn.addEventListener('click', function () {
                    var k = list[Number(btn.dataset.i)];
                    applyFn(k);
                    opsKbFeedback(k.kb_id, 'APPLIED');   // [P5] 채택 적재
                });
            });
        }).catch(function (e) { box.innerHTML = '<div class="ops-reco-loading">오류: ' + e + '</div>'; });
}

// [P5] 추천 채택 피드백 (fire-and-forget)
function opsKbFeedback(kbId, action) {
    if (window.OPS_KB_BROWSE) return;   // 조회 화면(지식베이스)의 '적용'=상세보기 — 채택 오기록 방지
    if (!kbId) return;
    fetch('/ops-doc/api/kb/feedback', {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, credentials: 'same-origin',
        body: JSON.stringify({ kb_id: kbId, doc_id: window.OPS_DOC_ID || null, fb_action: action })
    }).catch(function () {});
}

// [FR-M2-4] 협력업체 다중선택
var opsPartners = [];           // [{partner_id, name, role_label}]
var opsPartnerTimer = null;

function opsPartnerSearchInput() {
    var kw = opsVal('partnerSearch');
    if (opsPartnerTimer) clearTimeout(opsPartnerTimer);
    if (kw.length < 1) { opsHidePartnerResults(); return; }
    opsPartnerTimer = setTimeout(function () {
        fetch('/ops-doc/api/partner/search?kw=' + encodeURIComponent(kw), { credentials: 'same-origin' })
            .then(function (r) { return r.json(); }).then(opsRenderPartnerResults);
    }, 250);
}
function opsRenderPartnerResults(list) {
    var ul = document.getElementById('partnerResults');
    if (!ul) return;
    if (!list || !list.length) {
        ul.innerHTML = '<li style="color:var(--ops-text-secondary)">결과 없음 (외부업체 관리에서 등록)</li>';
        ul.classList.add('show'); return;
    }
    ul.innerHTML = list.map(function (p) {
        return '<li onclick="opsAddPartner(' + p.id + ',\'' + opsEsc(p.name) + '\')">'
            + opsEscHtml(p.name) + (p.type ? ' (' + opsEscHtml(p.type) + ')' : '') + '</li>';
    }).join('');
    ul.classList.add('show');
}
function opsHidePartnerResults() { var ul = document.getElementById('partnerResults'); if (ul) { ul.classList.remove('show'); ul.innerHTML = ''; } }
function opsAddPartner(id, name, role) {
    opsHidePartnerResults();
    var s = document.getElementById('partnerSearch'); if (s) s.value = '';
    opsPartners.push({ partner_id: id, name: name, role_label: role || '' });
    opsRenderPartnerChips();
}
function opsRemovePartner(i) { opsPartners.splice(i, 1); opsRenderPartnerChips(); }
function opsRenderPartnerChips() {
    var box = document.getElementById('partnerChips');
    if (!box) return;
    box.innerHTML = opsPartners.map(function (p, i) {
        return '<div class="ops-partner-chip"><span class="pc-name">' + opsEscHtml(p.name) + '</span>'
            + '<input type="text" class="pc-role" placeholder="역할(예: 유지보수)" value="' + opsEscHtml(p.role_label || '') + '" oninput="opsPartners[' + i + '].role_label=this.value">'
            + '<span class="x" onclick="opsRemovePartner(' + i + ')">&times;</span></div>';
    }).join('');
}
function opsPartnersBody() {
    return opsPartners.map(function (p) { return { partner_id: p.partner_id, role_label: p.role_label || '' }; });
}
function opsPrefillPartners(docId) {
    fetch('/ops-doc/api/' + docId + '/partners', { credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (list) {
            opsPartners = (list || []).map(function (p) { return { partner_id: p.partner_id, name: p.name, role_label: p.role_label || '' }; });
            opsRenderPartnerChips();
        });
}

document.addEventListener('DOMContentLoaded', function () {
    if (!document.getElementById('engineerId')) return;
    opsLoadEngineers().then(function () {
        if (window.OPS_DOC_ID) opsPrefillRelations(window.OPS_DOC_ID);  // 편집·상세 공통 프리필
    });
    if (window.OPS_DOC_ID && document.getElementById('partnerChips')) opsPrefillPartners(window.OPS_DOC_ID);
});

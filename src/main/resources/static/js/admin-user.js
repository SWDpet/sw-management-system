/**
 * admin-user-list.html 전용 JS.
 *
 * 스프린트 6 (2026-04-19) — docs/plans/admin-user-hybrid-layout.md
 *
 * 기능:
 *  - 행 펼치기/접기 (여러 행 동시 가능)
 *  - 펼침 상태를 모든 form hidden input `expand` 에 동기화
 *  - 민감정보 "보기" 클릭 시 서버 API 호출 + JS 메모리 보관 (DOM/data-raw 금지)
 *  - 재마스킹 시 JS 메모리 해제 + input 초기 마스킹값 복원
 *  - 페이지 이탈 (beforeunload) 시 자동 재마스킹
 */
(function(global) {
    'use strict';

    // 현재 펼쳐진 사용자 ID 집합
    var expanded = new Set();

    // 드러난 민감정보 — key = userSeq + ':' + field, value = 평문. 페이지 메모리에만 존재.
    var revealedValues = new Map();

    // 페이지 로드 시 서버에서 내려준 expand 리스트를 파싱해 초기 Set 구성
    function initExpanded() {
        var rows = document.querySelectorAll('.user-detail');
        rows.forEach(function(row) {
            if (row.style.display && row.style.display !== 'none') {
                var id = row.getAttribute('data-user-seq');
                if (id) expanded.add(id);
            }
        });
        syncExpandInputs();
    }

    function toggleExpand(userSeq) {
        var id = String(userSeq);
        var row = document.getElementById('detail-' + id);
        if (!row) return;
        if (expanded.has(id)) {
            row.style.display = 'none';
            expanded.delete(id);
            // 접을 때 해당 사용자의 드러난 민감 필드 자동 재마스킹
            remaskAllForUser(id);
        } else {
            row.style.display = 'table-row';
            expanded.add(id);
        }
        syncExpandInputs();
        updateToggleButton(userSeq);
    }

    function updateToggleButton(userSeq) {
        var btn = document.querySelector('.btn-expand[data-user-seq="' + userSeq + '"]');
        if (!btn) return;
        var isExpanded = expanded.has(String(userSeq));
        if (isExpanded) {
            btn.classList.add('expanded');
            btn.textContent = '▴ 접기';
        } else {
            btn.classList.remove('expanded');
            btn.textContent = '▾ 펼치기';
        }
        // 접근성 (NFR-5) — aria-expanded 상태 동기화
        btn.setAttribute('aria-expanded', isExpanded ? 'true' : 'false');
    }

    function syncExpandInputs() {
        var csv = Array.from(expanded).join(',');
        document.querySelectorAll('input[data-expand-input="true"]').forEach(function(el) {
            el.value = csv;
        });
    }

    function revealSensitive(button) {
        var field = button.getAttribute('data-field');
        var userSeq = button.getAttribute('data-user-seq');
        if (!field || !userSeq) return;
        var key = userSeq + ':' + field;
        var input = document.querySelector('input[data-sensitive-input="true"][data-field="' + field + '"][data-user-seq="' + userSeq + '"]');
        if (!input) return;

        if (revealedValues.has(key)) {
            // 이미 드러남 → 재마스킹
            remaskField(userSeq, field);
            return;
        }

        fetch('/admin/users/api/' + encodeURIComponent(userSeq) + '/sensitive?field=' + encodeURIComponent(field), {
            credentials: 'same-origin'
        }).then(function(r) {
            if (!r.ok) throw new Error('HTTP ' + r.status);
            return r.json();
        }).then(function(data) {
            var value = data && data.value != null ? data.value : '';
            revealedValues.set(key, value);
            input.value = value;
            input.removeAttribute('readonly');
            input.classList.add('is-revealed');
            button.classList.add('revealed');
            button.textContent = '🔓 숨기기';
            ensureWarning(input);
        }).catch(function(e) {
            alert('민감정보 조회 실패: ' + (e && e.message ? e.message : '오류'));
        });
    }

    function remaskField(userSeq, field) {
        var key = userSeq + ':' + field;
        var input = document.querySelector('input[data-sensitive-input="true"][data-field="' + field + '"][data-user-seq="' + userSeq + '"]');
        if (!input) return;
        // 서버 렌더 시 마스킹된 초기값으로 복원 (data-mask-initial 속성)
        var initial = input.getAttribute('data-mask-initial');
        input.value = initial != null ? initial : '';
        input.setAttribute('readonly', 'readonly');
        input.classList.remove('is-revealed');
        revealedValues.delete(key);
        removeWarning(input);
        var btn = document.querySelector('.btn-reveal[data-user-seq="' + userSeq + '"][data-field="' + field + '"]');
        if (btn) {
            btn.classList.remove('revealed');
            btn.textContent = '🔒 보기';
        }
    }

    function remaskAllForUser(userSeq) {
        var keysToRemask = [];
        revealedValues.forEach(function(_, key) {
            if (key.indexOf(userSeq + ':') === 0) keysToRemask.push(key);
        });
        keysToRemask.forEach(function(key) {
            var field = key.substring(key.indexOf(':') + 1);
            remaskField(userSeq, field);
        });
    }

    function remaskAll() {
        var keys = Array.from(revealedValues.keys());
        keys.forEach(function(key) {
            var idx = key.indexOf(':');
            if (idx > 0) remaskField(key.substring(0, idx), key.substring(idx + 1));
        });
        revealedValues.clear();
    }

    function ensureWarning(input) {
        var dd = input.closest('dd');
        if (!dd) return;
        if (dd.querySelector('.reveal-warning')) return;
        var span = document.createElement('span');
        span.className = 'reveal-warning';
        span.textContent = '⚠ 평문 표시 중 — 편집 후 저장하세요';
        dd.appendChild(span);
    }

    function removeWarning(input) {
        var dd = input.closest('dd');
        if (!dd) return;
        var w = dd.querySelector('.reveal-warning');
        if (w) w.remove();
    }

    function cancelEdit(button) {
        var form = button.closest('form');
        if (!form) return;
        // 모든 input·select 를 defaultValue / defaultSelected 로 복원
        form.querySelectorAll('input, select, textarea').forEach(function(el) {
            if (el.type === 'hidden') return; // expand/page/userSeq 등 유지
            if (el.tagName === 'SELECT') {
                for (var i = 0; i < el.options.length; i++) {
                    el.options[i].selected = el.options[i].defaultSelected;
                }
            } else if (el.type === 'checkbox' || el.type === 'radio') {
                el.checked = el.defaultChecked;
            } else {
                el.value = el.defaultValue;
            }
        });
        // 민감정보는 drawn 상태일 수 있음 → 재마스킹
        var userSeq = form.getAttribute('data-user-seq');
        if (userSeq) remaskAllForUser(userSeq);
    }

    // 페이지 이탈 시 자동 재마스킹 (FR-3-E)
    window.addEventListener('beforeunload', function() {
        remaskAll();
    });

    document.addEventListener('DOMContentLoaded', function() {
        initExpanded();

        // 펼치기 버튼
        document.querySelectorAll('.btn-expand').forEach(function(btn) {
            var id = btn.getAttribute('data-user-seq');
            if (id) updateToggleButton(id);
            btn.addEventListener('click', function() { toggleExpand(id); });
        });

        // 민감정보 보기 버튼
        document.querySelectorAll('.btn-reveal').forEach(function(btn) {
            btn.addEventListener('click', function() { revealSensitive(btn); });
        });

        // 취소 버튼
        document.querySelectorAll('.btn-cancel-edit').forEach(function(btn) {
            btn.addEventListener('click', function() { cancelEdit(btn); });
        });
    });

    // 글로벌 노출 (onclick inline 호출 대비)
    global.AdminUser = {
        toggleExpand: toggleExpand,
        cancelEdit: cancelEdit
    };
})(window);

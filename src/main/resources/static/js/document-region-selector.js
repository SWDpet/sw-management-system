/**
 * 문서 대상(시도+시군구+시스템) 3단 드롭다운 공통 모듈.
 *
 * 4개 문서(장애처리/업무지원 외부/설치보고서/패치내역서) 전용.
 * 이 문서들은 사업·인프라와 독립된 성과·히스토리 관리용이므로
 * **sigungu_code 마스터 + sys_mst 마스터** 에서 선택지를 가져옴.
 *
 * 스프린트 5 v2 (2026-04-19) — docs/product-specs/doc-selector-org-env.md
 *
 * API:
 *   DocumentRegionSelector.init({
 *     sidoEl, sigunguEl, systemEl,
 *     onChange: function(payload) {
 *       // payload = { regionCode, sidoNm, sigunguNm, sysType, sysNm }
 *       // 4가지 모두 채워지면 호출. 하나라도 비면 null 들어옴
 *     },
 *     onReady: function() {}
 *   });
 *
 *   DocumentRegionSelector.setLegacyBanner(containerEl, text);
 */
(function(global) {
    'use strict';

    var API_SIDOS    = '/document/api/region-sidos';
    var API_SIGUNGUS = '/document/api/region-sigungus';
    var API_SYSTEMS  = '/document/api/systems-all';

    function q(sel) {
        if (!sel) return null;
        if (typeof sel === 'string') return document.querySelector(sel);
        return sel;
    }

    function setOptions(selectEl, options, placeholder, disabled) {
        if (!selectEl) return;
        selectEl.innerHTML = '';
        selectEl.disabled = !!disabled;
        if (placeholder !== undefined && placeholder !== null) {
            var ph = document.createElement('option');
            ph.value = '';
            ph.textContent = placeholder;
            selectEl.appendChild(ph);
        }
        (options || []).forEach(function(opt) {
            var o = document.createElement('option');
            if (typeof opt === 'string') {
                o.value = opt; o.textContent = opt;
            } else {
                o.value = opt.value;
                o.textContent = opt.label;
                if (opt.extra) {
                    Object.keys(opt.extra).forEach(function(k) {
                        o.setAttribute('data-' + k, opt.extra[k] == null ? '' : String(opt.extra[k]));
                    });
                }
            }
            selectEl.appendChild(o);
        });
    }

    function fetchJson(url) {
        return fetch(url, { credentials: 'same-origin' }).then(function(r) {
            if (!r.ok) throw new Error('HTTP ' + r.status);
            return r.json();
        });
    }

    function Selector(cfg) {
        this.cfg = cfg || {};
        this.sidoEl    = q(cfg.sidoEl);
        this.sigunguEl = q(cfg.sigunguEl);
        this.systemEl  = q(cfg.systemEl);
        this.onChange  = cfg.onChange || function() {};
    }

    Selector.prototype.start = function() {
        var self = this;
        // 시도 + 시스템 동시 로드 (서로 독립)
        setOptions(this.sidoEl, [], '로딩...');
        setOptions(this.sigunguEl, [], '-- 시도 먼저 선택 --', true);
        setOptions(this.systemEl, [], '로딩...');

        fetchJson(API_SIDOS).then(function(list) {
            setOptions(self.sidoEl, list, '시도 선택', false);
        });
        fetchJson(API_SYSTEMS).then(function(list) {
            var options = list.map(function(s) {
                return { value: s.cd, label: s.nm + ' (' + s.cd + ')', extra: { nm: s.nm } };
            });
            setOptions(self.systemEl, options, '시스템 선택', false);
            if (self.cfg.onReady) self.cfg.onReady();
        });

        this.sidoEl    && this.sidoEl.addEventListener('change',    function() { self._onSidoChange(); });
        this.sigunguEl && this.sigunguEl.addEventListener('change', function() { self._emit(); });
        this.systemEl  && this.systemEl.addEventListener('change',  function() { self._emit(); });
    };

    Selector.prototype._onSidoChange = function() {
        var sido = this.sidoEl.value;
        setOptions(this.sigunguEl, [], '-- 시도 먼저 선택 --', true);
        this._emit();
        if (!sido) return;
        var self = this;
        setOptions(this.sigunguEl, [], '불러오는 중...', true);
        fetchJson(API_SIGUNGUS + '?sidoNm=' + encodeURIComponent(sido)).then(function(list) {
            var options = list.map(function(s) {
                return { value: s.admSectC, label: s.sggNm, extra: { nm: s.sggNm } };
            });
            setOptions(self.sigunguEl, options, '시군구 선택', false);
        });
    };

    Selector.prototype._emit = function() {
        var sido = this.sidoEl ? this.sidoEl.value : '';
        var sigungu = this.sigunguEl ? this.sigunguEl.value : '';
        var sys = this.systemEl ? this.systemEl.value : '';
        if (!sido || !sigungu || !sys) {
            this.onChange(null);
            return;
        }
        var sigOpt = this.sigunguEl.options[this.sigunguEl.selectedIndex];
        var sysOpt = this.systemEl.options[this.systemEl.selectedIndex];
        this.onChange({
            regionCode: sigungu,  // adm_sect_c
            sidoNm: sido,
            sigunguNm: sigOpt ? sigOpt.getAttribute('data-nm') : sigungu,
            sysType: sys,         // sys_mst.cd
            sysNm: sysOpt ? sysOpt.getAttribute('data-nm') : sys
        });
    };

    function escapeHtml(s) {
        return String(s == null ? '' : s).replace(/[&<>"']/g, function(c) {
            return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[c];
        });
    }

    var api = {
        init: function(cfg) { var s = new Selector(cfg); s.start(); return s; },
        setLegacyBanner: function(containerEl, text) {
            var c = q(containerEl);
            if (!c) return;
            c.style.display = 'block';
            c.innerHTML = '<div style="padding:8px 12px; background:#fff3cd; border:1px solid #ffc107; border-radius:4px; margin-bottom:8px;">' +
                '⚠ 현재 대상: <b>' + escapeHtml(text || '(알 수 없음)') + '</b> — 저장하려면 아래에서 대상을 다시 선택하세요.' +
                '</div>';
        }
    };

    global.DocumentRegionSelector = api;
})(window);

/**
 * 문서 대상(지자체+시스템) 3단 연쇄 드롭다운 공통 모듈.
 *
 *   시도(cityNm) → 시군구(distNm) → 시스템명(sys_nm_en) → infra_id 자동 조회
 *
 * 스프린트 5 후속 (2026-04-19) — 사용자 피드백: 연도·사업 단계 불필요, 시스템은 유지.
 * 장애처리/업무지원(외부)/설치보고서/패치내역서 에서 사용.
 *
 * API:
 *   DocumentInfraSelector.init({
 *     cityEl, distEl, systemEl,
 *     onInfraChange: function(infraId, infraDto) { ... },
 *     onReady: function() { ... }
 *   });
 *
 *   DocumentInfraSelector.setLegacyBanner(containerEl, text);
 */
(function(global) {
    'use strict';

    var API_CITIES  = '/document/api/infra-cities';
    var API_DISTS   = '/document/api/infra-districts';
    var API_SYSTEMS = '/document/api/infra-systems';
    var API_FIND    = '/document/api/infra-find';

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
        (options || []).forEach(function(v) {
            var o = document.createElement('option');
            o.value = v; o.textContent = v;
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
        this.cityEl   = q(cfg.cityEl);
        this.distEl   = q(cfg.distEl);
        this.systemEl = q(cfg.systemEl);
        this.onInfraChange = cfg.onInfraChange || function() {};
    }

    Selector.prototype.start = function() {
        var self = this;
        setOptions(this.cityEl, [], '로딩...');
        setOptions(this.distEl, [], '-- 시도 먼저 선택 --', true);
        setOptions(this.systemEl, [], '-- 시군구 먼저 선택 --', true);

        fetchJson(API_CITIES).then(function(list) {
            setOptions(self.cityEl, list, '시도 선택', false);
            if (self.cfg.onReady) self.cfg.onReady();
        });

        this.cityEl   && this.cityEl.addEventListener('change', function() { self._onCityChange(); });
        this.distEl   && this.distEl.addEventListener('change', function() { self._onDistChange(); });
        this.systemEl && this.systemEl.addEventListener('change', function() { self._onSystemChange(); });
    };

    Selector.prototype._resetFrom = function(level) {
        if (level === 'city') {
            setOptions(this.distEl, [], '-- 시도 먼저 선택 --', true);
            setOptions(this.systemEl, [], '-- 시군구 먼저 선택 --', true);
        } else if (level === 'dist') {
            setOptions(this.systemEl, [], '-- 시군구 먼저 선택 --', true);
        }
        this.onInfraChange(null, null);
    };

    Selector.prototype._onCityChange = function() {
        var city = this.cityEl.value;
        this._resetFrom('city');
        if (!city) return;
        var self = this;
        setOptions(this.distEl, [], '불러오는 중...', true);
        fetchJson(API_DISTS + '?cityNm=' + encodeURIComponent(city)).then(function(list) {
            setOptions(self.distEl, list, '시군구 선택', false);
        });
    };

    Selector.prototype._onDistChange = function() {
        var city = this.cityEl.value, dist = this.distEl.value;
        this._resetFrom('dist');
        if (!dist) return;
        var self = this;
        setOptions(this.systemEl, [], '불러오는 중...', true);
        fetchJson(API_SYSTEMS + '?cityNm=' + encodeURIComponent(city) + '&distNm=' + encodeURIComponent(dist))
            .then(function(list) {
                if (!list || list.length === 0) {
                    setOptions(self.systemEl, [], '(등록된 시스템 없음)', true);
                } else {
                    setOptions(self.systemEl, list, '시스템명 선택', false);
                }
            });
    };

    Selector.prototype._onSystemChange = function() {
        var city = this.cityEl.value, dist = this.distEl.value, sys = this.systemEl.value;
        if (!sys) { this.onInfraChange(null, null); return; }
        var self = this;
        fetchJson(API_FIND +
            '?cityNm=' + encodeURIComponent(city) +
            '&distNm=' + encodeURIComponent(dist) +
            '&sysNmEn=' + encodeURIComponent(sys)
        ).then(function(body) {
            if (!body || !body.found) {
                alert('해당 조합의 인프라 정보를 찾지 못했습니다. 관리자에게 문의하세요.');
                self.onInfraChange(null, null);
                return;
            }
            self.onInfraChange(body.infraId, body);
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

    global.DocumentInfraSelector = api;
})(window);

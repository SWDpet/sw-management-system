/**
 * 문서 대상(사업) 연쇄 드롭다운 공통 모듈.
 *
 *   연도 → 시도 → 시군구 → 시스템명(sys_nm_en) → 사업(proj_id)
 *
 * 기존 doc-commence.html / doc-inspect.html 의 인라인 로직을 추출·공통화.
 * 스프린트 5 (2026-04-19) — docs/product-specs/doc-selector-org-env.md FR-1-C
 *
 * API (전역):
 *   DocumentProjectSelector.init({
 *     yearEl, cityEl, distEl, systemEl, projectEl,  // CSS selector 문자열
 *     onProjectChange: function(projId, projDto) {},
 *     initialYear: null,
 *     onReady: function() {},   // 연도 목록 로드 완료 시
 *   });
 *
 *   DocumentProjectSelector.setLegacyBanner(containerEl, text);
 *     레거시 레코드 편집 진입 시 배너 표시 (FR-1-F).
 */
(function(global) {
    'use strict';

    var API_YEARS    = '/document/api/project-years';
    var API_CITIES   = '/document/api/project-cities';
    var API_DISTS    = '/document/api/project-districts';
    var API_SYSTEMS  = '/document/api/project-systems';
    var API_PROJECTS = '/document/api/projects';

    function q(sel) {
        if (!sel) return null;
        if (typeof sel === 'string') return document.querySelector(sel);
        return sel;
    }

    function setOptions(selectEl, options, placeholder) {
        if (!selectEl) return;
        selectEl.innerHTML = '';
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
                o.value = opt.value; o.textContent = opt.label;
                if (opt.data) {
                    Object.keys(opt.data).forEach(function(k) {
                        o.setAttribute('data-' + k, opt.data[k] == null ? '' : String(opt.data[k]));
                    });
                }
            }
            selectEl.appendChild(o);
        });
    }

    function fetchJson(url) {
        return fetch(url, { credentials: 'same-origin' }).then(function(r) {
            if (!r.ok) throw new Error('HTTP ' + r.status + ' for ' + url);
            return r.json();
        });
    }

    function Selector(cfg) {
        this.cfg = cfg || {};
        this.yearEl    = q(cfg.yearEl);
        this.cityEl    = q(cfg.cityEl);
        this.distEl    = q(cfg.distEl);
        this.systemEl  = q(cfg.systemEl);
        this.projectEl = q(cfg.projectEl);
        this.onProjectChange = cfg.onProjectChange || function() {};
    }

    Selector.prototype.start = function() {
        var self = this;
        // 연도 로드
        setOptions(this.yearEl, [], '로딩...');
        fetchJson(API_YEARS).then(function(years) {
            setOptions(self.yearEl, (years || []).map(function(y) { return String(y); }), '연도');
            if (self.cfg.initialYear) self.yearEl.value = String(self.cfg.initialYear);
            self._resetBelow('year');
            if (self.cfg.onReady) self.cfg.onReady();
        }).catch(function(e) {
            setOptions(self.yearEl, [], '연도 로드 실패');
        });

        // 이벤트 바인딩
        this.yearEl   && this.yearEl.addEventListener('change', function() { self._onYearChange(); });
        this.cityEl   && this.cityEl.addEventListener('change', function() { self._onCityChange(); });
        this.distEl   && this.distEl.addEventListener('change', function() { self._onDistChange(); });
        this.systemEl && this.systemEl.addEventListener('change', function() { self._onSystemChange(); });
        this.projectEl && this.projectEl.addEventListener('change', function() { self._onProjectSelect(); });
    };

    Selector.prototype._resetBelow = function(level) {
        // level: year / city / dist / system
        if (level === 'year') {
            setOptions(this.cityEl,    [], '-- 연도 먼저 선택 --');
            setOptions(this.distEl,    [], '-- 시도 먼저 선택 --');
            setOptions(this.systemEl,  [], '-- 시군구 먼저 선택 --');
            setOptions(this.projectEl, [], '-- 위 조건을 선택하세요 --');
        } else if (level === 'city') {
            setOptions(this.distEl,    [], '-- 시도 먼저 선택 --');
            setOptions(this.systemEl,  [], '-- 시군구 먼저 선택 --');
            setOptions(this.projectEl, [], '-- 위 조건을 선택하세요 --');
        } else if (level === 'dist') {
            setOptions(this.systemEl,  [], '-- 시군구 먼저 선택 --');
            setOptions(this.projectEl, [], '-- 위 조건을 선택하세요 --');
        } else if (level === 'system') {
            setOptions(this.projectEl, [], '-- 위 조건을 선택하세요 --');
        }
    };

    Selector.prototype._onYearChange = function() {
        var year = this.yearEl.value;
        this._resetBelow('year');
        if (!year) return;
        var self = this;
        setOptions(this.cityEl, [], '불러오는 중...');
        fetchJson(API_CITIES + '?year=' + encodeURIComponent(year)).then(function(list) {
            setOptions(self.cityEl, list, '시도 선택');
        });
    };

    Selector.prototype._onCityChange = function() {
        var year = this.yearEl.value, city = this.cityEl.value;
        this._resetBelow('city');
        if (!city) return;
        var self = this;
        setOptions(this.distEl, [], '불러오는 중...');
        fetchJson(API_DISTS + '?year=' + encodeURIComponent(year) + '&cityNm=' + encodeURIComponent(city)).then(function(list) {
            setOptions(self.distEl, list, '시군구 선택');
        });
    };

    Selector.prototype._onDistChange = function() {
        var year = this.yearEl.value, city = this.cityEl.value, dist = this.distEl.value;
        this._resetBelow('dist');
        if (!dist) return;
        var self = this;
        setOptions(this.systemEl, [], '불러오는 중...');
        fetchJson(API_SYSTEMS +
            '?year=' + encodeURIComponent(year) +
            '&cityNm=' + encodeURIComponent(city) +
            '&distNm=' + encodeURIComponent(dist)
        ).then(function(list) {
            setOptions(self.systemEl, list, '시스템 선택');
        });
    };

    Selector.prototype._onSystemChange = function() {
        var year = this.yearEl.value, city = this.cityEl.value,
            dist = this.distEl.value, sys = this.systemEl.value;
        this._resetBelow('system');
        if (!sys) return;
        var self = this;
        setOptions(this.projectEl, [], '불러오는 중...');
        fetchJson(API_PROJECTS +
            '?year=' + encodeURIComponent(year) +
            '&cityNm=' + encodeURIComponent(city) +
            '&distNm=' + encodeURIComponent(dist) +
            '&sysNmEn=' + encodeURIComponent(sys)
        ).then(function(list) {
            if (!list || list.length === 0) {
                setOptions(self.projectEl, [], '해당 조건의 사업이 없습니다');
                return;
            }
            var options = list.map(function(p) {
                return {
                    value: p.projId,
                    label: (p.projNm || '(무제)') + (p.contStatNm ? ' [' + p.contStatNm + ']' : ''),
                    data: { proj: JSON.stringify(p) }
                };
            });
            setOptions(self.projectEl, options, '사업 선택');
        });
    };

    Selector.prototype._onProjectSelect = function() {
        var val = this.projectEl.value;
        if (!val) { this.onProjectChange(null, null); return; }
        var opt = this.projectEl.options[this.projectEl.selectedIndex];
        var projDto = null;
        var raw = opt && opt.getAttribute('data-proj');
        if (raw) { try { projDto = JSON.parse(raw); } catch (e) {} }
        this.onProjectChange(val, projDto);
    };

    var api = {
        init: function(cfg) {
            var s = new Selector(cfg);
            s.start();
            return s;
        },
        setLegacyBanner: function(containerEl, text) {
            var c = q(containerEl);
            if (!c) return;
            c.style.display = 'block';
            c.innerHTML = '<div style="padding:8px 12px; background:#fff3cd; border:1px solid #ffc107; border-radius:4px; margin-bottom:8px;">' +
                '⚠ 현재 대상: <b>' + escapeHtml(text || '(알 수 없음)') + '</b> — 저장하려면 아래에서 대상을 다시 선택하세요.' +
                '</div>';
        },
        clearLegacyBanner: function(containerEl) {
            var c = q(containerEl);
            if (c) { c.style.display = 'none'; c.innerHTML = ''; }
        }
    };

    function escapeHtml(s) {
        return String(s).replace(/[&<>"']/g, function(ch) {
            return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[ch];
        });
    }

    global.DocumentProjectSelector = api;
})(window);

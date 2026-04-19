/**
 * 조직도(본부/부서/팀) 연쇄 드롭다운 공통 모듈.
 * 가변 계층 지원 (1~3단).
 *
 * 스프린트 5 (2026-04-19) — docs/plans/doc-selector-org-env.md FR-2-C / FR-3-D
 *
 * 사용:
 *   var sel = OrgUnitSelector.init({
 *     divisionEl: '#selDivision',
 *     departmentEl: '#selDepartment',
 *     teamEl: '#selTeam',
 *     onUnitChange: function(finalUnitId, pathNames) {
 *       // finalUnitId: 말단 선택 유닛 id (팀 or 부 or 본부)
 *       // pathNames: ['본부명','부서명','팀명'] (선택된 단계까지만)
 *     },
 *     initialUnitId: null
 *   });
 */
(function(global) {
    'use strict';

    var API_ROOTS    = '/api/org-units/roots';
    var API_CHILDREN = '/api/org-units/children/'; // + parentId

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
        (options || []).forEach(function(u) {
            var o = document.createElement('option');
            o.value = u.unit_id;
            o.textContent = u.name;
            o.setAttribute('data-type', u.unit_type);
            o.setAttribute('data-name', u.name);
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
        this.divisionEl   = q(cfg.divisionEl);
        this.departmentEl = q(cfg.departmentEl);
        this.teamEl       = q(cfg.teamEl);
        this.onUnitChange = cfg.onUnitChange || function() {};
    }

    Selector.prototype.start = function() {
        var self = this;
        setOptions(this.divisionEl, [], '로딩...');
        setOptions(this.departmentEl, [], '-- 본부 먼저 선택 --', true);
        setOptions(this.teamEl, [], '-- 부서 먼저 선택 --', true);

        fetchJson(API_ROOTS).then(function(list) {
            setOptions(self.divisionEl, list, '본부/연구소 선택', false);
        });

        this.divisionEl   && this.divisionEl.addEventListener('change', function() { self._onDivisionChange(); });
        this.departmentEl && this.departmentEl.addEventListener('change', function() { self._onDepartmentChange(); });
        this.teamEl       && this.teamEl.addEventListener('change', function() { self._onTeamChange(); });
    };

    Selector.prototype._emit = function() {
        // 말단 선택 유닛 결정 (팀 > 부서 > 본부 순)
        var teamVal = this.teamEl ? this.teamEl.value : '';
        var deptVal = this.departmentEl ? this.departmentEl.value : '';
        var divVal  = this.divisionEl ? this.divisionEl.value : '';
        var finalId = null;
        var path = [];
        if (divVal) {
            var divOpt = this.divisionEl.options[this.divisionEl.selectedIndex];
            if (divOpt) path.push(divOpt.getAttribute('data-name'));
            finalId = divVal;
        }
        if (deptVal) {
            var deptOpt = this.departmentEl.options[this.departmentEl.selectedIndex];
            if (deptOpt) path.push(deptOpt.getAttribute('data-name'));
            finalId = deptVal;
        }
        if (teamVal) {
            var teamOpt = this.teamEl.options[this.teamEl.selectedIndex];
            if (teamOpt) path.push(teamOpt.getAttribute('data-name'));
            finalId = teamVal;
        }
        this.onUnitChange(finalId ? Number(finalId) : null, path);
    };

    Selector.prototype._onDivisionChange = function() {
        var self = this;
        var divId = this.divisionEl.value;
        setOptions(this.departmentEl, [], '-- 본부 먼저 선택 --', true);
        setOptions(this.teamEl, [], '-- 부서 먼저 선택 --', true);
        if (!divId) { this._emit(); return; }
        fetchJson(API_CHILDREN + encodeURIComponent(divId)).then(function(list) {
            if (!list || list.length === 0) {
                setOptions(self.departmentEl, [], '(하위 없음)', true);
                setOptions(self.teamEl, [], '(하위 없음)', true);
                self._emit();
                return;
            }
            // 자식에 DEPARTMENT 있으면 부서 select 로, 없고 TEAM 만 있으면 TEAM 을 부서 자리에 노출 (본부 직속 팀 케이스)
            var departments = list.filter(function(u) { return u.unit_type === 'DEPARTMENT'; });
            var directTeams = list.filter(function(u) { return u.unit_type === 'TEAM'; });
            if (departments.length > 0 && directTeams.length > 0) {
                // 혼재 케이스 — 모두 부서 select 에 넣음. TEAM 은 선택 시 부서 단계 건너뜀
                setOptions(self.departmentEl, list, '부서/팀 선택', false);
                setOptions(self.teamEl, [], '-- 부서 먼저 선택 --', true);
            } else if (departments.length > 0) {
                setOptions(self.departmentEl, departments, '부서 선택', false);
                setOptions(self.teamEl, [], '-- 부서 먼저 선택 --', true);
            } else {
                // TEAM 만 있음 (부 생략 구조)
                setOptions(self.departmentEl, directTeams, '팀 선택', false);
                setOptions(self.teamEl, [], '(하위 없음)', true);
            }
            self._emit();
        });
    };

    Selector.prototype._onDepartmentChange = function() {
        var self = this;
        var deptId = this.departmentEl.value;
        setOptions(this.teamEl, [], '-- 부서 먼저 선택 --', true);
        if (!deptId) { this._emit(); return; }
        // 선택된 항목이 TEAM 이면 (본부 직속 팀 or 부서 대신 팀) teamEl 비활성 유지
        var opt = this.departmentEl.options[this.departmentEl.selectedIndex];
        var selType = opt && opt.getAttribute('data-type');
        if (selType === 'TEAM') {
            setOptions(this.teamEl, [], '(선택 완료)', true);
            this._emit();
            return;
        }
        fetchJson(API_CHILDREN + encodeURIComponent(deptId)).then(function(list) {
            if (!list || list.length === 0) {
                setOptions(self.teamEl, [], '(팀 없음, 부서로 저장)', true);
            } else {
                setOptions(self.teamEl, list, '팀 선택 (선택사항)', false);
            }
            self._emit();
        });
    };

    Selector.prototype._onTeamChange = function() {
        this._emit();
    };

    Selector.prototype.reset = function() {
        setOptions(this.divisionEl, [], '본부/연구소 선택', false);
        setOptions(this.departmentEl, [], '-- 본부 먼저 선택 --', true);
        setOptions(this.teamEl, [], '-- 부서 먼저 선택 --', true);
        this.start();
    };

    var api = {
        init: function(cfg) {
            var s = new Selector(cfg);
            s.start();
            return s;
        }
    };

    global.OrgUnitSelector = api;
})(window);

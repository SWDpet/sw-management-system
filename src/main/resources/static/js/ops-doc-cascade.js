// [ops-doc-region-cascade] 시도→시군구→시스템 캐스케이드 (업무계획 패턴 재사용).
// 선택 결과를 hidden #region_code(시군구 admSectC) / #sys_type(시스템명)에 기록 → 기존 저장 로직 호환.
(function () {
    function init() {
        var sido = document.getElementById('casSido');
        if (!sido) return;
        var sgg = document.getElementById('casSgg');
        var sys = document.getElementById('casSys');
        var direct = document.getElementById('casSysDirect');
        var hRegion = document.getElementById('region_code');
        var hSys = document.getElementById('sys_type');

        function setRegion(v) { if (hRegion) hRegion.value = v || ''; }
        function setSys(v) { if (hSys) hSys.value = v || ''; }
        function reset(sel, ph) { sel.innerHTML = '<option value="">' + ph + '</option>'; sel.disabled = true; }

        function loadSgg(sidoVal) {
            reset(sgg, '시군구'); reset(sys, '시스템'); setRegion(''); setSys('');
            if (direct) { direct.style.display = 'none'; direct.value = ''; }
            if (!sidoVal) return Promise.resolve();
            return fetch('/ops-doc/api/sgg?sido=' + encodeURIComponent(sidoVal), { credentials: 'same-origin' })
                .then(function (r) { return r.json(); })
                .then(function (list) {
                    list.forEach(function (s) {
                        var o = document.createElement('option');
                        o.value = s.admSectC;
                        o.textContent = s.isUnit ? (s.sggNm + ' (본청/도청)') : s.sggNm;
                        sgg.appendChild(o);
                    });
                    sgg.disabled = false;
                });
        }

        function loadSys(admSectC) {
            reset(sys, '시스템'); setRegion(admSectC || ''); setSys('');
            if (direct) { direct.style.display = 'none'; direct.value = ''; }
            if (!admSectC) return Promise.resolve();
            return fetch('/ops-doc/api/systems?admSectC=' + encodeURIComponent(admSectC), { credentials: 'same-origin' })
                .then(function (r) { return r.json(); })
                .then(function (list) {
                    list.forEach(function (x) {
                        var o = document.createElement('option'); o.value = x.sysNm; o.textContent = x.sysNm; sys.appendChild(o);
                    });
                    var od = document.createElement('option'); od.value = '__direct__'; od.textContent = '직접입력'; sys.appendChild(od);
                    sys.disabled = false;
                });
        }

        sido.addEventListener('change', function () { loadSgg(sido.value); });
        sgg.addEventListener('change', function () { loadSys(sgg.value); });
        sys.addEventListener('change', function () {
            if (sys.value === '__direct__') { direct.style.display = 'block'; direct.value = ''; setSys(''); direct.focus(); }
            else { direct.style.display = 'none'; setSys(sys.value); }
        });
        if (direct) direct.addEventListener('input', function () { setSys(direct.value); });

        // 프리필 (수정/상세) — region_code → 시도/시군구 복원, sys_type 선택/직접입력
        var restore = document.getElementById('casRestore');
        var preRegion = restore ? restore.getAttribute('data-region') : null;
        var preSys = restore ? restore.getAttribute('data-sys') : null;
        if (preRegion) {
            fetch('/ops-doc/api/region?admSectC=' + encodeURIComponent(preRegion), { credentials: 'same-origin' })
                .then(function (r) { return r.json(); })
                .then(function (reg) {
                    if (!reg || !reg.sido) return;
                    sido.value = reg.sido;
                    return loadSgg(reg.sido).then(function () {
                        sgg.value = preRegion;
                        return loadSys(preRegion);
                    }).then(function () {
                        if (!preSys) return;
                        var found = Array.prototype.some.call(sys.options, function (o) { return o.value === preSys; });
                        if (found) { sys.value = preSys; setSys(preSys); }
                        else { sys.value = '__direct__'; direct.style.display = 'block'; direct.value = preSys; setSys(preSys); }
                        setRegion(preRegion);
                    });
                });
        }
    }
    document.addEventListener('DOMContentLoaded', init);
})();

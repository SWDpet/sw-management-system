// [ops-doc-region-cascade] 시도→시군구(지역) + 시스템(sys_mst 마스터, 계약 무관).
// 결과를 hidden #region_code(시군구 admSectC) / #sys_type(시스템 cd)에 기록 → 기존 저장 로직 호환.
(function () {
    function init() {
        var sido = document.getElementById('casSido');
        if (!sido) return;
        var sgg = document.getElementById('casSgg');
        var sys = document.getElementById('casSys');
        var hRegion = document.getElementById('region_code');
        var hSys = document.getElementById('sys_type');

        function setRegion(v) { if (hRegion) hRegion.value = v || ''; }
        function setSys(v) { if (hSys) hSys.value = v || ''; }
        function resetRegionSel() {
            sgg.innerHTML = '<option value="">시군구</option>'; sgg.disabled = true; setRegion('');
        }

        // 시스템: sys_mst 전체 로드(지역 무관)
        function loadSystems(preCd) {
            return fetch('/ops-doc/api/systems', { credentials: 'same-origin' })
                .then(function (r) { return r.json(); })
                .then(function (list) {
                    sys.innerHTML = '<option value="">시스템</option>';
                    list.forEach(function (x) {
                        var o = document.createElement('option');
                        o.value = x.cd; o.textContent = x.nm + (x.cd ? ' (' + x.cd + ')' : '');
                        sys.appendChild(o);
                    });
                    sys.disabled = false;
                    if (preCd) { sys.value = preCd; setSys(preCd); }
                });
        }

        function loadSgg(sidoVal) {
            resetRegionSel();
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

        sido.addEventListener('change', function () { loadSgg(sido.value); });
        sgg.addEventListener('change', function () { setRegion(sgg.value); });
        sys.addEventListener('change', function () { setSys(sys.value); });

        // 시스템 마스터 먼저 로드
        loadSystems(null);

        // 프리필 (수정/상세)
        var restore = document.getElementById('casRestore');
        var preRegion = restore ? restore.getAttribute('data-region') : null;
        var preSys = restore ? restore.getAttribute('data-sys') : null;
        if (preSys) loadSystems(preSys);
        if (preRegion) {
            fetch('/ops-doc/api/region?admSectC=' + encodeURIComponent(preRegion), { credentials: 'same-origin' })
                .then(function (r) { return r.json(); })
                .then(function (reg) {
                    if (!reg || !reg.sido) return;
                    sido.value = reg.sido;
                    return loadSgg(reg.sido).then(function () { sgg.value = preRegion; setRegion(preRegion); });
                });
        }
    }
    document.addEventListener('DOMContentLoaded', init);
})();

/* team-monitor — EventSource 클라이언트 (sprint team-monitor-dashboard, Phase 6-2) */
/* sprint team-monitor-wildcard-watcher: 정적 카드 → 동적 생성 (FR-5-b/d/e/f/g) */
(function() {
    'use strict';

    const STREAM_URL = '/admin/team-monitor/stream';

    let es = null;
    let reconnectAttempt = 0;
    let clockSkewMs = 0;
    let stoppedByEvict = false;

    /**
     * 마지막 snapshot 의 teamMeta 캐시 (FR-5-META-FALLBACK 보완).
     * onUpdate 로 신규 팀이 들어올 때 메타 즉시 적용 — snapshot 재발행 지연 차단.
     */
    let lastTeamMeta = {};

    const seenTimelineIds = new Set();
    const MAX_TIMELINE_RENDER = 20;

    /** SchemaVersion=1 (legacy) 호환 fallback 메타. */
    // ALLOW_FIVE_TEAM_FALLBACK
    const FALLBACK_META = {
        planner:   { emoji: '🧭', label: 'PLANNER',   sort_order: 10 },
        db:        { emoji: '🗄️', label: 'DB',        sort_order: 20 },
        developer: { emoji: '🛠️', label: 'DEVELOPER', sort_order: 30 },
        codex:     { emoji: '🤖', label: 'CODEX',     sort_order: 40 },
        designer:  { emoji: '🎨', label: 'DESIGNER',  sort_order: 50 }
    };
    const DEFAULT_EMOJI = '📋';

    function init() {
        connect();
    }

    function connect() {
        if (stoppedByEvict) return;
        try {
            es = new EventSource(STREAM_URL);
        } catch (e) {
            scheduleReconnect();
            return;
        }
        es.addEventListener('snapshot', onSnapshot);
        es.addEventListener('update', onUpdate);
        es.addEventListener('ping', onPing);
        es.addEventListener('evicted', onEvicted);
        es.addEventListener('open', onOpen);
        es.addEventListener('error', onError);
    }

    function onOpen() {
        reconnectAttempt = 0;
        setConnStatus('연결됨', 'ok');
    }

    function onError() {
        setConnStatus('연결 끊김 — 재연결', 'bad');
        if (es) { es.close(); es = null; }
        scheduleReconnect();
    }

    function scheduleReconnect() {
        if (stoppedByEvict) return;
        const backoff = [3000, 10000, 30000, 60000];
        const delay = backoff[Math.min(reconnectAttempt, backoff.length - 1)];
        reconnectAttempt++;
        setTimeout(connect, delay);
    }

    function onSnapshot(e) {
        const data = JSON.parse(e.data);
        if (data.serverTime) {
            clockSkewMs = Date.now() - parseISO(data.serverTime);
            updateSkewBadge();
        }
        const container = document.getElementById('cards');
        if (!container) return;

        // FR-5-f / N2: snapshot 처리 중 aria-busy 토글 (다중 announce 방지)
        container.setAttribute('aria-busy', 'true');

        const teams = Array.isArray(data.teams) ? data.teams : [];
        const meta = data.teamMeta || FALLBACK_META;   // T-G-clarify: schemaVersion=1 fallback

        // FR-5-META-FALLBACK: 마지막 snapshot teamMeta 캐시 (onUpdate 신규 팀에 즉시 적용)
        lastTeamMeta = meta;

        renderTeamsAndEmpty(container, teams, meta);
        teams.forEach(team => renderCard(team, false));   // 카드 데이터 갱신 (애니 X)

        container.setAttribute('aria-busy', 'false');

        // timeline
        const list = document.getElementById('timeline-list');
        if (list) list.innerHTML = '';
        seenTimelineIds.clear();
        if (Array.isArray(data.timeline)) {
            data.timeline.forEach(prependTimeline);
        }
    }

    function onUpdate(e) {
        const data = JSON.parse(e.data);
        if (data.serverTime) {
            const skew = Date.now() - parseISO(data.serverTime);
            clockSkewMs = clockSkewMs * 0.7 + skew * 0.3;
            updateSkewBadge();
        }
        if (data.team) {
            // 카드 미존재 (= 신규 팀 첫 등장) 시 createCard
            const container = document.getElementById('cards');
            if (container && !document.getElementById('card-' + data.team.team)) {
                // FR-5-META-FALLBACK: 우선순위 — update payload teamMeta → 마지막 snapshot 캐시 → metaFor 폴백
                const teamMeta = (data.teamMeta && data.teamMeta[data.team.team])
                    || (lastTeamMeta && lastTeamMeta[data.team.team])
                    || metaFor(data.team.team);
                container.appendChild(createCard(data.team, teamMeta));
                // empty placeholder 가 있으면 제거
                const empty = container.querySelector('.tm-empty');
                if (empty) empty.remove();
            }
            renderCard(data.team, true);
        }
        if (data.timelineEntry) prependTimeline(data.timelineEntry);
    }

    function onPing(e) {
        try {
            const data = JSON.parse(e.data);
            if (data.serverTime) {
                const skew = Date.now() - parseISO(data.serverTime);
                clockSkewMs = clockSkewMs * 0.7 + skew * 0.3;
                updateSkewBadge();
            }
        } catch (_) {}
    }

    function onEvicted() {
        stoppedByEvict = true;
        setConnStatus('다른 세션이 자리를 차지하여 종료. 새로고침 후 재연결', 'bad');
        if (es) { es.close(); es = null; }
    }

    /** team meta lookup — fallback 우선순위: meta → FALLBACK_META → default. */
    function metaFor(teamName) {
        return FALLBACK_META[teamName] || {
            emoji: DEFAULT_EMOJI,
            label: teamName.toUpperCase(),
            sort_order: 99
        };
    }

    /**
     * FR-5-b + T-I-02-A: 노드 identity 유지 — 기존 카드 DOM 재사용 + 위치 재배치.
     * 신규 팀은 createCard 후 append, 사라진 팀은 remove.
     * empty placeholder 도 동시 관리.
     */
    function renderTeamsAndEmpty(container, teams, meta) {
        // 0팀 → empty placeholder 1개만
        if (teams.length === 0) {
            container.querySelectorAll('.team-card').forEach(n => n.remove());
            if (!container.querySelector('.tm-empty')) {
                container.appendChild(createEmptyPlaceholder());
            }
            return;
        }

        // 1팀 이상 → empty placeholder 제거
        const empty = container.querySelector('.tm-empty');
        if (empty) empty.remove();

        const presentTeams = new Set(teams.map(t => t.team));
        const existing = new Map();
        container.querySelectorAll('.team-card').forEach(card => {
            existing.set(card.dataset.team, card);
        });

        // 응답에 없는 카드 제거
        existing.forEach((card, name) => {
            if (!presentTeams.has(name)) card.remove();
        });

        // 신규 / 재배치 (서버 정렬 순서대로 append — 동일 노드 append 시 이동, identity 유지)
        const fragment = document.createDocumentFragment();
        teams.forEach(team => {
            let card = existing.get(team.team);
            if (!card) {
                const teamMeta = (meta && meta[team.team]) || metaFor(team.team);
                card = createCard(team, teamMeta);
            }
            fragment.appendChild(card);
        });
        container.appendChild(fragment);
    }

    /** FR-5-b: 카드 DOM 동적 생성 (textContent 만 사용 — innerHTML 금지, NFR-5). */
    function createCard(team, meta) {
        meta = meta || metaFor(team.team);

        const card = document.createElement('article');
        card.className = 'team-card';
        card.dataset.team = team.team;
        card.id = 'card-' + team.team;
        card.setAttribute('role', 'status');
        card.setAttribute('aria-live', 'polite');
        card.setAttribute('aria-atomic', 'true');     // N2
        card.setAttribute('aria-label', meta.label || team.team.toUpperCase());

        // Header
        const header = document.createElement('header');
        header.className = 'card-head';
        const emoji = document.createElement('span');
        emoji.className = 'team-emoji';
        emoji.textContent = meta.emoji || DEFAULT_EMOJI;
        const labelEl = document.createElement('span');
        labelEl.className = 'team-label';
        labelEl.textContent = meta.label || team.team.toUpperCase();
        const stateEl = document.createElement('span');
        stateEl.className = 'card-state';
        stateEl.dataset.field = 'state';
        stateEl.textContent = '-';
        header.appendChild(emoji);
        header.appendChild(labelEl);
        header.appendChild(stateEl);

        // Progress bar
        const progressBar = document.createElement('div');
        progressBar.className = 'progress-bar';
        const fill = document.createElement('div');
        fill.className = 'fill';
        fill.dataset.field = 'progress-fill';
        fill.style.width = '0%';
        progressBar.appendChild(fill);

        // Card meta
        const cardMeta = document.createElement('div');
        cardMeta.className = 'card-meta';
        const progressText = document.createElement('span');
        progressText.className = 'progress-text';
        progressText.dataset.field = 'progress-text';
        progressText.textContent = '-';
        const updatedText = document.createElement('span');
        updatedText.className = 'updated-text';
        updatedText.dataset.field = 'updated';
        updatedText.textContent = '-';
        cardMeta.appendChild(progressText);
        cardMeta.appendChild(updatedText);

        // Task
        const cardTask = document.createElement('div');
        cardTask.className = 'card-task';
        cardTask.dataset.field = 'task';
        cardTask.textContent = '-';

        card.appendChild(header);
        card.appendChild(progressBar);
        card.appendChild(cardMeta);
        card.appendChild(cardTask);

        return card;
    }

    /** FR-5-g + N6: empty state placeholder (SR 안내 포함). */
    function createEmptyPlaceholder() {
        const div = document.createElement('div');
        div.className = 'tm-empty';
        div.setAttribute('role', 'status');
        div.setAttribute('aria-live', 'polite');
        div.setAttribute('aria-label', '활성 팀 없음');
        div.appendChild(document.createTextNode('활성 팀이 없습니다. '));
        const code = document.createElement('code');
        code.textContent = 'bash .team-workspace/set-status.sh <팀명> ...';
        div.appendChild(code);
        div.appendChild(document.createTextNode(' 로 첫 팀을 추가하세요.'));
        return div;
    }

    function renderCard(team, animate) {
        const card = document.getElementById('card-' + team.team);
        if (!card) return;
        if (animate) {
            card.classList.remove('state-changed');
            void card.offsetWidth; // reflow
            card.classList.add('state-changed');
        }
        if (team.state) card.setAttribute('data-state', team.state);
        setField(card, 'state', team.state || '-');
        const stateEl = card.querySelector('[data-field="state"]');
        if (stateEl) {
            stateEl.className = 'card-state';
            if (team.state) stateEl.classList.add('state-' + team.state);
        }
        const progress = (team.progress == null) ? 0 : team.progress;
        const fill = card.querySelector('[data-field="progress-fill"]');
        if (fill) fill.style.width = progress + '%';
        setField(card, 'progress-text', progress + '%');
        setField(card, 'task', team.task || '-');
        setField(card, 'updated', formatRelative(team.updatedAt));
    }

    function prependTimeline(entry) {
        if (!entry || !entry.id || seenTimelineIds.has(entry.id)) return;
        seenTimelineIds.add(entry.id);
        const list = document.getElementById('timeline-list');
        if (!list) return;
        const li = document.createElement('li');
        const t = document.createElement('span');
        t.className = 'tl-time';
        t.textContent = formatTime(entry.occurredAt);
        const chip = document.createElement('span');
        chip.className = 'tl-team-chip';
        chip.textContent = entry.team;
        const msg = document.createElement('span');
        msg.className = 'tl-msg';
        const fromState = entry.prevState || '(신규)';
        msg.textContent = fromState + ' → ' + (entry.newState || '-')
                          + '  ' + (entry.newProgress == null ? '' : (entry.newProgress + '%'))
                          + '  ' + (entry.task || '');
        li.appendChild(t);
        li.appendChild(chip);
        li.appendChild(msg);
        list.insertBefore(li, list.firstChild);
        while (list.children.length > MAX_TIMELINE_RENDER) {
            list.removeChild(list.lastChild);
        }
    }

    function setField(card, field, value) {
        const el = card.querySelector('[data-field="' + field + '"]');
        if (el) el.textContent = value;
    }

    function setConnStatus(text, kind) {
        const el = document.getElementById('conn-status');
        if (!el) return;
        el.textContent = text;
        el.className = 'tm-badge tm-badge-conn ' + (kind || '');
    }

    function parseISO(iso) {
        if (!iso) return Date.now();
        const t = Date.parse(iso);
        return isNaN(t) ? Date.now() : t;
    }

    /** S1: null/빈값이면 "갱신: 미기록" 반환 (now() fallback 금지). */
    function formatRelative(iso) {
        if (!iso) return '갱신: 미기록';
        const t = parseISO(iso);
        const serverNow = Date.now() - clockSkewMs;
        const diff = Math.max(0, serverNow - t);
        const sec = Math.floor(diff / 1000);
        if (sec < 60) return sec + '초 전';
        const min = Math.floor(sec / 60);
        if (min < 60) return min + '분 전';
        const hr = Math.floor(min / 60);
        if (hr < 24) return hr + '시간 전';
        return Math.floor(hr / 24) + '일 전';
    }

    function formatTime(iso) {
        if (!iso) return '--:--:--';
        const d = new Date(parseISO(iso));
        const pad = n => String(n).padStart(2, '0');
        return pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
    }

    function updateSkewBadge() {
        const badge = document.getElementById('skew-badge');
        if (!badge) return;
        if (Math.abs(clockSkewMs) > 5000) {
            badge.hidden = false;
        } else {
            badge.hidden = true;
        }
    }

    // 풀스크린 타임라인 expand 토글
    document.addEventListener('DOMContentLoaded', function() {
        const tl = document.getElementById('timeline');
        if (tl) {
            tl.addEventListener('click', function() {
                if (document.body.classList.contains('fullscreen')) {
                    tl.classList.toggle('expanded');
                }
            });
        }
        init();
    });
})();

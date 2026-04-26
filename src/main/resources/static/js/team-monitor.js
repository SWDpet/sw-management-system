/* team-monitor — EventSource 클라이언트 (sprint team-monitor-dashboard, Phase 6-2) */
(function() {
    'use strict';

    const STREAM_URL = '/admin/team-monitor/stream';
    const TEAMS = ['planner', 'db', 'developer', 'codex'];

    let es = null;
    let reconnectAttempt = 0;
    let clockSkewMs = 0;
    let stoppedByEvict = false;

    const seenTimelineIds = new Set();
    const MAX_TIMELINE_RENDER = 20;

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
        // S1: serverTime null/빈값이면 skew 계산 스킵
        if (data.serverTime) {
            clockSkewMs = Date.now() - parseISO(data.serverTime);
            updateSkewBadge();
        }
        if (Array.isArray(data.teams)) {
            data.teams.forEach(renderCard);
        }
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
        if (data.team) renderCard(data.team, true);
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

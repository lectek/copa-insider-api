/* admin-email.js */
(() => {
  const API_BASE = '/api/email';
  let page = 0, size = 20, autoTimer = null;

  // Elements
  const $ = (sel) => document.querySelector(sel);
  const el = (id) => document.getElementById(id);
  const $dest = el('f-destination');
  const $purpose = el('f-purpose');
  const $status = el('f-status');
  const $refresh = el('f-refresh');
  const $grid = el('grid-body');
  const $rawBox = el('rawBox');
  const $raw = el('raw');
  const $pageNum = el('pageNum');

  // Utils
  const qs = (obj) => new URLSearchParams(
    Object.entries(obj).filter(([, v]) => v !== '' && v != null)
  ).toString();

  const fmtTs = (ts) => {
    if (!ts) return '-';
    try { return new Date(ts).toLocaleString(); } catch { return ts; }
  };

  const badgeHTML = (status) => {
    // Ajuste as classes conforme seu main.css
    const cls = status === 'SENT' ? 'badge badge--success'
              : status === 'FAILED' ? 'badge badge--danger'
              : 'badge';
    return `<span class="${cls}">${status}</span>`;
  };

  const renderError = (text) => {
    $grid.innerHTML = `<tr><td colspan="9" class="text-danger">${text || 'Erro ao carregar.'}</td></tr>`;
  };

  // Data
  async function fetchPage() {
    const params = {
      page,
      size,
      destination: ($dest.value || '').trim(),
      purpose: ($purpose.value || '').trim(),
      status: $status.value || ''
    };
    const url = `${API_BASE}?${qs(params)}`;

    let res;
    try {
      res = await fetch(url, { headers: { 'Accept': 'application/json' } });
    } catch (e) {
      renderError('Falha de rede ao buscar dados.');
      return;
    }
    if (!res.ok) {
      renderError(await res.text().catch(() => 'Erro ao buscar dados.'));
      return;
    }
    const data = await res.json();
    renderGrid(data);
  }

  function renderGrid(pg) {
    $grid.innerHTML = '';
    const rows = (pg && pg.content) ? pg.content : [];
    if (rows.length === 0) {
      $grid.innerHTML = `<tr><td colspan="9" class="text-center text-muted">Sem resultados…</td></tr>`;
      $pageNum.textContent = (pg?.number ?? page) + 1;
      return;
    }
    for (const r of rows) {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td class="mono">${r.id}</td>
        <td>${r.purpose ?? '-'}</td>
        <td class="mono">${r.destination}</td>
        <td>${r.provider ?? '-'}</td>
        <td>${badgeHTML(r.status)}</td>
        <td>${r.attempts}</td>
        <td class="mono">${r.messageId ?? '-'}</td>
        <td>${fmtTs(r.updatedAt || r.createdAt)}</td>
        <td class="nowrap">
          <button class="btn btn--ghost" data-action="raw" data-id="${r.id}">Raw</button>
          ${r.status === 'FAILED' ? `<button class="btn btn--warning" data-action="retry" data-id="${r.id}">Retry</button>` : ''}
        </td>
      `;
      $grid.appendChild(tr);
    }
    $pageNum.textContent = (pg.number || 0) + 1;
  }

  async function viewRaw(id) {
    let res;
    try {
      res = await fetch(`${API_BASE}/${id}/raw`, { headers: { 'Accept': 'application/json' } });
    } catch {
      renderError('Falha de rede ao carregar o payload.');
      return;
    }
    if (!res.ok) {
      renderError(await res.text().catch(() => 'Erro ao carregar payload.'));
      return;
    }
    const text = await res.text();
    try {
      $raw.textContent = JSON.stringify(JSON.parse(text), null, 2);
    } catch {
      $raw.textContent = text;
    }
    $rawBox.hidden = false;
    $rawBox.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  async function retry(id) {
    if (!confirm('Reenfileirar este envio?')) return;
    let res;
    try {
      res = await fetch(`${API_BASE}/${id}/retry`, { method: 'POST' });
    } catch {
      alert('Falha de rede ao reenfileirar.');
      return;
    }
    if (res.status === 202) {
      fetchPage();
    } else {
      alert(await res.text().catch(() => 'Falha ao reenfileirar.'));
    }
  }

  // Debounce para filtros digitados
  function debounce(fn, ms = 400) {
    let t;
    return (...args) => {
      clearTimeout(t);
      t = setTimeout(() => fn.apply(null, args), ms);
    };
  }

  const debouncedSearch = debounce(() => { page = 0; fetchPage(); });

  // Events
  el('btnSearch').addEventListener('click', () => { page = 0; fetchPage(); });
  el('btnClear').addEventListener('click', () => {
    $dest.value = '';
    $purpose.value = '';
    $status.value = '';
    page = 0;
    fetchPage();
  });
  el('prevPage').addEventListener('click', () => { if (page > 0) { page--; fetchPage(); } });
  el('nextPage').addEventListener('click', () => { page++; fetchPage(); });

  $dest.addEventListener('input', debouncedSearch);
  $purpose.addEventListener('input', debouncedSearch);
  $status.addEventListener('change', () => { page = 0; fetchPage(); });

  $refresh.addEventListener('change', () => {
    if (autoTimer) { clearInterval(autoTimer); autoTimer = null; }
    const sec = parseInt($refresh.value, 10) || 0;
    if (sec > 0) autoTimer = setInterval(fetchPage, sec * 1000);
  });

  // Delegação de eventos para Raw/Retry
  document.addEventListener('click', (ev) => {
    const btn = ev.target.closest('button[data-action]');
    if (!btn) return;
    const id = btn.getAttribute('data-id');
    const action = btn.getAttribute('data-action');
    if (action === 'raw') viewRaw(id);
    if (action === 'retry') retry(id);
  });

  // First load
  fetchPage();
})();

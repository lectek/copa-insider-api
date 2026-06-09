(function () {
  const app = document.querySelector('.alysson-main[data-endpoint]');
  if (!app) {
    return;
  }

  const endpoint = String(app.dataset.endpoint || '/api/ia/ask');
  const assistantName = String(app.dataset.assistantName || 'Alysson');
  const whatsappLink = String(app.dataset.whatsappLink || '').trim();

  const STORAGE_SESSION_KEY = 'alysson-chat-session-id';
  const STORAGE_HISTORY_KEY = 'alysson-chat-history';
  const MAX_MESSAGE_LENGTH = 2000;
  const MAX_STORED_MESSAGES = 30;

  const $thread = document.getElementById('alysson-thread');
  const $form = document.getElementById('alysson-form');
  const $input = document.getElementById('alysson-input');
  const $send = document.getElementById('alysson-send');
  const $clear = document.getElementById('alysson-clear');
  const $reset = document.getElementById('alysson-reset');
  const $status = document.getElementById('alysson-status');
  const $quickPrompts = Array.from(document.querySelectorAll('[data-quick-prompt]'));

  let sessionId = restoreSessionId();
  let pending = false;
  let history = restoreHistory();

  if (!history.length) {
    history = [createMessage('assistant', buildWelcomeMessage())];
    persistState();
  }

  renderHistory();
  syncUiState();

  function buildWelcomeMessage() {
    return `Oi! Eu sou ${assistantName}, o atendente virtual da SaudeMaisFarma.\n\nPosso ajudar com produtos, entrega, pedidos, pagamento e uso da loja.`;
  }

  function createMessage(role, text) {
    return {
      id: `${role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
      role,
      text: String(text || '').trim()
    };
  }

  function restoreSessionId() {
    const stored = safeStorageGet(STORAGE_SESSION_KEY);
    if (stored) {
      return stored;
    }
    const generated = generateSessionId();
    safeStorageSet(STORAGE_SESSION_KEY, generated);
    return generated;
  }

  function restoreHistory() {
    const raw = safeStorageGet(STORAGE_HISTORY_KEY);
    if (!raw) {
      return [];
    }
    try {
      const parsed = JSON.parse(raw);
      if (!Array.isArray(parsed)) {
        return [];
      }
      return parsed
        .filter((item) => item && typeof item === 'object')
        .map((item) => createMessage(item.role, item.text))
        .filter((item) => item.text);
    } catch {
      return [];
    }
  }

  function persistState() {
    safeStorageSet(STORAGE_SESSION_KEY, sessionId);
    safeStorageSet(
      STORAGE_HISTORY_KEY,
      JSON.stringify(history.slice(-MAX_STORED_MESSAGES))
    );
  }

  function safeStorageGet(key) {
    try {
      return window.localStorage.getItem(key);
    } catch {
      return null;
    }
  }

  function safeStorageSet(key, value) {
    try {
      window.localStorage.setItem(key, value);
    } catch {
      // noop
    }
  }

  function safeStorageRemove(key) {
    try {
      window.localStorage.removeItem(key);
    } catch {
      // noop
    }
  }

  function generateSessionId() {
    if (window.crypto && typeof window.crypto.randomUUID === 'function') {
      return window.crypto.randomUUID();
    }
    return `alysson-${Date.now()}-${Math.random().toString(16).slice(2)}`;
  }

  function syncUiState() {
    if ($send) {
      $send.disabled = pending;
    }
    if ($clear) {
      $clear.disabled = pending || history.length <= 1;
    }
    if ($reset) {
      $reset.disabled = pending;
    }
    if ($input) {
      $input.disabled = pending;
    }
    $quickPrompts.forEach((button) => {
      button.disabled = pending;
    });
  }

  function setStatus(message, type) {
    if (!$status) {
      return;
    }
    $status.textContent = String(message || '').trim();
    $status.classList.toggle('is-error', type === 'error');
  }

  function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (char) => ({
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;'
    })[char]);
  }

  function labelForRole(role) {
    if (role === 'user') {
      return 'Voce';
    }
    if (role === 'system') {
      return 'Sistema';
    }
    return assistantName;
  }

  function renderHistory() {
    if (!$thread) {
      return;
    }
    $thread.innerHTML = history.map((message) => renderMessage(message)).join('');
    scrollToBottom();
  }

  function renderMessage(message) {
    const safeRole = String(message.role || 'assistant');
    const safeText = escapeHtml(message.text || '');
    return `
      <article class="alysson-message alysson-message--${safeRole}">
        <span class="alysson-message-meta">${escapeHtml(labelForRole(safeRole))}</span>
        <div class="alysson-message-bubble">${safeText}</div>
      </article>
    `;
  }

  function renderPendingMessage() {
    if (!$thread) {
      return;
    }
    const shell = document.createElement('article');
    shell.className = 'alysson-message alysson-message--assistant alysson-message--pending';
    shell.id = 'alysson-pending-message';
    shell.innerHTML = `
      <span class="alysson-message-meta">${escapeHtml(assistantName)}</span>
      <div class="alysson-message-bubble">
        <span class="alysson-typing" aria-hidden="true">
          <span></span>
          <span></span>
          <span></span>
        </span>
        <span>Pensando na melhor resposta...</span>
      </div>
    `;
    $thread.appendChild(shell);
    scrollToBottom();
  }

  function clearPendingMessage() {
    const pendingNode = document.getElementById('alysson-pending-message');
    pendingNode?.remove();
  }

  function scrollToBottom() {
    if (!$thread) {
      return;
    }
    $thread.scrollTop = $thread.scrollHeight;
  }

  function normalizeMessage(raw) {
    return String(raw || '')
      .replace(/\r\n/g, '\n')
      .trim()
      .slice(0, MAX_MESSAGE_LENGTH);
  }

  function parseJsonSafely(raw) {
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }

  function normalizeErrorMessage(raw, fallback) {
    const text = String(raw || '')
      .replace(/<[^>]*>/g, ' ')
      .replace(/\s+/g, ' ')
      .trim();
    return text || fallback;
  }

  function isLikelyHtml(contentType, raw) {
    const safeType = String(contentType || '').toLowerCase();
    const safeRaw = String(raw || '');
    return safeType.includes('text/html')
      || /<html[\s>]/i.test(safeRaw)
      || /<body[\s>]/i.test(safeRaw);
  }

  function friendlyErrorMessage(response, raw) {
    const payload = parseJsonSafely(raw);
    const contentType = response.headers.get('content-type') || '';
    if (isLikelyHtml(contentType, raw)) {
      return 'O atendimento virtual ficou indisponivel agora. Tente novamente em instantes.';
    }
    return normalizeErrorMessage(
      payload?.message || payload?.error || payload?.detail || raw,
      `Falha ao falar com ${assistantName} (HTTP ${response.status}).`
    );
  }

  async function sendMessage(rawMessage) {
    const message = normalizeMessage(rawMessage);
    if (!message || pending) {
      return;
    }

    pending = true;
    setStatus('Enviando sua mensagem...', '');
    syncUiState();

    history.push(createMessage('user', message));
    history = history.slice(-MAX_STORED_MESSAGES);
    persistState();
    renderHistory();

    if ($input) {
      $input.value = '';
    }
    renderPendingMessage();

    try {
      const response = await window.fetch(endpoint, {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify({
          message,
          sessionId
        })
      });

      const raw = await response.text();
      if (!response.ok) {
        throw new Error(friendlyErrorMessage(response, raw));
      }

      const payload = parseJsonSafely(raw);
      const nextSessionId = String(payload?.sessionId || payload?.session_id || '').trim();
      const answer = normalizeMessage(payload?.answer);
      if (!answer) {
        throw new Error(`${assistantName} nao conseguiu responder agora.`);
      }

      if (nextSessionId) {
        sessionId = nextSessionId;
      }
      clearPendingMessage();
      history.push(createMessage('assistant', answer));
      history = history.slice(-MAX_STORED_MESSAGES);
      persistState();
      renderHistory();
      setStatus(`${assistantName} respondeu.`, '');
    } catch (error) {
      clearPendingMessage();
      const fallback = normalizeErrorMessage(
        error?.message,
        `${assistantName} ficou indisponivel no momento.`
      );
      history.push(createMessage(
        'system',
        whatsappLink
          ? `${fallback}\n\nSe preferir, fale agora com nosso atendimento humano pelo WhatsApp.`
          : fallback
      ));
      history = history.slice(-MAX_STORED_MESSAGES);
      persistState();
      renderHistory();
      setStatus(fallback, 'error');
    } finally {
      pending = false;
      syncUiState();
    }
  }

  function resetConversation() {
    sessionId = generateSessionId();
    history = [createMessage('assistant', buildWelcomeMessage())];
    safeStorageRemove(STORAGE_HISTORY_KEY);
    persistState();
    renderHistory();
    setStatus('Nova conversa iniciada.', '');
    syncUiState();
    $input?.focus();
  }

  $form?.addEventListener('submit', async (event) => {
    event.preventDefault();
    await sendMessage($input?.value);
  });

  $input?.addEventListener('keydown', async (event) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      await sendMessage($input?.value);
    }
  });

  $clear?.addEventListener('click', () => {
    if ($input) {
      $input.value = '';
      $input.focus();
    }
    setStatus('Mensagem limpa.', '');
  });

  $reset?.addEventListener('click', () => {
    if (pending) {
      return;
    }
    resetConversation();
  });

  $quickPrompts.forEach((button) => {
    button.addEventListener('click', async () => {
      const prompt = button.getAttribute('data-quick-prompt') || '';
      await sendMessage(prompt);
    });
  });
})();

(() => {
  const API_BASE = "/api/admin/pagamentos/metodos";
  const API_POS_CONFIG = "/api/admin/pagamentos/terminal/config";
  const API_POS_TEST = "/api/admin/pagamentos/terminal/test";

  const bodyEl = document.getElementById("custom-methods-body");
  const formEl = document.getElementById("pagamentos-metodo-form");
  const feedbackEl = document.getElementById("custom-methods-feedback");
  const nomeEl = document.getElementById("novoMetodoNome");
  const tipoEl = document.getElementById("novoMetodoTipo");
  const taxaEl = document.getElementById("novoMetodoTaxa");
  const ativoEl = document.getElementById("novoMetodoAtivo");
  const nomeErrorEl = document.getElementById("novoMetodoNomeError");
  const tipoErrorEl = document.getElementById("novoMetodoTipoError");
  const taxaErrorEl = document.getElementById("novoMetodoTaxaError");
  const editModalEl = document.getElementById("modal-metodo-editar");
  const editFormEl = document.getElementById("pagamentos-metodo-edit-form");
  const editIdEl = document.getElementById("editMetodoId");
  const editNomeEl = document.getElementById("editMetodoNome");
  const editTipoEl = document.getElementById("editMetodoTipo");
  const editTaxaEl = document.getElementById("editMetodoTaxa");
  const editAtivoEl = document.getElementById("editMetodoAtivo");
  const editNomeErrorEl = document.getElementById("editMetodoNomeError");
  const editTipoErrorEl = document.getElementById("editMetodoTipoError");
  const editTaxaErrorEl = document.getElementById("editMetodoTaxaError");
  const editCancelEl = document.getElementById("pagamentos-metodo-edit-cancel");
  const editCloseButtons = document.querySelectorAll("[data-close-edit-modal]");
  const posEnabledEl = document.getElementById("posEnabled");
  const posModeEl = document.getElementById("posMode");
  const posProviderEl = document.getElementById("posProvider");
  const posEndpointEl = document.getElementById("posEndpointUrl");
  const posTerminalIdEl = document.getElementById("posTerminalId");
  const posMerchantIdEl = document.getElementById("posMerchantId");
  const posTimeoutEl = document.getElementById("posTimeoutMs");
  const posSecretEl = document.getElementById("posSecret");
  const posClearSecretEl = document.getElementById("posClearSecret");
  const posSecretHintEl = document.getElementById("posSecretHint");
  const posTestAmountEl = document.getElementById("posTestAmount");
  const posTestMethodEl = document.getElementById("posTestMethod");
  const posSaveBtn = document.getElementById("posSaveBtn");
  const posTestBtn = document.getElementById("posTestBtn");
  const posFeedbackEl = document.getElementById("posFeedback");
  let methodsCache = [];
  let editingMethodId = null;
  let posSecretConfigured = false;

  if (!bodyEl || !formEl || !editFormEl || !editModalEl) {
    return;
  }

  function getCsrf() {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header =
      document.querySelector('meta[name="_csrf_header"]')?.content || "X-CSRF-TOKEN";
    return { token, header };
  }

  function withCsrf(init = {}) {
    const { token, header } = getCsrf();
    init.headers = Object.assign({}, init.headers || {}, token ? { [header]: token } : {});
    return init;
  }

  function setFeedback(message, error = false) {
    if (!feedbackEl) {
      return;
    }
    feedbackEl.textContent = message || "";
    feedbackEl.style.color = error ? "#b91c1c" : "var(--layout-muted)";
  }

  function setPosFeedback(message, error = false) {
    if (!posFeedbackEl) {
      return;
    }
    posFeedbackEl.textContent = message || "";
    posFeedbackEl.style.color = error ? "#b91c1c" : "var(--layout-muted)";
  }

  function refreshPosSecretHint() {
    if (!posSecretHintEl) {
      return;
    }
    posSecretHintEl.textContent = posSecretConfigured
      ? "Token salvo. Preencha apenas se quiser substituir."
      : "Token ainda nao configurado.";
  }

  function refreshPosFieldsByMode() {
    const isWebhook = String(posModeEl?.value ?? "").toLowerCase() === "webhook";
    if (posEndpointEl) {
      posEndpointEl.required = isWebhook;
      posEndpointEl.placeholder = isWebhook
        ? "Obrigatorio em webhook: https://.../maquineta/autorizar"
        : "https://.../maquineta/autorizar";
    }
  }

  function refreshPosSecretInputState() {
    if (!posSecretEl) {
      return;
    }
    const clearChecked = !!posClearSecretEl?.checked;
    if (clearChecked) {
      posSecretEl.value = "";
      posSecretEl.setAttribute("disabled", "disabled");
    } else {
      posSecretEl.removeAttribute("disabled");
    }
  }

  function formatTaxa(value) {
    if (value == null || value === "") {
      return "-";
    }
    const parsed = Number(value);
    if (!Number.isFinite(parsed)) {
      return "-";
    }
    return `${parsed.toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}%`;
  }

  function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, (char) => {
      const map = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;",
      };
      return map[char];
    });
  }

  function parseApiError(payload, fallback) {
    if (!payload) {
      return fallback;
    }
    if (typeof payload === "string") {
      return payload.trim() || fallback;
    }
    const message = payload.message || payload.error || payload.detail;
    return String(message || fallback).trim();
  }

  function normalizeText(value) {
    return String(value ?? "")
      .trim()
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "");
  }

  async function fetchJson(url, init = {}) {
    const response = await fetch(url, withCsrf(init));
    const raw = await response.text();
    let payload = null;
    try {
      payload = raw ? JSON.parse(raw) : null;
    } catch (err) {
      payload = raw;
    }
    if (!response.ok) {
      const fallback = `Falha na operacao (HTTP ${response.status}).`;
      throw new Error(parseApiError(payload, fallback));
    }
    return payload;
  }

  function renderRows(methods) {
    if (!Array.isArray(methods) || methods.length === 0) {
      bodyEl.innerHTML = `
        <tr>
          <td colspan="5" class="text-center text-muted">
            Nenhum metodo adicional cadastrado.
          </td>
        </tr>
      `;
      return;
    }

    bodyEl.innerHTML = methods
      .map(
        (method) => `
          <tr>
            <td>${escapeHtml(method.nome)}</td>
            <td>${escapeHtml(method.tipo || "custom")}</td>
            <td>${escapeHtml(formatTaxa(method.taxa))}</td>
            <td>
              ${
                method.ativo
                  ? '<span class="badge badge--success">Ativo</span>'
                  : '<span class="badge badge--neutral">Inativo</span>'
              }
            </td>
            <td class="table-actions">
              <button class="btn btn--ghost" type="button"
                      data-action="edit" data-id="${escapeHtml(method.id)}">
                Editar
              </button>
              <button class="btn btn--ghost" type="button"
                      data-action="delete" data-id="${escapeHtml(method.id)}">
                Remover
              </button>
            </td>
          </tr>
        `
      )
      .join("");
  }

  async function loadMethods() {
    setFeedback("Carregando metodos...");
    try {
      const methods = await fetchJson(API_BASE, { method: "GET" });
      methodsCache = Array.isArray(methods) ? methods : [];
      renderRows(methods);
      setFeedback("");
    } catch (error) {
      setFeedback(error.message || "Nao foi possivel carregar os metodos.", true);
    }
  }

  function parseTimeout(value) {
    const parsed = Number(value);
    if (!Number.isInteger(parsed) || parsed < 1000 || parsed > 30000) {
      throw new Error("Timeout deve estar entre 1000 e 30000 ms.");
    }
    return parsed;
  }

  function readPosConfigPayload() {
    const endpointUrl = String(posEndpointEl?.value ?? "").trim();
    const timeoutMs = parseTimeout(String(posTimeoutEl?.value ?? "10000"));
    const clearSecret = !!posClearSecretEl?.checked;
    const secretRaw = String(posSecretEl?.value ?? "").trim();
    const mode = String(posModeEl?.value ?? "mock").trim().toLowerCase() || "mock";
    if (mode === "webhook" && !endpointUrl) {
      throw new Error("Endpoint obrigatorio para modo webhook.");
    }
    return {
      enabled: !!posEnabledEl?.checked,
      mode,
      provider: String(posProviderEl?.value ?? "").trim(),
      endpointUrl,
      terminalId: String(posTerminalIdEl?.value ?? "").trim(),
      merchantId: String(posMerchantIdEl?.value ?? "").trim(),
      timeoutMs,
      secret: clearSecret ? null : (secretRaw || null),
      clearSecret,
    };
  }

  function applyPosConfig(config) {
    if (!config) {
      return;
    }
    if (posEnabledEl) {
      posEnabledEl.checked = !!config.enabled;
    }
    if (posModeEl) {
      posModeEl.value = config.mode || "mock";
    }
    if (posProviderEl) {
      posProviderEl.value = config.provider || "";
    }
    if (posEndpointEl) {
      posEndpointEl.value = config.endpointUrl || "";
    }
    if (posTerminalIdEl) {
      posTerminalIdEl.value = config.terminalId || "";
    }
    if (posMerchantIdEl) {
      posMerchantIdEl.value = config.merchantId || "";
    }
    if (posTimeoutEl) {
      posTimeoutEl.value = String(config.timeoutMs || 10000);
    }
    if (posSecretEl) {
      posSecretEl.value = "";
    }
    if (posClearSecretEl) {
      posClearSecretEl.checked = false;
    }
    posSecretConfigured = !!config.secretConfigured;
    refreshPosSecretHint();
    refreshPosFieldsByMode();
    refreshPosSecretInputState();
  }

  async function loadPosConfig() {
    if (!posSaveBtn || !posTestBtn) {
      return;
    }
    setPosFeedback("Carregando configuracao da maquineta...");
    try {
      const payload = await fetchJson(API_POS_CONFIG, { method: "GET" });
      applyPosConfig(payload);
      setPosFeedback("");
    } catch (error) {
      setPosFeedback(
        error.message || "Nao foi possivel carregar a configuracao da maquineta.",
        true
      );
    }
  }

  async function savePosConfig() {
    if (!posSaveBtn) {
      return;
    }
    try {
      const payload = readPosConfigPayload();
      setPosFeedback("Salvando configuracao da maquineta...");
      const saved = await fetchJson(API_POS_CONFIG, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      applyPosConfig(saved);
      setPosFeedback("Configuracao da maquineta salva.");
    } catch (error) {
      setPosFeedback(error.message || "Nao foi possivel salvar a maquineta.", true);
    }
  }

  async function testPosConfig() {
    if (!posTestBtn) {
      return;
    }
    try {
      const configPayload = readPosConfigPayload();
      const savedConfig = await fetchJson(API_POS_CONFIG, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(configPayload),
      });
      applyPosConfig(savedConfig);
      const amount = Number(String(posTestAmountEl?.value ?? "0").replace(",", "."));
      if (!Number.isFinite(amount) || amount <= 0) {
        throw new Error("Informe um valor de teste maior que zero.");
      }
      setPosFeedback("Executando teste da maquineta...");
      const result = await fetchJson(API_POS_TEST, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          valor: amount,
          metodo: String(posTestMethodEl?.value ?? "credito"),
          referencia: "ADMIN-TESTE",
        }),
      });
      if (result && result.approved) {
        const tx = result.transactionId ? ` Tx: ${result.transactionId}.` : "";
        setPosFeedback((result.message || "Pagamento aprovado.") + tx);
        return;
      }
      setPosFeedback(result?.message || "Pagamento recusado no teste.", true);
    } catch (error) {
      setPosFeedback(error.message || "Falha ao testar maquineta.", true);
    }
  }

  function parseTaxaInput(value) {
    const trimmed = String(value ?? "").trim();
    if (!trimmed) {
      return null;
    }
    const parsed = Number(trimmed.replace(",", "."));
    if (!Number.isFinite(parsed) || parsed < 0) {
      throw new Error("Taxa invalida.");
    }
    return parsed;
  }

  function parseAtivoInput(value) {
    return String(value).toLowerCase() !== "false";
  }

  function normalizeType(value) {
    const type = String(value ?? "").trim();
    return type || "custom";
  }

  function clearFieldError(field, errorEl) {
    if (field) {
      field.classList.remove("is-invalid");
      field.removeAttribute("aria-invalid");
    }
    if (errorEl) {
      errorEl.textContent = "";
    }
  }

  function setFieldError(field, errorEl, message) {
    if (field) {
      field.classList.add("is-invalid");
      field.setAttribute("aria-invalid", "true");
    }
    if (errorEl) {
      errorEl.textContent = message || "";
    }
  }

  function clearCreateErrors() {
    clearFieldError(nomeEl, nomeErrorEl);
    clearFieldError(tipoEl, tipoErrorEl);
    clearFieldError(taxaEl, taxaErrorEl);
  }

  function clearEditErrors() {
    clearFieldError(editNomeEl, editNomeErrorEl);
    clearFieldError(editTipoEl, editTipoErrorEl);
    clearFieldError(editTaxaEl, editTaxaErrorEl);
  }

  function validateMethodForm(values, opts = {}) {
    const isEdit = opts.isEdit === true;
    const errors = [];
    const nome = String(values.nome ?? "").trim();
    const tipoRaw = String(values.tipo ?? "").trim().toLowerCase();
    const tipo = normalizeType(tipoRaw);

    if (!nome) {
      errors.push({
        field: "nome",
        message: "Nome do metodo obrigatorio.",
      });
    } else if (nome.length > 120) {
      errors.push({
        field: "nome",
        message: "Nome pode ter no maximo 120 caracteres.",
      });
    }

    if (!["offline", "online", "custom", "pos"].includes(tipo)) {
      errors.push({
        field: "tipo",
        message: "Tipo invalido. Use offline, online, custom ou pos.",
      });
    }

    let taxa = null;
    try {
      taxa = parseTaxaInput(values.taxa);
      if (taxa != null && taxa > 1000) {
        errors.push({
          field: "taxa",
          message: "Taxa acima do limite permitido.",
        });
      }
    } catch (error) {
      errors.push({
        field: "taxa",
        message: "Taxa invalida.",
      });
    }

    if (errors.length > 0) {
      if (isEdit) {
        clearEditErrors();
      } else {
        clearCreateErrors();
      }

      errors.forEach((error) => {
        if (error.field === "nome") {
          setFieldError(
            isEdit ? editNomeEl : nomeEl,
            isEdit ? editNomeErrorEl : nomeErrorEl,
            error.message
          );
          return;
        }
        if (error.field === "tipo") {
          setFieldError(
            isEdit ? editTipoEl : tipoEl,
            isEdit ? editTipoErrorEl : tipoErrorEl,
            error.message
          );
          return;
        }
        if (error.field === "taxa") {
          setFieldError(
            isEdit ? editTaxaEl : taxaEl,
            isEdit ? editTaxaErrorEl : taxaErrorEl,
            error.message
          );
        }
      });
      return null;
    }

    return {
      nome,
      tipo,
      taxa,
      ativo: parseAtivoInput(values.ativo),
    };
  }

  function applyServerFieldErrors(message, opts = {}) {
    const isEdit = opts.isEdit === true;
    const raw = String(message ?? "").trim();
    const normalized = normalizeText(raw);
    if (!raw) {
      return false;
    }

    let mapped = false;
    if (isEdit) {
      clearEditErrors();
    } else {
      clearCreateErrors();
    }

    const mapError = (field, text) => {
      if (field === "nome") {
        setFieldError(
          isEdit ? editNomeEl : nomeEl,
          isEdit ? editNomeErrorEl : nomeErrorEl,
          text
        );
        mapped = true;
        return;
      }
      if (field === "tipo") {
        setFieldError(
          isEdit ? editTipoEl : tipoEl,
          isEdit ? editTipoErrorEl : tipoErrorEl,
          text
        );
        mapped = true;
        return;
      }
      if (field === "taxa") {
        setFieldError(
          isEdit ? editTaxaEl : taxaEl,
          isEdit ? editTaxaErrorEl : taxaErrorEl,
          text
        );
        mapped = true;
      }
    };

    const normalizedParts = normalized
      .split(";")
      .map((part) => part.trim())
      .filter(Boolean);

    normalizedParts.forEach((part) => {
      if (part.startsWith("nome:") || part.includes("nome do metodo")) {
        mapError("nome", "Nome do metodo invalido.");
        return;
      }
      if (part.startsWith("tipo:") || part.includes("tipo invalido")) {
        mapError("tipo", "Tipo invalido.");
        return;
      }
      if (part.startsWith("taxa:") || part.includes("taxa")) {
        mapError("taxa", "Taxa invalida.");
      }
    });

    if (!mapped && normalized.includes("ja existe")) {
      mapError("nome", "Metodo ja existe.");
    }
    if (!mapped && normalized.includes("nome") && normalized.includes("obrigatorio")) {
      mapError("nome", "Nome do metodo obrigatorio.");
    }
    if (!mapped && normalized.includes("tipo") && normalized.includes("inval")) {
      mapError("tipo", "Tipo invalido.");
    }
    if (
      !mapped
      && normalized.includes("taxa")
      && (
        normalized.includes("inval")
        || normalized.includes("negativa")
        || normalized.includes("nao pode")
      )
    ) {
      mapError("taxa", "Taxa invalida.");
    }

    return mapped;
  }

  function openModal(modal) {
    if (!modal) {
      return;
    }
    modal.removeAttribute("hidden");
    modal.style.display = "flex";
    modal.setAttribute("aria-hidden", "false");
    document.body.classList.add("modal-open");
  }

  function closeModal(modal) {
    if (!modal) {
      return;
    }
    modal.setAttribute("hidden", "");
    modal.style.display = "none";
    modal.setAttribute("aria-hidden", "true");
    if (!document.querySelector(".modal:not([hidden])")) {
      document.body.classList.remove("modal-open");
    }
  }

  function openEditForm(method) {
    if (!method) {
      return;
    }
    editingMethodId = method.id;
    editIdEl.value = method.id || "";
    editNomeEl.value = method.nome || "";
    editTipoEl.value = normalizeType(method.tipo);
    editTaxaEl.value = method.taxa == null ? "" : String(method.taxa);
    editAtivoEl.value = method.ativo ? "true" : "false";
    clearEditErrors();
    openModal(editModalEl);
    editNomeEl.focus();
  }

  function closeEditForm() {
    editingMethodId = null;
    editFormEl.reset();
    editIdEl.value = "";
    clearEditErrors();
    closeModal(editModalEl);
  }

  async function createMethod(event) {
    event.preventDefault();
    clearCreateErrors();
    const validated = validateMethodForm(
      {
        nome: nomeEl?.value,
        tipo: tipoEl?.value,
        taxa: taxaEl?.value,
        ativo: ativoEl?.value,
      },
      { isEdit: false }
    );
    if (!validated) {
      setFeedback("Corrija os campos destacados.", true);
      return;
    }

    try {
      const payload = validated;
      await fetchJson(API_BASE, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      formEl.reset();
      if (ativoEl) {
        ativoEl.value = "true";
      }
      clearCreateErrors();
      setFeedback("Metodo cadastrado com sucesso.");
      await loadMethods();
    } catch (error) {
      const msg = error.message || "Nao foi possivel cadastrar o metodo.";
      const mapped = applyServerFieldErrors(msg, { isEdit: false });
      setFeedback(mapped ? "Corrija os campos destacados." : msg, true);
    }
  }

  async function editMethod(id) {
    const current = methodsCache.find((item) => item.id === id);
    if (!current) {
      setFeedback("Metodo de pagamento nao encontrado.", true);
      return;
    }
    openEditForm(current);
    setFeedback("");
  }

  async function saveEditMethod(event) {
    event.preventDefault();
    const id = String(editIdEl?.value || "").trim();
    if (!id) {
      setFeedback("Metodo de pagamento invalido.", true);
      return;
    }
    clearEditErrors();
    const validated = validateMethodForm(
      {
        nome: editNomeEl?.value,
        tipo: editTipoEl?.value,
        taxa: editTaxaEl?.value,
        ativo: editAtivoEl?.value,
      },
      { isEdit: true }
    );
    if (!validated) {
      setFeedback("Corrija os campos destacados.", true);
      return;
    }

    try {
      const payload = validated;
      await fetchJson(`${API_BASE}/${encodeURIComponent(id)}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      setFeedback("Metodo atualizado com sucesso.");
      closeEditForm();
      await loadMethods();
    } catch (error) {
      const msg = error.message || "Nao foi possivel atualizar o metodo.";
      const mapped = applyServerFieldErrors(msg, { isEdit: true });
      setFeedback(mapped ? "Corrija os campos destacados." : msg, true);
    }
  }

  async function removeMethod(id) {
    const confirmed = window.confirm("Deseja remover este metodo de pagamento?");
    if (!confirmed) {
      return;
    }
    try {
      await fetchJson(`${API_BASE}/${encodeURIComponent(id)}`, {
        method: "DELETE",
      });
      if (editingMethodId === id) {
        closeEditForm();
      }
      setFeedback("Metodo removido com sucesso.");
      await loadMethods();
    } catch (error) {
      setFeedback(error.message || "Nao foi possivel remover o metodo.", true);
    }
  }

  bodyEl.addEventListener("click", async (event) => {
    const target = event.target;
    if (!(target instanceof Element)) {
      return;
    }
    const actionButton = target.closest("[data-action]");
    if (!actionButton) {
      return;
    }
    const action = actionButton.getAttribute("data-action");
    const id = actionButton.getAttribute("data-id");
    if (!id) {
      return;
    }

    if (action === "edit") {
      await editMethod(id);
      return;
    }
    if (action === "delete") {
      await removeMethod(id);
    }
  });

  formEl.addEventListener("submit", createMethod);
  editFormEl.addEventListener("submit", saveEditMethod);
  nomeEl?.addEventListener("input", () => clearFieldError(nomeEl, nomeErrorEl));
  tipoEl?.addEventListener("change", () => clearFieldError(tipoEl, tipoErrorEl));
  taxaEl?.addEventListener("input", () => clearFieldError(taxaEl, taxaErrorEl));
  editNomeEl?.addEventListener("input", () => clearFieldError(editNomeEl, editNomeErrorEl));
  editTipoEl?.addEventListener("change", () => clearFieldError(editTipoEl, editTipoErrorEl));
  editTaxaEl?.addEventListener("input", () => clearFieldError(editTaxaEl, editTaxaErrorEl));
  posSaveBtn?.addEventListener("click", savePosConfig);
  posTestBtn?.addEventListener("click", testPosConfig);
  posModeEl?.addEventListener("change", () => {
    refreshPosFieldsByMode();
    setPosFeedback("");
  });
  posSecretEl?.addEventListener("input", () => {
    setPosFeedback("");
  });
  posClearSecretEl?.addEventListener("change", () => {
    refreshPosSecretInputState();
    setPosFeedback("");
  });
  editCancelEl?.addEventListener("click", () => {
    closeEditForm();
    setFeedback("");
  });
  editCloseButtons.forEach((button) => {
    button.addEventListener("click", () => {
      closeEditForm();
      setFeedback("");
    });
  });
  document.addEventListener("keydown", (event) => {
    if (event.key !== "Escape") {
      return;
    }
    if (!editModalEl.hasAttribute("hidden")) {
      closeEditForm();
      setFeedback("");
    }
  });
  refreshPosFieldsByMode();
  refreshPosSecretInputState();
  loadMethods();
  loadPosConfig();
})();

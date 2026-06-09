(() => {
  const input = document.getElementById("quickProdutoTermo");
  const clearBtn = document.getElementById("quickProdutoClear");
  const hint = document.getElementById("quickProdutoHint");
  const results = document.getElementById("quickProdutoResultados");
  const pendingInput = document.getElementById("pendingProdutoTermo");
  const pendingBuscarBtn = document.getElementById("pendingProdutoBuscar");
  const pendingHint = document.getElementById("pendingProdutoHint");
  const pendingResults = document.getElementById("pendingProdutoResultados");
  const pendingLoadMoreBtn = document.getElementById("pendingProdutoLoadMore");
  const imageInputs = Array.from(
    document.querySelectorAll('[data-product-image-source]')
  );
  const imagePreview = document.getElementById("produtoImagemPreviewNovo");
  const imageHint = document.getElementById("produtoImagemHintNovo");

  if (!input || !results || !hint) return;

  const fields = {
    legacyId: document.getElementById("legacyId"),
    nome: document.getElementById("nome"),
    descricao: document.getElementById("descricao"),
    precoVenda: document.getElementById("precoVenda"),
    precoPromocional: document.getElementById("precoPromocional"),
    estoque: document.getElementById("estoque"),
    categoria: document.getElementById("categoria"),
    codigoBarras: document.getElementById("codigoBarras"),
    metodoLeituraCodigoBarras: document.getElementById("metodoLeituraCodigoBarras"),
    fabricante: document.getElementById("fabricante"),
    unidade: document.getElementById("unidade"),
    disponivel: document.getElementById("disponivel")
  };

  let debounceId = null;
  let requestId = 0;
  let pendingPage = 0;
  let pendingHasNext = false;
  let pendingItems = [];
  let pendingLoading = false;
  let pendingQuery = "";

  const setHint = (message) => {
    hint.textContent = message;
  };

  const setPendingHint = (message) => {
    if (!pendingHint) return;
    pendingHint.textContent = message;
  };

  const setHints = (message) => {
    setHint(message);
    setPendingHint(message);
  };

  const updateImagePreview = (file) => {
    if (!imagePreview) return;
    if (!(file instanceof File)) {
      imagePreview.src = "/img/produtos/placeholder-generico.png";
      return;
    }
    imagePreview.src = URL.createObjectURL(file);
  };

  const updateImageHint = (files, source) => {
    if (!imageHint) return;
    const selectedFiles = Array.from(files || []).filter((file) => file instanceof File);
    if (!selectedFiles.length) {
      imageHint.textContent = "Voce pode tirar a foto direto pelo celular ou escolher um ou varios arquivos existentes.";
      return;
    }
    const sourceLabel = source === "camera" ? "camera do celular" : "galeria/arquivos";
    if (selectedFiles.length === 1) {
      const file = selectedFiles[0];
      imageHint.textContent = `Imagem pronta para envio via ${sourceLabel}: ${file.name} (${Math.max(1, Math.round(file.size / 1024))} KB).`;
      return;
    }
    imageHint.textContent = `${selectedFiles.length} imagens prontas para envio via ${sourceLabel}. A primeira sera usada como principal.`;
  };

  const syncImageInputs = (activeInput) => {
    imageInputs.forEach((inputNode) => {
      if (inputNode !== activeInput) {
        inputNode.value = "";
      }
    });

    const files = Array.from(activeInput?.files || []);
    updateImagePreview(files[0]);
    updateImageHint(files, activeInput?.dataset?.productImageSource || "");
  };

  const money = (value) => {
    if (value == null || value === "") return "-";
    const n = Number(value);
    if (!Number.isFinite(n)) return "-";
    return n.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
  };

  const escapeHtml = (value) =>
    String(value ?? "").replace(/[&<>"']/g, (char) => ({
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#039;"
    })[char]);

  const clearResults = () => {
    results.innerHTML = "";
  };

  const hasValue = (el) => {
    if (!el) return false;
    return String(el.value ?? "").trim().length > 0;
  };

  const setText = (el, value) => {
    if (!el) return;
    el.value = value == null ? "" : String(value);
  };

  const setNumber = (el, value) => {
    if (!el) return;
    if (value == null || value === "") {
      el.value = "";
      return;
    }
    const n = Number(value);
    el.value = Number.isFinite(n) ? String(n) : "";
  };

  const setSelect = (el, value) => {
    if (!el || !value) return;
    const optionExists = Array.from(el.options || []).some((opt) => opt.value === value);
    if (!optionExists) {
      const opt = new Option(value, value, true, true);
      el.add(opt);
    }
    el.value = value;
  };

  const syncTarjaReceitaRule = () => {
    const tarja = document.getElementById("tarjaMedicacao");
    const receita = document.getElementById("exigeReceita");
    if (tarja) {
      tarja.value = "";
      tarja.disabled = true;
    }
    if (receita) {
      receita.checked = false;
      receita.disabled = true;
    }
  };

  const applySuggestion = (item) => {
    const origem = String(item?.origem || "").toUpperCase();
    const fromPhysicalStock = origem === "ESTOQUE_FISICO" || origem === "CATALOGO_PENDENTE";
    const fromNationalCatalog = origem === "CATALOGO_NACIONAL";
    const prefillOnlySource = fromPhysicalStock || fromNationalCatalog;

    const formHasData = [
      fields.nome,
      fields.descricao,
      fields.precoVenda,
      fields.precoPromocional,
      fields.codigoBarras
    ].some(hasValue);

    if (formHasData) {
      const ok = window.confirm(
        "Deseja substituir os campos atuais pelos dados do produto selecionado?"
      );
      if (!ok) return;
    }

    setText(fields.legacyId, item.legacyId);
    setText(fields.nome, item.nome);
    setText(fields.descricao, item.descricao);
    setNumber(fields.precoVenda, prefillOnlySource ? null : item.precoVenda);
    setNumber(fields.precoPromocional, prefillOnlySource ? null : item.precoPromocional);
    setNumber(fields.estoque, item.estoque);
    if (!fromNationalCatalog) {
      setSelect(fields.categoria, item.categoria);
    }
    setText(fields.codigoBarras, item.codigoBarras);
    setText(
      fields.metodoLeituraCodigoBarras,
      fromNationalCatalog ? "LEGADO" : (fromPhysicalStock ? "CSV_ESTOQUE" : "MANUAL")
    );
    setText(fields.fabricante, item.fabricante);
    setText(fields.unidade, item.unidade);
    if (fields.disponivel && item.estoque != null) {
      fields.disponivel.checked = Number(item.estoque) > 0;
    }

    if (fromPhysicalStock) {
      setHints("Dados base do estoque fisico aplicados. Preencha preco/tamanho/dosagem/ml e envie a foto.");
      fields.precoVenda?.focus();
      return;
    }

    if (fromNationalCatalog) {
      setHints("Dados base da tabela nacional aplicados. Revise categoria, preco, estoque e foto antes de salvar.");
      fields.precoVenda?.focus();
      return;
    }

    setHints("Formulario preenchido. Revise os dados e clique em Salvar.");
    fields.precoVenda?.focus();
  };

  const renderCards = (items, offset = 0) => {
    return items.map((item, idx) => {
      const pointer = offset + idx;
      const origem = String(item.origem || "CATALOGO").toUpperCase();
      const origemLabel = origem === "ESTOQUE_FISICO"
        ? "Estoque fisico"
        : (origem === "CATALOGO_PENDENTE"
          ? "Catalogo pendente"
          : (origem === "CATALOGO_NACIONAL"
            ? "Tabela nacional"
            : (origem === "CATALOGO_LOCAL" ? "Catalogo local" : "Catalogo")));
      const titulo = escapeHtml(item.nome || "Produto sem nome");
      const descricao = escapeHtml(item.descricao || "Sem descricao");
      const categoria = escapeHtml(item.categoria || "Sem categoria");
      const codigo = escapeHtml(item.codigoBarras || "-");
      const estoque = item.estoque == null ? "-" : escapeHtml(item.estoque);
      const preco = escapeHtml(money(item.precoVenda));
      const id = item.id == null ? "" : encodeURIComponent(item.id);
      const hasEditLink = id !== "";
      const badgeClass = (origem === "ESTOQUE_FISICO" || origem === "CATALOGO_NACIONAL")
        ? "badge badge--warning"
        : "badge badge--success";
      const cadastroParams = new URLSearchParams();
      if (item.legacyId != null) cadastroParams.set("legacyId", String(item.legacyId));
      if (item.nome) cadastroParams.set("nome", String(item.nome));
      if (item.descricao) cadastroParams.set("descricao", String(item.descricao));
      if (item.categoria) cadastroParams.set("categoria", String(item.categoria));
      if (item.codigoBarras) cadastroParams.set("codigoBarras", String(item.codigoBarras));
      if (item.estoque != null) cadastroParams.set("estoque", String(item.estoque));
      if (item.fabricante) cadastroParams.set("fabricante", String(item.fabricante));
      if (item.unidade) cadastroParams.set("unidade", String(item.unidade));
      if (item.origem) cadastroParams.set("origem", String(item.origem));
      const cadastroHref = `/admin/produtos/novo?${cadastroParams.toString()}`;
      const editAction = hasEditLink
        ? `<a class="btn btn-ghost" href="/admin/produtos/${id}/editar">Abrir edicao</a>`
        : `<a class="btn btn-ghost" href="${cadastroHref}">Cadastrar + imagem</a>`;

      return `
        <article class="card p-3">
          <div class="between">
            <strong>${titulo}</strong>
            <small class="${badgeClass}">${origemLabel}</small>
          </div>
          <p class="text-muted m-0">${descricao}</p>
          <small class="text-muted">Categoria: ${categoria} | EAN: ${codigo} | Estoque: ${estoque} | Preco: ${preco}</small>
          <div class="form-actions mt-2">
            <button type="button" class="btn btn-primary" data-apply-index="${pointer}">Usar dados</button>
            ${editAction}
          </div>
        </article>
      `;
    }).join("");
  };

  const bindApplyButtons = (container, source) => {
    container.querySelectorAll("[data-apply-index]").forEach((button) => {
      button.addEventListener("click", () => {
        const raw = button.getAttribute("data-apply-index");
        const idx = Number(raw);
        if (!Number.isInteger(idx) || idx < 0) return;
        const selected = source[idx];
        if (selected) applySuggestion(selected);
      });
    });
  };

  const renderResults = (items) => {
    clearResults();

    if (!Array.isArray(items) || items.length === 0) {
      setHint("Nenhum produto encontrado. Continue com cadastro manual.");
      return;
    }

    setHint(`Encontrados ${items.length} produto(s).`);
    results.innerHTML = renderCards(items, 0);
    bindApplyButtons(results, items);
  };

  const renderPendingResults = () => {
    if (!pendingResults) return;

    pendingResults.innerHTML = "";
    if (!pendingItems.length) {
      pendingResults.innerHTML = `
        <article class="card p-3">
          <p class="text-muted m-0">Nenhum produto pendente encontrado para esse filtro.</p>
        </article>
      `;
      if (pendingLoadMoreBtn) pendingLoadMoreBtn.style.display = "none";
      return;
    }

    pendingResults.innerHTML = renderCards(pendingItems, 0);
    bindApplyButtons(pendingResults, pendingItems);
    if (pendingLoadMoreBtn) {
      pendingLoadMoreBtn.style.display = pendingHasNext ? "" : "none";
    }
  };

  const search = async (query) => {
    const currentRequest = ++requestId;

    if (query.length < 2) {
      clearResults();
      setHint("Digite pelo menos 2 caracteres para pesquisar.");
      return;
    }

    setHint("Buscando produtos...");

    try {
      const resp = await fetch(
        `/admin/produtos/busca-rapida?q=${encodeURIComponent(query)}&limit=8`,
        {
          method: "GET",
          headers: { Accept: "application/json" },
          credentials: "same-origin"
        }
      );

      if (currentRequest !== requestId) return;

      if (!resp.ok) {
        clearResults();
        setHint("Nao foi possivel pesquisar agora. Tente novamente.");
        return;
      }

      const items = await resp.json();
      if (currentRequest !== requestId) return;
      renderResults(items);
    } catch (_error) {
      if (currentRequest !== requestId) return;
      clearResults();
      setHint("Falha de rede ao pesquisar produtos.");
    }
  };

  const carregarPendentes = async (reset) => {
    if (!pendingResults || pendingLoading) return;

    const nextPage = reset ? 0 : pendingPage;
    if (!reset && !pendingHasNext) return;

    pendingLoading = true;
    setPendingHint(reset ? "Carregando pendentes..." : "Carregando mais pendentes...");
    if (pendingLoadMoreBtn) pendingLoadMoreBtn.disabled = true;

    try {
      const resp = await fetch(
        `/admin/produtos/nao-prontos?q=${encodeURIComponent(pendingQuery)}&page=${nextPage}&size=12`,
        {
          method: "GET",
          headers: { Accept: "application/json" },
          credentials: "same-origin"
        }
      );

      if (!resp.ok) {
        setPendingHint("Nao foi possivel carregar pendentes agora.");
        return;
      }

      const payload = await resp.json();
      const items = Array.isArray(payload?.items) ? payload.items : [];
      const total = Number(payload?.total) || 0;

      if (reset) {
        pendingItems = items;
      } else {
        pendingItems = pendingItems.concat(items);
      }

      pendingHasNext = Boolean(payload?.hasNext);
      pendingPage = nextPage + 1;
      renderPendingResults();
      setPendingHint(`Mostrando ${pendingItems.length} de ${total} pendente(s).`);
    } catch (_error) {
      setPendingHint("Falha de rede ao carregar pendentes.");
    } finally {
      pendingLoading = false;
      if (pendingLoadMoreBtn) pendingLoadMoreBtn.disabled = false;
    }
  };

  input.addEventListener("input", () => {
    clearTimeout(debounceId);
    const query = String(input.value || "").trim();
    debounceId = setTimeout(() => search(query), 260);
  });

  input.addEventListener("keydown", (event) => {
    if (event.key !== "Enter") return;
    event.preventDefault();
    clearTimeout(debounceId);
    search(String(input.value || "").trim());
  });

  clearBtn?.addEventListener("click", () => {
    input.value = "";
    clearTimeout(debounceId);
    clearResults();
    setHint("Digite pelo menos 2 caracteres para pesquisar.");
    input.focus();
  });

  pendingBuscarBtn?.addEventListener("click", () => {
    pendingQuery = String(pendingInput?.value || "").trim();
    pendingPage = 0;
    pendingHasNext = false;
    pendingItems = [];
    carregarPendentes(true);
  });

  pendingInput?.addEventListener("keydown", (event) => {
    if (event.key !== "Enter") return;
    event.preventDefault();
    pendingQuery = String(pendingInput?.value || "").trim();
    pendingPage = 0;
    pendingHasNext = false;
    pendingItems = [];
    carregarPendentes(true);
  });

  pendingLoadMoreBtn?.addEventListener("click", () => {
    carregarPendentes(false);
  });

  fields.categoria?.addEventListener("change", syncTarjaReceitaRule);
  syncTarjaReceitaRule();

  if (pendingResults) {
    carregarPendentes(true);
  }

  imageInputs.forEach((inputNode) => {
    inputNode.addEventListener("change", () => syncImageInputs(inputNode));
  });
})();

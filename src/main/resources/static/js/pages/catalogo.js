(() => {
  const isAdmin = !!(window.CATALOGO_CTX && window.CATALOGO_CTX.isAdmin);
  const grid = document.getElementById("catalogo-grid");
  const formBusca = document.getElementById("form-busca");
  const inputQ = document.getElementById("q");
  const btnLimpar = document.getElementById("btn-limpar");
  const btnPrev = document.getElementById("btn-prev");
  const btnNext = document.getElementById("btn-next");
  const pagInfo = document.getElementById("paginacao-info");

  // imagem padrão quando o produto não tem foto
  const PLACEHOLDER_IMG = "/img/produtos/placeholder-generico.png";

  // paginação simples (server-side)
  let page = 0;
  let size = 24;
  let hasNext = false;
  const parseEntityId = (rawValue) => {
    const value = String(rawValue ?? "").trim();
    return /^\d+$/.test(value) ? value : null;
  };
  const resolveProdutoId = (p) => parseEntityId(p?.entityId ?? p?.id ?? null);
  const resolveProdutoKey = (p) => {
    const id = resolveProdutoId(p);
    return id == null ? "" : String(id);
  };

  document.addEventListener("DOMContentLoaded", () => {
    carregar();
    wireBusca();
    wirePaginacao();
    wireModal();
  });

  function wireBusca() {
    formBusca?.addEventListener("submit", (e) => {
      e.preventDefault();
      page = 0;
      carregar();
    });
    btnLimpar?.addEventListener("click", () => {
      inputQ.value = "";
      page = 0;
      carregar();
    });
  }

  function wirePaginacao() {
    btnPrev?.addEventListener("click", () => {
      if (page > 0) {
        page--;
        carregar();
      }
    });
    btnNext?.addEventListener("click", () => {
      if (hasNext) {
        page++;
        carregar();
      }
    });
  }

  async function carregar() {
    if (!grid) return;
    grid.innerHTML = `<div class="card p-4">Carregando…</div>`;
    const q = inputQ?.value?.trim() || "";

    const endpoint = isAdmin ? "/api/admin/produtos" : "/api/public/produtos";
    const url = new URL(endpoint, window.location.origin);
    url.searchParams.set("page", page);
    url.searchParams.set("size", size);
    if (q) url.searchParams.set("q", q);
    if (!isAdmin) {
      url.searchParams.set("sort", "nome");
      url.searchParams.set("dir", "asc");
    }

    try {
      const resp = await fetch(url, { headers: { "Accept": "application/json" } });
      if (!resp.ok) {
        grid.innerHTML = `<div class="card p-4">Erro ao carregar (${resp.status}).</div>`;
        return;
      }

      const data = await resp.json();
      const content = Array.isArray(data) ? data : (data.content || []);
      hasNext = Array.isArray(data) ? false : !data.last;
      atualizarPaginacao();

      if (!content.length) {
        grid.innerHTML = `<div class="card p-4">Nenhum produto encontrado.</div>`;
        return;
      }

      grid.innerHTML = content.map(p => renderCard(p)).join("");
      bindCardActions(content);
    } catch (err) {
      grid.innerHTML = `<div class="card p-4">Falha de rede ao consultar catálogo.</div>`;
    }
  }

  function atualizarPaginacao() {
    if (!btnPrev || !btnNext || !pagInfo) return;
    btnPrev.disabled = page <= 0;
    btnNext.disabled = !hasNext;
    pagInfo.textContent = `Página ${page + 1}`;
  }

  function renderCard(p) {
    const produtoId = resolveProdutoKey(p);

    // ProdutoResponseDTO -> preco / estoqueAtual / situacao
    const preco = (p.preco != null) ? Number(p.preco) :
                  (p.precoVenda != null ? Number(p.precoVenda) : 0);
    const precoFmt = preco.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

    const estoqueNum = Number.isFinite(p.estoqueAtual) ? p.estoqueAtual :
                       (Number.isFinite(p.estoque) ? p.estoque : undefined);
    const estoque = Number.isFinite(estoqueNum)
      ? `<small class="text-muted">Estoque: ${estoqueNum}</small>`
      : "";

    const disponivelCalc =
      (typeof p.disponivel === "boolean" ? p.disponivel :
        (p.situacao ? String(p.situacao).toUpperCase() === "ATIVO" : (preco > 0 && (estoqueNum ?? 0) > 0)));

    const disponibilidade = disponivelCalc
      ? ""
      : `<span class="badge danger ml-2">Indisponível</span>`;

    const acoesCliente = `
      <div class="form-row gap-2 mt-2">
        <button class="btn" data-add-carrinho data-id="${escapeHtml(produtoId)}" ${disponivelCalc && produtoId ? "" : "disabled"}>
          Adicionar ao carrinho
        </button>
      </div>`;

    // ações admin: editar, trocar foto (modal), gerar IA, regenerar IA
    const acoesAdmin = `
      <div class="form-row gap-2 mt-2">
        <button class="btn" data-editar data-id="${escapeHtml(produtoId)}">Editar</button>
        <button class="btn btn-ghost" data-trocar-foto data-id="${escapeHtml(produtoId)}">Trocar foto</button>
        <button class="btn btn-primary" data-ia-queue data-id="${escapeHtml(produtoId)}">Gerar IA</button>
        <button class="btn btn-ghost" data-ia-regenerate data-id="${escapeHtml(produtoId)}">Regenerar</button>
      </div>`;

    const nome = p.nome || "Produto sem nome";
    const desc = p.descricao || "";

    const hasImagem = p.imagem && String(p.imagem).trim() !== "";
    const imageUrl = hasImagem ? p.imagem : PLACEHOLDER_IMG;

    const imagemHtml = `
      <div class="card-img-top-wrapper" style="position:relative;">
        <img
          src="${escapeHtml(imageUrl)}"
          alt="${escapeHtml(nome)}"
          class="card-img-top"
          style="height:180px;object-fit:cover;border-radius: var(--radius-lg);width:100%;"
          loading="lazy"
          onerror="this.onerror=null;this.src='${PLACEHOLDER_IMG}';"
        />
        ${
          !hasImagem && isAdmin
            ? `<span class="badge badge-soft" style="position:absolute;left:.5rem;top:.5rem;">
                 Sem foto cadastrada
               </span>`
            : ""
        }
      </div>`;

    return `
      <article class="card p-3 product-card" data-card data-id="${escapeHtml(produtoId)}">
        ${imagemHtml}
        <div class="stack mt-2">
          <h3 class="h5 m-0 line-clamp-2">
            ${escapeHtml(nome)}${disponibilidade}
          </h3>
          <p class="text-muted m-0 line-clamp-2">${escapeHtml(desc)}</p>
          <div class="between mt-1">
            <strong>${precoFmt}</strong>
            ${estoque}
          </div>
          ${isAdmin ? acoesAdmin : acoesCliente}
        </div>
      </article>
    `;
  }

  function bindCardActions(produtos) {
    // Cliente: adicionar ao carrinho
    document.querySelectorAll("[data-add-carrinho]").forEach(btn => {
      btn.addEventListener("click", async () => {
        const id = btn.getAttribute("data-id");
        btn.disabled = true;
        try {
          const ok = await addToCart(id);
          toast(ok ? "Adicionado ao carrinho!" : "Não foi possível adicionar.");
        } finally {
          btn.disabled = false;
        }
      });
    });

    if (!isAdmin) return;

    // Admin: editar
    document.querySelectorAll("[data-editar]").forEach(btn => {
      btn.addEventListener("click", () => {
        const id = String(btn.getAttribute("data-id"));
        const p = produtos.find(x => resolveProdutoKey(x) === id);
        if (p) abrirModalEdicao(p);
      });
    });

    // Admin: trocar foto (abre modal com foco no campo de arquivo)
    document.querySelectorAll("[data-trocar-foto]").forEach(btn => {
      btn.addEventListener("click", () => {
        const id = String(btn.getAttribute("data-id"));
        const p = produtos.find(x => resolveProdutoKey(x) === id);
        if (p) abrirModalEdicao(p, /*focoImagem*/ true);
      });
    });

    // Admin: IA (queue / regenerate)
    document.querySelectorAll("[data-ia-queue]").forEach(btn => {
      btn.addEventListener("click", async () => {
        const id = btn.getAttribute("data-id");
        await iaRequest(id, "queue", btn);
      });
    });
    document.querySelectorAll("[data-ia-regenerate]").forEach(btn => {
      btn.addEventListener("click", async () => {
        const id = btn.getAttribute("data-id");
        await iaRequest(id, "regenerate", btn);
      });
    });
  }

  async function iaRequest(produtoId, action, btn) {
    if (!produtoId) return;
    const endpoint = action === "regenerate"
      ? `/admin/imagens/${encodeURIComponent(produtoId)}/regenerate`
      : `/admin/imagens/${encodeURIComponent(produtoId)}/queue`;

    btn.disabled = true;
    try {
      const r = await fetch(endpoint, withCsrf({ method: "POST", credentials: "same-origin" }));
      const raw = await r.text();
      const payload = parseJsonSafely(raw);

      if (!r.ok) {
        throw new Error(
          normalizeErrorMessage(
            payload?.lastJob?.errorMsg || payload?.message || raw,
            `Falha ao solicitar imagem (HTTP ${r.status}).`
          )
        );
      }

      const lastJobStatus = String(payload?.lastJob?.status || "");
      if (lastJobStatus === "ERROR") {
        throw new Error(
          normalizeErrorMessage(
            payload?.lastJob?.errorMsg,
            "A IA nao conseguiu gerar a imagem para esse produto."
          )
        );
      }

      const result = String(payload?.result || "");
      if (lastJobStatus === "DONE" || result.startsWith("PROCESSADO_SYNC")) {
        toast("Imagem gerada e salva!");
        carregar();
      } else {
        toast(action === "regenerate" ? "Regeneracao solicitada!" : "Geracao enfileirada!");
      }
    } catch (e) {
      toast(e?.message || "Falha ao solicitar imagem.", true);
    } finally {
      btn.disabled = false;
    }
  }

  async function addToCart(produtoId) {
    if (!produtoId) return false;
    try {
      const body = new URLSearchParams({
        produtoId: String(produtoId),
        quantidade: "1"
      });
      const resp = await fetch("/carrinho/adicionar", withCsrf({
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
        body: body.toString(),
        credentials: "same-origin"
      }));
      return resp.ok;
    } catch {
      return false;
    }
  }

  // ===== Modal de edição =====

  const modal = document.getElementById("modal-editar");
  const formEditar = document.getElementById("form-editar");
  const closeBtns = modal?.querySelectorAll(".modal-close, [data-close]");

  function wireModal() {
    closeBtns?.forEach(b => b.addEventListener("click", fecharModal));
    document.addEventListener("click", (e) => {
      if (e.target === modal) fecharModal();
    });

    formEditar?.addEventListener("submit", async (e) => {
      e.preventDefault();
      await salvarEdicao();
    });
  }

  function abrirModalEdicao(p, focoImagem = false) {
    if (!modal) {
      const id = resolveProdutoKey(p);
      if (isAdmin && id) {
        window.location.href = `/admin/produtos/${encodeURIComponent(id)}/editar`;
      }
      return;
    }
    modal.style.display = "flex";
    modal.setAttribute("aria-hidden", "false");

    byId("edit-id").value = resolveProdutoKey(p);
    byId("edit-nome").value = p.nome || "";
    byId("edit-desc").value = p.descricao || "";
    const preco = (p.preco != null ? Number(p.preco) :
                   (p.precoVenda != null ? Number(p.precoVenda) : 0));
    byId("edit-preco").value = preco.toFixed(2);

    const estoque = Number.isFinite(p.estoqueAtual) ? p.estoqueAtual :
                    (Number.isFinite(p.estoque) ? p.estoque : 0);
    byId("edit-estoque").value = estoque;

    const disponivel =
      (typeof p.disponivel === "boolean" ? p.disponivel :
        (p.situacao ? String(p.situacao).toUpperCase() === "ATIVO" : (preco > 0 && estoque > 0)));
    byId("edit-disponivel").value = String(!!disponivel);

    byId("edit-categoria").value = p.categoria || "";
    byId("edit-imagem-url").value = p.imagem || "";
    byId("edit-imagem-file").value = "";
    byId("edit-ean").textContent = p.codigoBarras || "—";

    if (focoImagem) {
      byId("edit-imagem-file").focus();
    } else {
      byId("edit-nome").focus();
    }
  }

  function fecharModal() {
    if (!modal) return;
    modal.style.display = "none";
    modal.setAttribute("aria-hidden", "true");
    formEditar?.reset();
  }

  async function salvarEdicao() {
    const id = byId("edit-id")?.value;
    if (!id) return;

    // ProdutoRequestDTO esperado pelo Admin REST:
    const body = {
      nome: byId("edit-nome").value.trim(),
      descricao: byId("edit-desc").value.trim(),
      preco: parseFloat(byId("edit-preco").value.replace(",", ".")),
      estoque: parseInt(byId("edit-estoque").value, 10) || 0,
      ativo: byId("edit-disponivel").value === "true",
      categoria: byId("edit-categoria").value.trim(),
      imagem: byId("edit-imagem-url").value.trim() || null
    };

    // 1) Atualiza dados principais
    const resp = await fetch(`/api/admin/produtos/${encodeURIComponent(id)}`, withCsrf({
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
      credentials: "same-origin"
    }));

    if (!resp.ok) {
      try {
        const err = await resp.text();
        toast(err || "Erro ao salvar dados.", true);
      } catch {
        toast("Erro ao salvar dados.", true);
      }
      return;
    }

    // 2) Upload de imagem, se houver
    const file = byId("edit-imagem-file").files?.[0];
    if (file) {
      const fd = new FormData();
      fd.append("file", file);
      const up = await fetch(`/api/admin/produtos/${encodeURIComponent(id)}/imagem`, withCsrf({
        method: "POST",
        body: fd,
        credentials: "same-origin"
      }));
      if (!up.ok) {
        try {
          const err = await up.text();
          toast(err || "Dados salvos, mas falhou ao enviar a imagem.", true);
        } catch {
          toast("Dados salvos, mas falhou ao enviar a imagem.", true);
        }
        fecharModal();
        carregar();
        return;
      }
    }

    toast("Produto atualizado!");
    fecharModal();
    carregar();
  }

  // ===== CSRF + Utils =====

  function getCsrf() {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    return { token, header };
  }
  function withCsrf(init = {}) {
    const { token, header } = getCsrf();
    init.headers = Object.assign({}, init.headers || {}, token ? { [header]: token } : {});
    return init;
  }

  function byId(id) { return document.getElementById(id); }

  function toast(msg, err) {
    const el = document.createElement("div");
    el.className = `toast ${err ? "danger" : "success"}`;
    el.textContent = msg;
    Object.assign(el.style, {
      position: "fixed", right: "1rem", bottom: "1rem",
      padding: "0.75rem 1rem", background: err ? "#ef4444" : "#10b981",
      color: "#fff", borderRadius: "8px", zIndex: 9999, boxShadow: "0 8px 30px rgba(0,0,0,.2)"
    });
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 2500);
  }

  function normalizeErrorMessage(raw, fallback) {
    const text = String(raw || "")
      .replace(/<[^>]*>/g, " ")
      .replace(/\s+/g, " ")
      .trim();
    return text || fallback;
  }

  function parseJsonSafely(raw) {
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }

  function escapeHtml(s) {
    return String(s ?? "").replace(/[&<>"']/g, m => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;"
    }[m]));
  }
})();

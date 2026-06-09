(() => {
  const $ = (id) => document.getElementById(id);

  const termInput = $("pdv-term");
  const qtyInput = $("pdv-qty");
  const resultsEl = $("pdv-results");
  const cartEl = $("pdv-cart");
  const emptyEl = $("pdv-empty");
  const totalEl = $("pdv-total");
  const totalItemsEl = $("pdv-total-items");
  const statusEl = $("pdv-status");
  const scanStatusEl = $("pdv-scan-status");
  const cpfInput = $("pdv-cliente-cpf");
  const nomeInput = $("pdv-cliente-nome");
  const emailInput = $("pdv-cliente-email");
  const notaEmailRadio = document.querySelector("input[name='nota'][value='EMAIL']");
  const finalizarBtn = $("pdv-finalizar");

  const cashDinheiroEl = $("pdv-caixa-dinheiro");
  const cashPixEl = $("pdv-caixa-pix");
  const cashCartaoEl = $("pdv-caixa-cartao");
  const cashTotalVendidoEl = $("pdv-caixa-total-vendido");
  const cashEsperadoEl = $("pdv-caixa-esperado");
  const cashDiferencaEl = $("pdv-caixa-diferenca");
  const cashPeriodoEl = $("pdv-caixa-periodo");
  const caixaRefreshBtn = $("pdv-caixa-refresh");

  const caixaAberturaInput = $("pdv-caixa-abertura");
  const caixaSuprimentoInput = $("pdv-caixa-suprimento");
  const caixaSangriaInput = $("pdv-caixa-sangria");
  const caixaContadoInput = $("pdv-caixa-contado");
  const caixaInputs = [
    caixaAberturaInput,
    caixaSuprimentoInput,
    caixaSangriaInput,
    caixaContadoInput,
  ].filter(Boolean);

  const cart = new Map();
  let criarCliente = false;
  let caixaResumo = {
    dia: null,
    pedidosConsiderados: 0,
    totalVendas: 0,
    totalDinheiro: 0,
    totalPix: 0,
    totalCartao: 0,
  };

  function toNumber(value) {
    const source = String(value ?? "").trim().replace(",", ".");
    const parsed = Number(source);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  function formatMoney(value) {
    const num = Number(value || 0);
    return num.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
  }

  function getCsrfToken() {
    const meta = document.querySelector("meta[name='_csrf']");
    return meta ? meta.getAttribute("content") : "";
  }

  function setStatus(message, isError = false) {
    statusEl.textContent = message || "";
    statusEl.style.color = isError ? "#b91c1c" : "var(--layout-muted)";
  }

  function setScanStatus(message, isError = false) {
    if (!scanStatusEl) return;
    scanStatusEl.textContent = message || "";
    scanStatusEl.style.color = isError ? "#b91c1c" : "var(--layout-muted)";
  }

  function normalizeBarcode(value) {
    if (!value) return "";
    let cleaned = value.trim();
    if (cleaned.startsWith("]C1") || cleaned.startsWith("]C2") || cleaned.startsWith("]C0")) {
      cleaned = cleaned.slice(3);
    }
    return cleaned.replace(/\D+/g, "");
  }

  function toggleEmailOption() {
    const hasEmail = !!emailInput.value.trim();
    if (!notaEmailRadio) return;

    notaEmailRadio.disabled = !hasEmail;
    if (!hasEmail && notaEmailRadio.checked) {
      document.querySelector("input[name='nota'][value='IMPRESSAO']").checked = true;
    }
  }

  function renderCart() {
    cartEl.innerHTML = "";
    let total = 0;
    let totalItems = 0;

    cart.forEach((item) => {
      const subtotal = item.preco * item.quantidade;
      total += subtotal;
      totalItems += item.quantidade;

      const row = document.createElement("tr");
      row.innerHTML = `
        <td>
          <div>${item.nome}</div>
          <small>${item.codigoBarras || ""}</small>
        </td>
        <td>
          <input class="input pdv-cart-qty" type="number" min="1" value="${item.quantidade}" data-id="${item.id}"/>
        </td>
        <td>${formatMoney(item.preco)}</td>
        <td>${formatMoney(subtotal)}</td>
        <td><button class="btn btn--ghost" data-remove="${item.id}">Remover</button></td>
      `;
      cartEl.appendChild(row);
    });

    emptyEl.style.display = cart.size ? "none" : "block";
    totalEl.textContent = formatMoney(total);
    totalItemsEl.textContent = totalItems.toString();
  }

  function addToCart(produto, quantidade) {
    const qty = Number(quantidade) || 1;
    const existing = cart.get(produto.id);

    if (existing) {
      existing.quantidade += qty;
    } else {
      cart.set(produto.id, {
        id: produto.id,
        nome: produto.nome,
        codigoBarras: produto.codigoBarras,
        preco: Number(produto.preco || 0),
        quantidade: qty,
      });
    }

    renderCart();
  }

  async function buscarProdutos(termo) {
    const resp = await fetch(`/admin/vendas/rapida/produtos?q=${encodeURIComponent(termo)}`);
    if (!resp.ok) {
      throw new Error("Falha ao buscar produtos.");
    }
    return resp.json();
  }

  function renderResultados(lista) {
    resultsEl.innerHTML = "";

    if (!lista || !lista.length) {
      resultsEl.innerHTML = "<div class='muted'>Nenhum produto encontrado.</div>";
      return;
    }

    lista.forEach((p) => {
      const div = document.createElement("div");
      div.className = "pdv-result";
      div.innerHTML = `
        <div>
          <strong>${p.nome}</strong>
          <small>${p.codigoBarras || "Sem codigo"}</small>
        </div>
        <div>${formatMoney(p.preco)}</div>
        <button class="btn btn--sm" data-add="${p.id}">Adicionar</button>
      `;
      div.querySelector("[data-add]").addEventListener("click", () => {
        addToCart(p, qtyInput.value);
        termInput.value = "";
        resultsEl.innerHTML = "";
        termInput.focus();
      });
      resultsEl.appendChild(div);
    });
  }

  function todayIsoDate() {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, "0");
    const d = String(now.getDate()).padStart(2, "0");
    return `${y}-${m}-${d}`;
  }

  function dateIsoToBr(isoDate) {
    if (!isoDate || !isoDate.includes("-")) return "hoje";
    const [y, m, d] = isoDate.split("-");
    return `${d}/${m}/${y}`;
  }

  function caixaStorageKey(dia) {
    return `pdv-caixa-ajustes:${dia}`;
  }

  function readCaixaAjustes() {
    return {
      abertura: toNumber(caixaAberturaInput?.value),
      suprimento: toNumber(caixaSuprimentoInput?.value),
      sangria: toNumber(caixaSangriaInput?.value),
      contado: toNumber(caixaContadoInput?.value),
    };
  }

  function setInputMoneyValue(input, value) {
    if (!input) return;
    const num = toNumber(value);
    input.value = num > 0 ? String(num) : "";
  }

  function loadCaixaAjustes() {
    if (!caixaResumo.dia) return;
    const key = caixaStorageKey(caixaResumo.dia);

    try {
      const raw = localStorage.getItem(key);
      if (!raw) {
        setInputMoneyValue(caixaAberturaInput, 0);
        setInputMoneyValue(caixaSuprimentoInput, 0);
        setInputMoneyValue(caixaSangriaInput, 0);
        setInputMoneyValue(caixaContadoInput, 0);
        return;
      }
      const parsed = JSON.parse(raw);
      setInputMoneyValue(caixaAberturaInput, parsed.abertura);
      setInputMoneyValue(caixaSuprimentoInput, parsed.suprimento);
      setInputMoneyValue(caixaSangriaInput, parsed.sangria);
      setInputMoneyValue(caixaContadoInput, parsed.contado);
    } catch (err) {
      console.warn("Falha ao ler ajustes do caixa", err);
    }
  }

  function saveCaixaAjustes() {
    if (!caixaResumo.dia) return;
    const key = caixaStorageKey(caixaResumo.dia);
    const payload = readCaixaAjustes();

    try {
      localStorage.setItem(key, JSON.stringify(payload));
    } catch (err) {
      console.warn("Falha ao salvar ajustes do caixa", err);
    }
  }

  function paintDiferenca(diferenca) {
    if (!cashDiferencaEl) return;
    cashDiferencaEl.classList.remove("is-positive", "is-negative", "is-neutral");

    if (diferenca > 0.009) {
      cashDiferencaEl.classList.add("is-positive");
      return;
    }
    if (diferenca < -0.009) {
      cashDiferencaEl.classList.add("is-negative");
      return;
    }
    cashDiferencaEl.classList.add("is-neutral");
  }

  function recalcularConferenciaCaixa() {
    if (!cashEsperadoEl || !cashDiferencaEl) return;

    const ajustes = readCaixaAjustes();
    const esperado = ajustes.abertura + toNumber(caixaResumo.totalDinheiro) + ajustes.suprimento - ajustes.sangria;
    const diferenca = ajustes.contado - esperado;

    cashEsperadoEl.textContent = formatMoney(esperado);
    cashDiferencaEl.textContent = formatMoney(diferenca);
    paintDiferenca(diferenca);
  }

  function renderResumoCaixa() {
    if (!cashDinheiroEl || !cashPixEl || !cashCartaoEl || !cashTotalVendidoEl) {
      return;
    }

    cashDinheiroEl.textContent = formatMoney(caixaResumo.totalDinheiro);
    cashPixEl.textContent = formatMoney(caixaResumo.totalPix);
    cashCartaoEl.textContent = formatMoney(caixaResumo.totalCartao);
    cashTotalVendidoEl.textContent = formatMoney(caixaResumo.totalVendas);

    if (cashPeriodoEl) {
      const ref = dateIsoToBr(caixaResumo.dia);
      cashPeriodoEl.textContent = `Resumo de ${ref}. Pedidos considerados: ${caixaResumo.pedidosConsiderados || 0}.`;
    }
  }

  async function carregarResumoCaixa() {
    if (!cashDinheiroEl) return;

    const dia = todayIsoDate();
    try {
      const resp = await fetch(`/admin/vendas/caixa/resumo?dia=${encodeURIComponent(dia)}`);
      if (!resp.ok) {
        throw new Error("Falha ao carregar resumo do caixa.");
      }

      const data = await resp.json();
      caixaResumo = {
        dia: data.dia || dia,
        pedidosConsiderados: Number(data.pedidosConsiderados || 0),
        totalVendas: toNumber(data.totalVendas),
        totalDinheiro: toNumber(data.totalDinheiro),
        totalPix: toNumber(data.totalPix),
        totalCartao: toNumber(data.totalCartao),
      };

      renderResumoCaixa();
      loadCaixaAjustes();
      recalcularConferenciaCaixa();
    } catch (err) {
      setStatus(err.message || "Nao foi possivel atualizar o caixa.", true);
    }
  }

  document.getElementById("pdv-search").addEventListener("submit", async (ev) => {
    ev.preventDefault();

    const raw = termInput.value.trim();
    if (!raw) return;

    const termo = normalizeBarcode(raw) || raw;
    if (termo !== raw) {
      termInput.value = termo;
      setScanStatus(`Leitura normalizada: ${termo}`);
    } else {
      setScanStatus("Leitor pronto. Foque o campo e escaneie o codigo.");
    }

    setStatus("");
    try {
      const lista = await buscarProdutos(termo);
      if (lista.length === 1) {
        addToCart(lista[0], qtyInput.value);
        termInput.value = "";
        resultsEl.innerHTML = "";
      } else {
        renderResultados(lista);
      }
    } catch (err) {
      setStatus(err.message, true);
    }
  });

  async function buscarClientePorCpf(cpf) {
    const resp = await fetch(`/admin/vendas/rapida/cliente?cpf=${encodeURIComponent(cpf)}`);
    if (!resp.ok) {
      throw new Error("Falha ao buscar cliente.");
    }
    return resp.json();
  }

  cpfInput?.addEventListener("blur", async () => {
    const cpf = cpfInput.value.trim();
    if (!cpf) {
      criarCliente = false;
      return;
    }

    try {
      const data = await buscarClientePorCpf(cpf);
      if (data.existe) {
        criarCliente = false;
        if (data.nome && !nomeInput.value.trim()) nomeInput.value = data.nome;
        if (data.email && !emailInput.value.trim()) emailInput.value = data.email;
        setStatus("Cliente encontrado. Dados carregados.");
      } else {
        const aceita = window.confirm("CPF nao encontrado. Deseja criar cadastro?");
        criarCliente = aceita;
        if (!aceita) {
          setStatus("Sem cadastro. Informe e-mail se quiser envio por e-mail.");
        } else {
          setStatus("Cadastro sera criado ao finalizar.");
        }
      }
      toggleEmailOption();
    } catch (err) {
      setStatus(err.message, true);
    }
  });

  emailInput?.addEventListener("input", toggleEmailOption);

  cartEl.addEventListener("input", (ev) => {
    const target = ev.target;
    if (!target.matches("[data-id]")) return;

    const id = Number(target.getAttribute("data-id"));
    const item = cart.get(id);
    if (!item) return;

    const qty = Math.max(1, Number(target.value || 1));
    item.quantidade = qty;
    renderCart();
  });

  document.addEventListener("click", (ev) => {
    const target = ev.target;
    if (!cartEl || !(target instanceof Element)) return;

    const removeBtn = target.closest("[data-remove]");
    if (!removeBtn || !cartEl.contains(removeBtn)) return;

    const id = removeBtn.getAttribute("data-remove");
    if (!id) return;

    cart.delete(Number(id));
    renderCart();
  });

  function getPagamento() {
    const radio = document.querySelector("input[name='pagamento']:checked");
    return radio ? radio.value : "PIX";
  }

  function getNota() {
    const radio = document.querySelector("input[name='nota']:checked");
    return radio ? radio.value : "IMPRESSAO";
  }

  finalizarBtn.addEventListener("click", async () => {
    setStatus("");

    if (!cart.size) {
      setStatus("Adicione ao menos um item.", true);
      return;
    }

    const pagamentoTipo = getPagamento();
    const notaOpcao = getNota();
    const trocoPara = Number($("pdv-troco").value || 0);
    const total = Array.from(cart.values()).reduce((acc, item) => acc + item.preco * item.quantidade, 0);

    if (pagamentoTipo === "DINHEIRO" && trocoPara > 0 && trocoPara < total) {
      setStatus("Troco para deve ser maior que o total.", true);
      return;
    }

    const email = $("pdv-cliente-email").value.trim();
    if (notaOpcao === "EMAIL" && !email) {
      setStatus("Informe o e-mail para envio da nota.", true);
      return;
    }

    const payload = {
      itens: Array.from(cart.values()).map((item) => ({
        produtoId: item.id,
        quantidade: item.quantidade,
      })),
      clienteCpf: cpfInput.value.trim(),
      clienteNome: nomeInput.value.trim(),
      clienteEmail: email,
      criarCliente,
      pagamentoTipo,
      trocoPara: pagamentoTipo === "DINHEIRO" ? trocoPara : null,
      notaOpcao,
    };

    try {
      const resp = await fetch("/admin/vendas/rapida/finalizar", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-TOKEN": getCsrfToken(),
        },
        body: JSON.stringify(payload),
      });
      const data = await resp.json();
      if (!resp.ok || !data.ok) {
        throw new Error(data.message || "Falha ao finalizar venda.");
      }

      setStatus(data.message || "Venda finalizada.");
      cart.clear();
      renderCart();
      await carregarResumoCaixa();

      if (data.reciboUrl && notaOpcao === "IMPRESSAO") {
        window.open(data.reciboUrl, "_blank", "width=480,height=640");
      }
    } catch (err) {
      setStatus(err.message, true);
    }
  });

  if (caixaRefreshBtn) {
    caixaRefreshBtn.addEventListener("click", async () => {
      await carregarResumoCaixa();
    });
  }

  caixaInputs.forEach((input) => {
    input.addEventListener("input", () => {
      saveCaixaAjustes();
      recalcularConferenciaCaixa();
    });
  });

  renderCart();
  toggleEmailOption();
  carregarResumoCaixa();
})();

(() => {
  const organizeButton = document.getElementById("entregas-organizar-rota");
  const createButton = document.getElementById("entregas-criar-rota");
  const feedbackEl = document.getElementById("entregas-feedback");
  const originEl = document.getElementById("entregas-origem");
  const useGpsButton = document.getElementById("entregas-usar-gps");
  const originStatusEl = document.getElementById("entregas-origin-status");
  const toggleAllEl = document.getElementById("entregas-select-all");
  const countEl = document.getElementById("entregas-selected-count");
  const previewEl = document.getElementById("entregas-preview");
  const previewOriginEl = document.getElementById("entregas-preview-origin");
  const previewDistanceEl = document.getElementById("entregas-preview-distance");
  const previewCountEl = document.getElementById("entregas-preview-count");
  const previewListEl = document.getElementById("entregas-preview-list");
  const previewMapEl = document.getElementById("entregas-preview-map");
  const rowCheckboxSelector = 'input[name="pedidoIds"]';
  const gpsOriginLabel = "Localizacao atual do dispositivo";
  let previewSignature = null;
  let currentOriginCoordinates = null;

  if (!createButton || !organizeButton) {
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
    const headers = Object.assign({}, init.headers || {});
    if (token) {
      headers[header] = token;
    }
    headers.Accept = headers.Accept || "application/json";
    headers["X-Requested-With"] = "XMLHttpRequest";
    return Object.assign({}, init, {
      credentials: "same-origin",
      headers,
    });
  }

  function selectedIds() {
    return Array.from(document.querySelectorAll(`${rowCheckboxSelector}:checked`))
      .filter((input) => !input.disabled)
      .map((input) => Number.parseInt(input.value, 10))
      .filter((id) => Number.isFinite(id));
  }

  function eligibleIdsOnPage() {
    return Array.from(document.querySelectorAll(rowCheckboxSelector))
      .filter((input) => !input.disabled)
      .map((input) => Number.parseInt(input.value, 10))
      .filter((id) => Number.isFinite(id));
  }

  function selectIds(ids) {
    const idSet = new Set(ids || []);
    document.querySelectorAll(rowCheckboxSelector).forEach((input) => {
      if (input.disabled) {
        return;
      }
      const id = Number.parseInt(input.value, 10);
      input.checked = idSet.has(id);
    });
    syncSelectedState();
  }

  function resolveRoutePedidoIds() {
    const manualSelection = selectedIds();
    if (manualSelection.length >= 3) {
      return manualSelection;
    }
    const eligible = eligibleIdsOnPage();
    if (eligible.length >= 3) {
      selectIds(eligible);
      return eligible;
    }
    return manualSelection;
  }

  function currentOrigin() {
    return String(originEl?.value || "").trim();
  }

  function currentSignature() {
    return JSON.stringify({
      pedidoIds: selectedIds(),
      origem: currentOrigin(),
      origemLatitude: currentOriginCoordinates?.latitude || null,
      origemLongitude: currentOriginCoordinates?.longitude || null,
    });
  }

  function buildRouteRequestBody(pedidoIds) {
    const params = new URLSearchParams();
    (pedidoIds || []).forEach((pedidoId) => {
      params.append("pedidoIds", String(pedidoId));
    });
    const origin = currentOrigin();
    if (origin) {
      params.append("origem", origin);
    }
    if (currentOriginCoordinates) {
      params.append("origemLatitude", String(currentOriginCoordinates.latitude));
      params.append("origemLongitude", String(currentOriginCoordinates.longitude));
    }
    return params.toString();
  }

  function setFeedback(message, error = false) {
    if (!feedbackEl) {
      return;
    }
    feedbackEl.textContent = message || "";
    feedbackEl.classList.toggle("is-error", Boolean(error));
    feedbackEl.classList.toggle("is-ok", !error && Boolean(message));
  }

  function setOriginStatus(message, error = false) {
    if (!originStatusEl) {
      return;
    }
    originStatusEl.textContent = message || "";
    originStatusEl.classList.toggle("text-danger", Boolean(error));
  }

  function clearOriginCoordinates() {
    currentOriginCoordinates = null;
    if (originEl) {
      delete originEl.dataset.originMode;
    }
    setOriginStatus("");
  }

  function formatCoordinate(value) {
    return Number(value).toFixed(5);
  }

  function applyCurrentLocation(position) {
    const latitude = Number(position?.coords?.latitude);
    const longitude = Number(position?.coords?.longitude);
    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
      throw new Error("Nao foi possivel ler as coordenadas da localizacao atual.");
    }
    currentOriginCoordinates = { latitude, longitude };
    if (originEl) {
      originEl.value = gpsOriginLabel;
      originEl.dataset.originMode = "gps";
    }
    setOriginStatus(
      `GPS pronto em ${formatCoordinate(latitude)}, ${formatCoordinate(longitude)}.`
    );
    invalidatePreview("Origem atualizada com a localizacao do dispositivo. Clique em Organizar rotas.");
    setFeedback("Localizacao atual carregada. Clique em Organizar rotas.");
  }

  function resolveGeolocationError(error) {
    if (!error) {
      return "Nao foi possivel obter a localizacao atual.";
    }
    switch (error.code) {
      case error.PERMISSION_DENIED:
        return "Permissao de localizacao negada no navegador.";
      case error.POSITION_UNAVAILABLE:
        return "Localizacao indisponivel no dispositivo.";
      case error.TIMEOUT:
        return "Tempo esgotado ao tentar capturar o GPS.";
      default:
        return "Nao foi possivel obter a localizacao atual.";
    }
  }

  function escapeHtml(value) {
    return String(value || "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#39;");
  }

  function formatDistance(value) {
    if (value === null || value === undefined || value === "") {
      return "-";
    }
    return `${value} km`;
  }

  function formatDuration(seconds) {
    if (!Number.isFinite(seconds) || seconds <= 0) {
      return "-";
    }
    const totalMinutes = Math.round(seconds / 60);
    if (totalMinutes < 60) {
      return `${totalMinutes} min`;
    }
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    return minutes > 0 ? `${hours}h ${minutes}min` : `${hours}h`;
  }

  function hasValue(value) {
    return value !== null && value !== undefined && value !== "";
  }

  function isCompletePreviewStop(stop) {
    return Boolean(stop)
      && hasValue(stop.pedidoId)
      && hasValue(stop.enderecoEntrega)
      && hasValue(stop.distanciaAnteriorKm)
      && hasValue(stop.distanciaAcumuladaKm)
      && hasValue(stop.latitude)
      && hasValue(stop.longitude);
  }

  function isCompletePreview(payload) {
    const stops = Array.isArray(payload?.paradas) ? payload.paradas : [];
    return hasValue(payload?.distanciaTotalKm)
      && stops.length > 0
      && stops.every(isCompletePreviewStop);
  }

  function clearRouteOrderBadges() {
    document.querySelectorAll("[data-route-order]").forEach((badge) => {
      badge.textContent = "";
      badge.classList.add("is-hidden");
    });
    document.querySelectorAll("tr[data-pedido-id]").forEach((row) => {
      row.classList.remove("is-route-planned");
    });
  }

  function clearPreview() {
    previewSignature = null;
    clearRouteOrderBadges();
    if (previewEl) {
      previewEl.classList.add("is-hidden");
    }
    if (previewOriginEl) {
      previewOriginEl.textContent = "-";
    }
    if (previewDistanceEl) {
      previewDistanceEl.textContent = "-";
    }
    if (previewCountEl) {
      previewCountEl.textContent = "0";
    }
    if (previewListEl) {
      previewListEl.innerHTML = "";
    }
    if (previewMapEl) {
      previewMapEl.classList.add("is-hidden");
      previewMapEl.setAttribute("href", "#");
    }
  }

  function applyRouteOrderBadges(stops) {
    clearRouteOrderBadges();
    (stops || []).forEach((stop) => {
      const row = document.querySelector(`tr[data-pedido-id="${stop.pedidoId}"]`);
      if (!row) {
        return;
      }
      row.classList.add("is-route-planned");
      const badge = row.querySelector("[data-route-order]");
      if (badge) {
        badge.textContent = `Parada ${stop.ordem}`;
        badge.classList.remove("is-hidden");
      }
    });
  }

  function renderPreview(payload) {
    if (!previewEl || !previewListEl) {
      return;
    }
    const stops = Array.isArray(payload?.paradas) ? payload.paradas : [];
    applyRouteOrderBadges(stops);
    previewOriginEl.textContent = payload?.origem || "-";
    previewDistanceEl.textContent = formatDistance(payload?.distanciaTotalKm);
    previewCountEl.textContent = String(stops.length);
    previewListEl.innerHTML = stops
      .map((stop, index) => {
        const previousLabel =
          index === 0 ? "Base" : `pedido #${stops[index - 1]?.pedidoId || "-"}`;
        return `
          <li class="entregas-preview__item">
            <div class="entregas-preview__item-order">${escapeHtml(stop.ordem)}</div>
            <div class="entregas-preview__item-body">
              <div class="entregas-preview__item-title">
                <strong>Pedido #${escapeHtml(stop.pedidoId)} - ${escapeHtml(stop.clienteNome || "Cliente")}</strong>
                <span>${escapeHtml(formatDistance(stop.distanciaAnteriorKm))} apos ${escapeHtml(previousLabel)}</span>
              </div>
              <p>${escapeHtml(stop.enderecoEntrega || "-")}</p>
              <div class="entregas-preview__item-meta">
                <span>Acumulado: ${escapeHtml(formatDistance(stop.distanciaAcumuladaKm))}</span>
                <span>Tempo: ${escapeHtml(formatDuration(stop.duracaoAcumuladaSegundos))}</span>
              </div>
            </div>
          </li>
        `;
      })
      .join("");
    if (payload?.mapaUrl) {
      previewMapEl.setAttribute("href", payload.mapaUrl);
      previewMapEl.classList.remove("is-hidden");
    } else {
      previewMapEl.classList.add("is-hidden");
      previewMapEl.setAttribute("href", "#");
    }
    previewEl.classList.remove("is-hidden");
  }

  function invalidatePreview(message) {
    if (!previewSignature) {
      return;
    }
    clearPreview();
    if (message) {
      setFeedback(message);
    }
  }

  function syncSelectedState() {
    const checkboxes = Array.from(document.querySelectorAll(rowCheckboxSelector))
      .filter((input) => !input.disabled);
    const checked = checkboxes.filter((input) => input.checked);
    if (countEl) {
      countEl.textContent = String(checked.length);
    }
    if (toggleAllEl) {
      toggleAllEl.checked = checkboxes.length > 0 && checked.length === checkboxes.length;
      toggleAllEl.indeterminate = checked.length > 0 && checked.length < checkboxes.length;
    }
  }

  async function organizeRoute() {
    const pedidoIds = resolveRoutePedidoIds();
    if (pedidoIds.length < 3) {
      setFeedback("Nao ha pedidos prontos suficientes nesta pagina para organizar a rota.", true);
      return;
    }

    organizeButton.disabled = true;
    createButton.disabled = true;
    setFeedback("Calculando a menor rota para os pedidos selecionados...");
    try {
      const response = await fetch(
        "/api/admin/entregas/roteirizar",
        withCsrf({
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
          },
          body: buildRouteRequestBody(pedidoIds),
        })
      );
      const raw = await response.text();
      let payload = null;
      try {
        payload = raw ? JSON.parse(raw) : null;
      } catch {
        payload = { message: raw };
      }

      if (!response.ok) {
        const message = payload?.message || payload?.error || `Falha ao organizar a rota (HTTP ${response.status}).`;
        throw new Error(String(message).trim());
      }
      if (!Array.isArray(payload?.paradas) || payload.paradas.length === 0) {
        throw new Error("Nao foi possivel montar a ordem da rota agora.");
      }
      if (!isCompletePreview(payload)) {
        throw new Error(
          "Nao foi possivel calcular a rota completa. A rota so pode ser usada quando todas as distancias forem calculadas."
        );
      }

      previewSignature = currentSignature();
      renderPreview(payload);
      setFeedback("Rota organizada. Confira a ordem sugerida e confirme a criacao.");
    } catch (error) {
      console.error(error);
      clearPreview();
      setFeedback(error?.message || "Nao foi possivel organizar a rota agora.", true);
    } finally {
      organizeButton.disabled = false;
      createButton.disabled = false;
    }
  }

  async function createRoute() {
    const pedidoIds = resolveRoutePedidoIds();
    if (pedidoIds.length < 3) {
      setFeedback("Nao ha pedidos prontos suficientes nesta pagina para criar a rota.", true);
      return;
    }

    createButton.disabled = true;
    organizeButton.disabled = true;
    setFeedback(
      previewSignature === currentSignature()
        ? "Salvando a rota com a ordem organizada..."
        : "Montando rota e salvando paradas..."
    );
    try {
      const response = await fetch(
        "/api/admin/entregas/rotas",
        withCsrf({
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
          },
          body: buildRouteRequestBody(pedidoIds),
        })
      );
      const raw = await response.text();
      let payload = null;
      try {
        payload = raw ? JSON.parse(raw) : null;
      } catch {
        payload = { message: raw };
      }

      if (!response.ok) {
        const message = payload?.message || payload?.error || `Falha ao criar rota (HTTP ${response.status}).`;
        throw new Error(String(message).trim());
      }
      if (!payload?.id) {
        throw new Error("A rota foi criada, mas a resposta do servidor veio incompleta.");
      }

      window.location.href = `/admin/entregas/rotas/${encodeURIComponent(payload.id)}`;
    } catch (error) {
      console.error(error);
      setFeedback(error?.message || "Nao foi possivel criar a rota agora.", true);
    } finally {
      createButton.disabled = false;
      organizeButton.disabled = false;
    }
  }

  function useCurrentLocation() {
    if (!navigator.geolocation) {
      setOriginStatus("Geolocalizacao nao suportada neste navegador.", true);
      setFeedback("Geolocalizacao nao suportada neste navegador.", true);
      return;
    }

    if (useGpsButton) {
      useGpsButton.disabled = true;
    }
    setOriginStatus("Capturando localizacao atual...");
    navigator.geolocation.getCurrentPosition(
      (position) => {
        try {
          applyCurrentLocation(position);
        } catch (error) {
          setOriginStatus(error?.message || "Falha ao ler a localizacao atual.", true);
          setFeedback(error?.message || "Falha ao ler a localizacao atual.", true);
        } finally {
          if (useGpsButton) {
            useGpsButton.disabled = false;
          }
        }
      },
      (error) => {
        const message = resolveGeolocationError(error);
        setOriginStatus(message, true);
        setFeedback(message, true);
        if (useGpsButton) {
          useGpsButton.disabled = false;
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 15000,
        maximumAge: 60000,
      }
    );
  }

  toggleAllEl?.addEventListener("change", () => {
    document.querySelectorAll(rowCheckboxSelector).forEach((input) => {
      if (input.disabled) {
        return;
      }
      input.checked = toggleAllEl.checked;
    });
    syncSelectedState();
    invalidatePreview("Selecao alterada. Clique em Organizar rotas para recalcular a ordem sugerida.");
  });

  document.addEventListener("change", (event) => {
    if (event.target?.matches(rowCheckboxSelector)) {
      syncSelectedState();
      invalidatePreview("Selecao alterada. Clique em Organizar rotas para recalcular a ordem sugerida.");
    }
  });

  originEl?.addEventListener("input", () => {
    if (originEl?.dataset.originMode === "gps") {
      clearOriginCoordinates();
    }
    invalidatePreview("Origem alterada. Clique em Organizar rotas para recalcular a ordem sugerida.");
  });

  useGpsButton?.addEventListener("click", () => {
    useCurrentLocation();
  });

  organizeButton.addEventListener("click", () => {
    organizeRoute();
  });

  createButton.addEventListener("click", () => {
    createRoute();
  });

  clearPreview();
  syncSelectedState();
})();

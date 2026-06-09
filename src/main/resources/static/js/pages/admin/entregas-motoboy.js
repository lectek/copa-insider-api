(() => {
  const root = document.querySelector("[data-driver-tracking='true']");
  if (!root) {
    return;
  }

  const detailUrl = root.dataset.detailUrl;
  const updateUrl = root.dataset.updateUrl;
  const initialRouteStatus = root.dataset.routeStatus || "";
  const mapFrame = root.querySelector("[data-driver-map-frame]");
  const mapEmpty = root.querySelector("[data-driver-map-empty]");
  const trackingStatus = root.querySelector("[data-driver-tracking-status]");
  const lastSeen = root.querySelector("[data-driver-last-seen]");
  const distanceEl = root.querySelector("[data-driver-distance]");
  const nextStopEl = root.querySelector("[data-driver-next-stop]");

  let lastSentAt = 0;
  let refreshTimer = null;
  let watchId = null;

  function getCsrf() {
    const hidden = document.querySelector('input[name="_csrf"]');
    const metaToken = document.querySelector('meta[name="_csrf"]');
    const metaHeader = document.querySelector('meta[name="_csrf_header"]');
    return {
      token: hidden?.value || metaToken?.content || "",
      header: metaHeader?.content || "X-CSRF-TOKEN",
    };
  }

  function withCsrf(init = {}) {
    const csrf = getCsrf();
    const headers = Object.assign({}, init.headers || {});
    if (csrf.token) {
      headers[csrf.header] = csrf.token;
    }
    headers.Accept = headers.Accept || "application/json";
    headers["Content-Type"] = headers["Content-Type"] || "application/json";
    headers["X-Requested-With"] = "XMLHttpRequest";
    return Object.assign({}, init, {
      credentials: "same-origin",
      headers,
    });
  }

  function formatDateTime(value) {
    if (!value) {
      return "Aguardando localizacao";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return "Aguardando localizacao";
    }
    return new Intl.DateTimeFormat("pt-BR", {
      dateStyle: "short",
      timeStyle: "medium",
    }).format(date);
  }

  function setMap(url) {
    if (mapFrame) {
      if (url) {
        mapFrame.src = url;
        mapFrame.classList.remove("is-hidden");
      } else {
        mapFrame.classList.add("is-hidden");
      }
    }
    if (mapEmpty) {
      mapEmpty.classList.toggle("is-hidden", Boolean(url));
    }
  }

  function applyView(view) {
    if (!view) {
      return;
    }
    const routeActive = view.status === "EM_EXECUCAO";
    if (trackingStatus) {
      trackingStatus.textContent = routeActive
        ? "Localizacao ao vivo ativa no painel do motoboy."
        : "A rota precisa estar em execucao para enviar localizacao.";
    }
    if (lastSeen) {
      lastSeen.textContent = formatDateTime(view.motoristaLocalizacaoEm);
    }
    if (distanceEl) {
      distanceEl.textContent = view.distanciaAteProximaParadaKm != null
        ? `${view.distanciaAteProximaParadaKm} km`
        : "-";
    }
    if (nextStopEl) {
      nextStopEl.textContent = view.proximaParada?.clienteNome || "Rota concluida";
    }
    setMap(view.mapaAoVivoUrl || "");
    if (routeActive) {
      startTracking();
    } else {
      stopTracking();
    }
  }

  async function refresh() {
    if (!detailUrl) {
      return;
    }
    try {
      const response = await fetch(detailUrl, {
        credentials: "same-origin",
        headers: {
          Accept: "application/json",
          "X-Requested-With": "XMLHttpRequest",
        },
      });
      if (!response.ok) {
        throw new Error(`Falha ao atualizar painel (HTTP ${response.status}).`);
      }
      applyView(await response.json());
    } catch (error) {
      if (trackingStatus) {
        trackingStatus.textContent = error?.message || "Nao foi possivel atualizar a rota.";
      }
    }
  }

  async function sendPosition(position) {
    if (!updateUrl) {
      return;
    }
    const now = Date.now();
    if (now - lastSentAt < 12000) {
      return;
    }
    lastSentAt = now;

    try {
      const response = await fetch(updateUrl, withCsrf({
        method: "POST",
        body: JSON.stringify({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        }),
      }));
      if (!response.ok) {
        throw new Error(`Falha ao enviar localizacao (HTTP ${response.status}).`);
      }
      applyView(await response.json());
    } catch (error) {
      if (trackingStatus) {
        trackingStatus.textContent = error?.message || "Nao foi possivel enviar sua localizacao.";
      }
    }
  }

  function handleLocationError(error) {
    if (!trackingStatus) {
      return;
    }
    if (error?.code === 1) {
      trackingStatus.textContent = "Permita a geolocalizacao para compartilhar sua rota.";
      return;
    }
    trackingStatus.textContent = "Nao foi possivel ler sua localizacao agora.";
  }

  function startTracking() {
    if (watchId !== null) {
      return;
    }
    if (!navigator.geolocation) {
      if (trackingStatus) {
        trackingStatus.textContent = "Geolocalizacao indisponivel neste navegador.";
      }
      return;
    }
    watchId = navigator.geolocation.watchPosition(
      sendPosition,
      handleLocationError,
      {
        enableHighAccuracy: true,
        maximumAge: 10000,
        timeout: 15000,
      }
    );
  }

  function stopTracking() {
    if (watchId === null || !navigator.geolocation) {
      return;
    }
    navigator.geolocation.clearWatch(watchId);
    watchId = null;
  }

  refresh();
  if (initialRouteStatus === "EM_EXECUCAO") {
    startTracking();
  }
  refreshTimer = window.setInterval(refresh, 20000);

  window.addEventListener("beforeunload", () => {
    if (refreshTimer) {
      window.clearInterval(refreshTimer);
    }
    stopTracking();
  });
})();

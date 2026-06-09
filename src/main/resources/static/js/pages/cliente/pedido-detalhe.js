(() => {
  const POLL_INTERVAL_MS = 30000;

  const section = document.querySelector("[data-order-tracking='true']");
  if (!section) {
    return;
  }

  const trackingUrl = (section.dataset.trackingUrl || "").trim();
  if (!trackingUrl) {
    return;
  }

  const preparingMessage = (section.dataset.preparingMessage || "").trim();
  const titleNode = section.querySelector("[data-order-tracking-title]");
  const subtitleNode = section.querySelector("[data-order-tracking-subtitle]");
  const messageNode = section.querySelector("[data-order-tracking-message]");
  const lastSeenNode = section.querySelector("[data-order-last-seen]");
  const distanceNode = section.querySelector("[data-order-distance]");
  const etaNode = section.querySelector("[data-order-eta]");
  const stopsAheadNode = section.querySelector("[data-order-stops-ahead]");
  const addressNode = section.querySelector("[data-order-address]");
  const statusBadgeNode = section.querySelector("[data-order-status-badge]");
  const nextStopBadgeNode = section.querySelector("[data-order-next-stop-badge]");
  const arrivingBadgeNode = section.querySelector("[data-order-arriving-badge]");
  const mapFrame = section.querySelector("[data-order-map-frame]");
  const mapEmpty = section.querySelector("[data-order-map-empty]");
  const googleMapsLink = section.querySelector("[data-order-google-maps]");
  const wazeLink = section.querySelector("[data-order-waze]");

  let pollHandle = null;

  function setText(node, value) {
    if (node) {
      node.textContent = value;
    }
  }

  function toggleHidden(node, hidden) {
    if (!node) {
      return;
    }
    node.classList.toggle("is-hidden", hidden);
    if (hidden) {
      node.setAttribute("aria-hidden", "true");
    } else {
      node.removeAttribute("aria-hidden");
    }
  }

  function formatDistance(value) {
    if (value === null || value === undefined || value === "") {
      return "-";
    }
    return `${value} km`;
  }

  function formatStopsAhead(tracking) {
    if (!routeInExecution(tracking)) {
      return "-";
    }
    if (tracking?.motoboyChegou || tracking?.pedidoEhProximaParada) {
      return "0";
    }
    return `${tracking?.entregasAntesDaSua ?? 0}`;
  }

  function formatDateTime(value) {
    if (!value) {
      return "Aguardando localizacao";
    }

    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return "Aguardando localizacao";
    }

    return new Intl.DateTimeFormat("pt-BR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit"
    }).format(parsed);
  }

  function routeInExecution(tracking) {
    return !!tracking?.disponivel && tracking.statusRota === "EM_EXECUCAO";
  }

  function resolveTitle(tracking) {
    if (!routeInExecution(tracking)) {
      return "Sendo preparado";
    }
    if (tracking?.motoboyChegou) {
      return "Motoboy chegou";
    }
    if (tracking?.aproximando) {
      return "Motoboy chegando";
    }
    return "Motoboy a caminho";
  }

  function resolveSubtitle(tracking) {
    if (!routeInExecution(tracking)) {
      return preparingMessage || "Seu pedido ainda nao entrou em rota.";
    }
    if (tracking?.motoboyChegou) {
      return "O motoboy ja esta no local. Tenha o codigo de confirmacao em maos.";
    }
    if (tracking?.etaLabel) {
      return `Previsao aproximada: ${tracking.etaLabel}`;
    }
    return "Acompanhe a ultima posicao enviada pelo painel do motoboy.";
  }

  function resolveStatusBadge(tracking) {
    if (routeInExecution(tracking)) {
      if (tracking?.motoboyChegou) {
        return "Motoboy chegou";
      }
      return tracking.pedidoEhProximaParada ? "Proxima parada" : "Em rota";
    }
    return tracking?.disponivel ? "Pronto para entrega" : "Sendo preparado";
  }

  function applyLinks(linkNode, href, active) {
    if (!linkNode) {
      return;
    }
    if (href) {
      linkNode.href = href;
    }
    toggleHidden(linkNode, !active || !href);
  }

  function applyMap(tracking, active) {
    const mapUrl = (tracking?.mapaAoVivoUrl || "").trim();
    if (mapFrame) {
      if (active && mapUrl) {
        if (mapFrame.getAttribute("src") !== mapUrl) {
          mapFrame.setAttribute("src", mapUrl);
        }
        toggleHidden(mapFrame, false);
      } else {
        mapFrame.removeAttribute("src");
        toggleHidden(mapFrame, true);
      }
    }

    if (mapEmpty) {
      setText(
        mapEmpty,
        active
          ? "O mapa aparece assim que o motoboy compartilhar a localizacao da rota."
          : "Seu pedido ainda nao entrou em rota. Avisaremos quando sair para entrega."
      );
      toggleHidden(mapEmpty, active && !!mapUrl);
    }
  }

  function applyBadges(tracking, active) {
    const statusLabel = resolveStatusBadge(tracking);
    if (statusBadgeNode) {
      setText(statusBadgeNode, statusLabel);
      statusBadgeNode.classList.toggle(
        "badge--success",
        active && (!!tracking?.pedidoEhProximaParada || !!tracking?.motoboyChegou)
      );
      statusBadgeNode.classList.toggle(
        "badge--neutral",
        !active && !!tracking?.disponivel
      );
    }

    if (nextStopBadgeNode && nextStopBadgeNode !== statusBadgeNode) {
      setText(nextStopBadgeNode, statusLabel);
      nextStopBadgeNode.classList.toggle(
        "badge--success",
        active && (!!tracking?.pedidoEhProximaParada || !!tracking?.motoboyChegou)
      );
      nextStopBadgeNode.classList.toggle(
        "badge--neutral",
        !active && !!tracking?.disponivel
      );
    }

    toggleHidden(
      arrivingBadgeNode,
      !(active && !!tracking?.aproximando && !tracking?.motoboyChegou)
    );
  }

  function applyTracking(tracking) {
    const active = routeInExecution(tracking);

    setText(titleNode, resolveTitle(tracking));
    setText(subtitleNode, resolveSubtitle(tracking));
    setText(
      messageNode,
      tracking?.mensagem || preparingMessage || "Rastreamento indisponivel no momento."
    );
    setText(lastSeenNode, formatDateTime(tracking?.motoristaLocalizacaoEm));
    setText(distanceNode, formatDistance(tracking?.distanciaAteEntregaKm));
    setText(etaNode, tracking?.etaLabel || "-");
    setText(stopsAheadNode, formatStopsAhead(tracking));
    setText(addressNode, tracking?.enderecoEntrega || addressNode?.textContent || "-");

    applyBadges(tracking, active);
    applyMap(tracking, active);
    applyLinks(googleMapsLink, tracking?.googleMapsUrl, active);
    applyLinks(wazeLink, tracking?.wazeUrl, active);
  }

  async function refreshTracking() {
    try {
      const response = await fetch(trackingUrl, {
        method: "GET",
        headers: {
          Accept: "application/json"
        }
      });
      if (!response.ok) {
        return;
      }
      const tracking = await response.json();
      applyTracking(tracking);
    } catch (error) {
      window.console?.warn?.("Falha ao atualizar rastreamento do pedido.", error);
    }
  }

  function startPolling() {
    if (pollHandle !== null) {
      window.clearInterval(pollHandle);
    }
    pollHandle = window.setInterval(refreshTracking, POLL_INTERVAL_MS);
  }

  document.addEventListener("visibilitychange", () => {
    if (document.hidden) {
      if (pollHandle !== null) {
        window.clearInterval(pollHandle);
        pollHandle = null;
      }
      return;
    }
    refreshTracking();
    startPolling();
  });

  refreshTracking();
  startPolling();
})();

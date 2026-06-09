(() => {
  const DEFAULT_SOURCE_URL = "/api/public/produtos/destaques?limit=10";
  const AUTOPLAY_MS = 4000;

  document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-carousel]").forEach((root) => {
      initCarousel(root);
    });
  });

  async function initCarousel(root) {
    const viewport = root.querySelector(".carousel__viewport");
    const prevBtn = root.querySelector(".carousel__btn.prev");
    const nextBtn = root.querySelector(".carousel__btn.next");
    const dots = root.querySelector(".carousel__dots");
    const key = root.dataset.carouselKey || "";
    const sourceUrl = root.dataset.carouselSourceUrl || DEFAULT_SOURCE_URL;

    if (!viewport || !prevBtn || !nextBtn || !dots) {
      return;
    }

    let currentIndex = 0;
    let timer = null;
    let items = Array.from(viewport.children).filter((node) => node.nodeType === Node.ELEMENT_NODE);

    if (!items.length) {
      const ssrItems = resolveSSRItems(key);
      if (ssrItems.length) {
        viewport.innerHTML = ssrItems.map(renderItem).join("");
        items = Array.from(viewport.children).filter((node) => node.nodeType === Node.ELEMENT_NODE);
      }
    }

    if (!items.length) {
      try {
        const response = await fetch(sourceUrl, { headers: { Accept: "application/json" } });
        if (response.ok) {
          const payload = await response.json();
          const fetchedItems = Array.isArray(payload)
            ? payload
            : (Array.isArray(payload?.content) ? payload.content : []);
          if (fetchedItems.length) {
            viewport.innerHTML = fetchedItems.map(renderItem).join("");
            items = Array.from(viewport.children).filter((node) => node.nodeType === Node.ELEMENT_NODE);
          }
        }
      } catch (error) {
        console.error("Falha ao carregar carrossel", error);
      }
    }

    if (!items.length) {
      viewport.innerHTML = '<p class="muted">Sem produtos disponiveis no momento.</p>';
      prevBtn.disabled = true;
      nextBtn.disabled = true;
      dots.innerHTML = "";
      return;
    }

    function render() {
      items.forEach((item, index) => {
        item.hidden = index !== currentIndex;
        item.setAttribute("aria-hidden", index === currentIndex ? "false" : "true");
      });

      prevBtn.disabled = items.length <= 1;
      nextBtn.disabled = items.length <= 1;
      dots.innerHTML = items.map((_, index) => `
        <button type="button"
                role="tab"
                class="dot ${index === currentIndex ? "active" : ""}"
                aria-selected="${index === currentIndex}"
                aria-label="Ir para item ${index + 1} de ${items.length}"></button>
      `).join("");

      dots.querySelectorAll(".dot").forEach((dot, index) => {
        dot.addEventListener("click", () => go(index));
      });
    }

    function go(index) {
      if (!items.length) {
        return;
      }
      currentIndex = (index + items.length) % items.length;
      render();
      restart();
    }

    function next() {
      go(currentIndex + 1);
    }

    function prev() {
      go(currentIndex - 1);
    }

    function stop() {
      if (timer) {
        clearInterval(timer);
        timer = null;
      }
    }

    function start() {
      if (timer || items.length <= 1 || prefersReducedMotion()) {
        return;
      }
      timer = window.setInterval(next, AUTOPLAY_MS);
    }

    function restart() {
      stop();
      start();
    }

    let touchStartX = null;
    viewport.addEventListener("touchstart", (event) => {
      touchStartX = event.touches[0].clientX;
      stop();
    }, { passive: true });

    viewport.addEventListener("touchend", (event) => {
      if (touchStartX == null) {
        return;
      }
      const deltaX = event.changedTouches[0].clientX - touchStartX;
      touchStartX = null;
      if (Math.abs(deltaX) > 30) {
        deltaX < 0 ? next() : prev();
      }
      start();
    }, { passive: true });

    root.addEventListener("pointerenter", stop);
    root.addEventListener("pointerleave", start);
    root.addEventListener("focusin", stop);
    root.addEventListener("focusout", start);
    prevBtn.addEventListener("click", prev);
    nextBtn.addEventListener("click", next);

    root.addEventListener("keydown", (event) => {
      if (event.key === "ArrowRight") {
        next();
      }
      if (event.key === "ArrowLeft") {
        prev();
      }
    });

    document.addEventListener("visibilitychange", () => {
      if (document.hidden) {
        stop();
      } else {
        start();
      }
    });

    render();
    start();
  }

  function resolveSSRItems(key) {
    if (!key || typeof window === "undefined" || !window.__HOME_CAROUSELS) {
      return [];
    }
    const items = window.__HOME_CAROUSELS[key];
    return Array.isArray(items) ? items : [];
  }

  function renderItem(product) {
    const nome = escapeHtml(product?.nome || product?.titulo || "Produto");
    const categoria = escapeHtml(product?.categoria || "Categoria");
    const preco = formatPrice(product?.preco ?? product?.precoVenda ?? 0);
    const imagem = resolveImage(product?.imagem || product?.imagemUrl);
    const href = `/produto/${encodeURIComponent(product?.id ?? product?.entityId ?? "")}`;

    return `
      <article class="carousel__item" aria-roledescription="slide" aria-label="${nome}">
        <a class="carousel__media" href="${href}">
          <img class="carousel__img" src="${imagem}" alt="${nome}" loading="lazy" decoding="async" />
        </a>
        <div class="carousel__info">
          <span class="carousel__eyebrow">${categoria}</span>
          <h3 class="carousel__title">${nome}</h3>
          <div class="carousel__price-wrap">
            <span class="carousel__price">${preco}</span>
          </div>
          <div class="carousel__actions">
            <a class="carousel__cta" href="${href}">Ver produto</a>
          </div>
        </div>
      </article>
    `;
  }

  function resolveImage(raw) {
    const value = (raw || "").trim();
    if (!value) {
      return "/img/produtos/placeholder-generico.png";
    }
    if (/^https?:\/\/|^\/\//i.test(value)) {
      return value;
    }
    if (value.startsWith("/")) {
      return value;
    }
    if (/^(media|images|img|assets)\//i.test(value)) {
      return `/${value}`;
    }
    return `/media/products/${value.replace(/^[/\\]+/, "")}`;
  }

  function formatPrice(value) {
    return Number(value || 0).toLocaleString("pt-BR", {
      style: "currency",
      currency: "BRL"
    });
  }

  function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, (char) => ({
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#039;"
    }[char]));
  }

  function prefersReducedMotion() {
    return window.matchMedia
      && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  }
})();

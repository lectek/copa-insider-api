/* =========================================================
   UI Core – RedeMaisFarma (LekTeC)
   - Menu mobile off-canvas acessível (com backdrop e trap de foco)
   - Skip links, Dark mode, Header shadow
   - Newsletter (rodapé), Modais, Navegação do Painel
   ========================================================= */

document.addEventListener("DOMContentLoaded", () => {
  initAccessibleNav();        // substitui initNavigationToggle()
  initSkipLinks();
  initDarkModeDetection();
  initScrollShadow();
  initNewsletterValidation();
  initModalEvents();
  initPainelNavigation();
});

/* ===========================
   📱 Menu responsivo acessível
   =========================== */
function initAccessibleNav() {
  // Aceita .nav-toggle OU .menu-button (mantém compatibilidade)
  const toggleButton = document.querySelector(".nav-toggle, .menu-button[data-role='nav-toggle']");
  const navMenu = document.getElementById("primary-nav");
  if (!toggleButton || !navMenu) return;

  // Garante atributos ARIA
  toggleButton.setAttribute("aria-controls", "primary-nav");
  toggleButton.setAttribute("aria-expanded", "false");
  navMenu.setAttribute("role", "navigation");
  navMenu.setAttribute("aria-label", navMenu.getAttribute("aria-label") || "Menu principal");

  // Injeta backdrop se não existir
  let backdrop = document.querySelector("button.nav-backdrop");
  if (!backdrop) {
    backdrop = document.createElement("button");
    backdrop.type = "button";
    backdrop.className = "nav-backdrop";
    backdrop.setAttribute("aria-label", "Fechar menu");
    document.body.appendChild(backdrop);
  }

  // Utilidades
  const root = document.documentElement;
  const body = document.body;
  let lastFocused = null;

  const isOpen = () => root.classList.contains("nav-open");
  const focusables = () => navMenu.querySelectorAll(
    'a[href],button:not([disabled]),input,select,textarea,[tabindex]:not([tabindex="-1"])'
  );

  function openNav() {
    lastFocused = document.activeElement;
    root.classList.add("nav-open");
    body.classList.add("no-scroll");
    toggleButton.setAttribute("aria-expanded", "true");
    const f = focusables();
    f.length && f[0].focus();
  }

  function closeNav() {
    root.classList.remove("nav-open");
    body.classList.remove("no-scroll");
    toggleButton.setAttribute("aria-expanded", "false");
    if (lastFocused) lastFocused.focus();
  }

  function toggleNav() {
    isOpen() ? closeNav() : openNav();
  }

  // Eventos
  toggleButton.addEventListener("click", toggleNav);
  backdrop.addEventListener("click", closeNav);

  // Fecha ao clicar em links do menu
  document.addEventListener("click", (e) => {
    const a = e.target.closest("a[href]");
    if (a && navMenu.contains(a)) closeNav();
  });

  // Teclado: ESC fecha e Tab trapa foco
  document.addEventListener("keydown", (e) => {
    if (!isOpen()) return;

    if (e.key === "Escape") {
      e.preventDefault();
      closeNav();
      return;
    }

    if (e.key === "Tab") {
      const f = Array.from(focusables());
      if (!f.length) return;
      const first = f[0], last = f[f.length - 1];

      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault(); last.focus();
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault(); first.focus();
      }
    }
  });

  // Segurança em resize: se voltar ao desktop, garante fechado
  window.addEventListener("resize", () => {
    if (window.innerWidth >= 1024 && isOpen()) closeNav();
  });
}

/* ==============
   ⌨️ Skip links
   ============== */
function initSkipLinks() {
  const skipLinks = document.querySelectorAll(".skip-link, .skip-footer");
  skipLinks.forEach(link => {
    link.addEventListener("click", () => {
      const targetId = link.getAttribute("href")?.replace("#", "");
      const target = targetId && document.getElementById(targetId);
      if (target) setTimeout(() => target.focus(), 0);
    });
  });
}

/* ====================
   🌙 Dark Mode automático
   ==================== */
function initDarkModeDetection() {
  try {
    const root = document.documentElement;
    const stored = window.localStorage ? window.localStorage.getItem("rmf-theme") : null;
    if (stored === "dark" || stored === "light") {
      root.setAttribute("data-theme", stored);
      root.style.colorScheme = stored;
      return;
    }

    const prefersDark = window.matchMedia
      && window.matchMedia("(prefers-color-scheme: dark)").matches;
    const theme = prefersDark ? "dark" : "light";
    root.setAttribute("data-theme", theme);
    root.style.colorScheme = theme;
  } catch {}
}

/* ============================
   🧭 Header com sombra ao rolar
   ============================ */
function initScrollShadow() {
  const header = document.querySelector(".site-header, header, .navbar");
  if (!header) return;

  const onScroll = () => {
    if (window.scrollY > 8) header.classList.add("scrolled");
    else header.classList.remove("scrolled");
  };

  onScroll();
  window.addEventListener("scroll", onScroll, { passive: true });
}

/* ===========================
   📧 Newsletter (robusta/tolerante)
   =========================== */
function initNewsletterValidation() {
  // Aceita tanto #newsletter-form quanto .newsletter-form
  const form = document.querySelector("#newsletter-form, .newsletter-form");
  if (!form) return;

  // Aceita #newsletter-email ou #nl-email
  const emailInput = form.querySelector("#newsletter-email, #nl-email, input[type='email']");
  // status padrão (#newsletter-status) se existir
  const status = document.getElementById("newsletter-status");

  // Garante um contêiner de erro dentro do form (.error-message)
  let errorMsg = form.querySelector(".error-message");
  if (!errorMsg) {
    errorMsg = document.createElement("div");
    errorMsg.className = "error-message";
    errorMsg.setAttribute("role", "alert");
    errorMsg.style.marginTop = "6px";
    form.appendChild(errorMsg);
  }

  form.addEventListener("submit", (e) => {
    const ok = !!emailInput?.value && /\S+@\S+\.\S+/.test(emailInput.value);
    if (!ok) {
      e.preventDefault();
      errorMsg.textContent = "Digite um e-mail válido.";
      form.setAttribute("aria-invalid", "true");
      emailInput?.focus();
    } else {
      errorMsg.textContent = "";
      form.removeAttribute("aria-invalid");
      if (status) status.textContent = "Inscrição enviada!";
    }
  });
}

/* ===============
   🧩 Modais genéricos
   =============== */
function abrirModal(id = "modal-padrao") {
  const modal = document.getElementById(id);
  if (!modal) return;
  modal.style.display = "flex";
  modal.setAttribute("aria-hidden", "false");
}

function fecharModal(id = "modal-padrao") {
  const modal = document.getElementById(id);
  if (!modal) return;
  modal.style.display = "none";
  modal.setAttribute("aria-hidden", "true");
}

function confirmarAcao(callback) {
  fecharModal();
  if (typeof callback === "function") callback();
}

function initModalEvents() {
  document.querySelectorAll(".modal-close").forEach(btn => {
    btn.addEventListener("click", () => {
      const modal = btn.closest(".modal");
      if (modal) fecharModal(modal.id);
    });
  });
}

/* =========================================
   🛠️ Painel admin: destacar botão de navegação
   ========================================= */
function initPainelNavigation() {
  const botoes = document.querySelectorAll(".menu-button");
  const urlAtual = (window.location.pathname || "").toLowerCase();

  botoes.forEach(btn => {
    const texto = (btn.textContent || "").trim().toLowerCase();

    if (
      (texto === "painel"   && urlAtual.includes("/admin"))    ||
      (texto === "produtos" && urlAtual.includes("/produtos")) ||
      (texto === "pedidos"  && urlAtual.includes("/pedidos"))  ||
      (texto === "clientes" && urlAtual.includes("/clientes"))
    ) {
      btn.classList.add("ativo");
    }

    if (!btn.getAttribute("href")) {
      btn.addEventListener("click", () => {
        switch (texto) {
          case "painel":   window.location.href = "/admin/painel";   break;
          case "produtos": window.location.href = "/admin/produtos"; break;
          case "pedidos":  window.location.href = "/admin/pedidos";  break;
          case "clientes": window.location.href = "/admin/clientes"; break;
        }
      });
    }
  });
}

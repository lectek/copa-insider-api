// ============================
// cadastro-otp.js (ATUALIZADO FINAL)
// Fluxo: valida -> envia OTP -> confirma -> cria conta
// ============================

// -------- Helpers --------
const $ = (s) => document.querySelector(s);
const byId = (id) => document.getElementById(id);

const errBox = (name) =>
  document.querySelector(`.error-message[data-for="${name}"]`);

const setFieldError = (el, msg = "") => {
  if (!el) return;
  el.setAttribute("aria-invalid", msg ? "true" : "false");
  const box = errBox(el.id);
  if (box) box.textContent = msg || "";
};

const setAlert = (msg = "", type = "error") => {
  const a = byId("alert");
  if (!a) return;
  a.textContent = msg || "";
  if (!msg) {
    a.className = "alert";
    a.hidden = true;
    return;
  }
  // components/alerts.css uses alert-info|alert-warning|alert-error
  a.className = `alert alert-${type}`;
  a.hidden = false;
};

// Lê CSRF das metas OU do cookie XSRF-TOKEN e decide o header
const readCookie = (name) => {
  const m = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`));
  return m ? decodeURIComponent(m[1]) : "";
};
const getCsrf = () => {
  const metaTok = document.querySelector('meta[name="_csrf"]')?.content || "";
  const metaHdr = document.querySelector('meta[name="_csrf_header"]')?.content || "";
  const cookieTok = readCookie("XSRF-TOKEN"); // padrão do Spring Security
  const header = metaHdr || "X-XSRF-TOKEN";
  const token  = cookieTok || metaTok || "";
  return { header, token };
};

// pick tolerante (camel/snake/diffs)
const pick = (obj, ...keys) => {
  if (!obj || typeof obj !== "object") return undefined;
  const k = keys.find((key) => Object.prototype.hasOwnProperty.call(obj, key));
  return k ? obj[k] : undefined;
};

// tenta ler JSON com segurança
async function safeParse(resp) {
  const ct = resp.headers.get("content-type") || "";
  if (ct.includes("application/json")) {
    try { return await resp.json(); } catch { return {}; }
  }
  try { return JSON.parse(await resp.text()); } catch { return {}; }
}

// -------- Campos --------
const form       = byId("cadastroForm");
const nome       = byId("nome");
const email      = byId("email");
const cpf        = byId("cpf");
const telefone   = byId("telefone");
const dataNasc   = byId("dataNascimento");
const senha      = byId("senha");
const confirmar  = byId("confirmarSenha");
const criarBtn   = byId("criarContaBtn");

// -------- Modal/OTP --------
const otpDialog       = byId("otpDialog");
const otpDestinoEl    = byId("otpDestino");
const otpConfirmBtn   = byId("otpConfirmBtn");
const otpResendBtn    = byId("otpResendBtn");
const otpError        = byId("otpError");
const otpTimer        = byId("otpTimer");
const otpNoCodeLink   = byId("otpNoCodeLink");
const otpInputs       = () => Array.from(otpDialog?.querySelectorAll(".otp") || []);

// -------- Hidden fields --------
const otpDeliveryIdInput = byId("otpDeliveryId");
const otpTokenInput      = byId("otpToken");

// Keeps the UI deterministic when Thymeleaf renders no checked radio initially.
const defaultCanal = byId("canalOtp1");
if (!document.querySelector('input[name="canalOtp"]:checked') && defaultCanal) {
  defaultCanal.checked = true;
}

// -------- Mensagens --------
const MSG = {
  nome: "Informe seu nome completo.",
  email: "Informe um e-mail válido.",
  cpf: "CPF inválido.",
  telefone: "Telefone inválido.",
  senhaVazia: "Informe uma senha.",
  senhaPattern: senha?.dataset?.patternMsg || "A senha precisa ter 8+ caracteres, maiúscula, minúscula, número e caractere especial.",
  confirmar: "As senhas não coincidem.",
  network: "Falha de rede. Tente novamente.",
  otpEnviado: "Código enviado. Verifique sua caixa de entrada.",
  otpInvalido: "Código incorreto. Tente novamente.",
  otpExpirado: "Código expirado. Reenvie um novo.",
  contaCriada: "Conta criada com sucesso! Você já pode entrar.",
};

// -------- Validações --------
const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(String(v).trim());
const onlyDigits = (v) => String(v || "").replace(/\D/g, "");
// Matches backend validation: 11 digits (with or without mask on input).
const isCpf = (v) => /^\d{11}$/.test(onlyDigits(v));
const isPhone = (v) => {
  const raw = String(v || "").trim();
  if (!raw) return true;
  // Same accepted charset/length as backend (@Pattern in CadastroClienteForm).
  return /^[+()\d\s-]{8,20}$/.test(raw);
};
const SENHA_REGEX = /^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,128}$/;
const senhaOk = (v) => SENHA_REGEX.test(v);

// Canal/Destino
const canalSelecionado = () =>
  (document.querySelector('input[name="canalOtp"]:checked')?.value || "email");
const getDestino = (canal) => (canal === "sms" ? telefone?.value : email?.value);

// ======== UX/ACESSIBILIDADE OTP ========
let isSubmitting = false;

function setOtpState({ loading = false, valid = false } = {}) {
  const grid = otpDialog?.querySelector(".otp-grid");
  if (!grid) return;
  grid.classList.toggle("loading", !!loading);
  grid.classList.toggle("valid", !!valid);
}

function collectOtpCode() {
  return otpInputs().map((i) => (i.value || "").replace(/\D/g, "").slice(0, 1)).join("");
}

function clearOtp() {
  otpInputs().forEach((i) => (i.value = ""));
  setOtpState({ loading: false, valid: false });
  if (otpError) otpError.textContent = "";
  otpInputs()[0]?.focus();
}

// Focus trap dentro do dialog
function trapFocus(e) {
  if (!otpDialog?.open) return;
  if (e.key !== "Tab") return;
  const focusables = otpDialog.querySelectorAll(
    'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
  );
  if (!focusables.length) return;
  const first = focusables[0];
  const last  = focusables[focusables.length - 1];
  if (e.shiftKey && document.activeElement === first) {
    last.focus();
    e.preventDefault();
  } else if (!e.shiftKey && document.activeElement === last) {
    first.focus();
    e.preventDefault();
  }
}

// Eventos dos inputs OTP (digitação, backspace, colagem)
if (otpDialog) {
  document.addEventListener("keydown", (e) => {
    if (!otpDialog.open) return;
    const target = e.target;
    if (!(target instanceof Element) || !otpDialog.contains(target)) return;
    if (e.key === "Escape") {
      otpDialog?.close?.();
      return;
    }
    trapFocus(e);
    if (!(target instanceof HTMLInputElement) || !target.classList.contains("otp")) return;
    if (e.key === "Backspace" && !target.value && target.previousElementSibling instanceof HTMLInputElement) {
      target.previousElementSibling.focus();
    }
  });

  otpDialog.addEventListener("input", (e) => {
    const el = e.target;
    if (!(el instanceof HTMLInputElement) || !el.classList.contains("otp")) return;

    el.value = el.value.replace(/\D/g, "").slice(0, 1);
    if (el.value && el.nextElementSibling instanceof HTMLInputElement) {
      el.nextElementSibling.focus();
    }

    const code = collectOtpCode();
    if (code.length === 6) {
      setOtpState({ valid: true });
    } else {
      setOtpState({ valid: false });
    }
  });

  // backspace para voltar
  // Colagem: distribui os dígitos entre os 6 campos
  otpDialog.addEventListener("paste", (e) => {
    const target = e.target;
    if (!(target instanceof HTMLInputElement) || !target.classList.contains("otp")) return;
    const clip = (e.clipboardData?.getData("text") || "").replace(/\D/g, "").slice(0, 6);
    if (!clip) return;
    e.preventDefault();
    const inputs = otpInputs();
    inputs.forEach((inp, idx) => (inp.value = clip[idx] || ""));
    (inputs.find((i) => !i.value) || inputs[inputs.length - 1]).focus();

    const code = collectOtpCode();
    setOtpState({ valid: code.length === 6 });
  });
}

otpNoCodeLink?.addEventListener("click", (e) => {
  e.preventDefault();
  if (otpResendBtn?.disabled) {
    const t = otpTimer?.textContent || "";
    otpError.textContent = t ? `Aguarde: ${t}` : "Aguarde alguns segundos…";
  } else {
    otpResendBtn?.click();
  }
});

// -------- Cooldown resend --------
let resendCooldown = 60;
let cooldownTimer = null;
const startCooldown = () => {
  clearInterval(cooldownTimer);
  let t = resendCooldown;
  if (otpResendBtn) otpResendBtn.disabled = true;
  if (otpTimer) otpTimer.textContent = t > 0 ? `Reenviar em ${t}s` : "";
  if (t <= 0) {
    if (otpResendBtn) otpResendBtn.disabled = false;
    return;
  }
  cooldownTimer = setInterval(() => {
    t -= 1;
    if (t <= 0) {
      clearInterval(cooldownTimer);
      if (otpResendBtn) otpResendBtn.disabled = false;
      if (otpTimer) otpTimer.textContent = "";
    } else {
      if (otpTimer) otpTimer.textContent = `Reenviar em ${t}s`;
    }
  }, 1000);
};

// -------- Loading no botão --------
const setLoading = (loading, textWhenLoading = "Enviando código…", textDefault = "Criar conta") => {
  if (!criarBtn) return;
  criarBtn.disabled = loading;
  criarBtn.textContent = loading ? textWhenLoading : textDefault;
};

// -------- Util: mostra erros de campo vindos do backend --------
function applyServerFieldErrors(fieldErrors) {
  if (!fieldErrors || typeof fieldErrors !== "object") return;
  Object.entries(fieldErrors).forEach(([field, message]) => {
    const el = byId(field) || byId(field === "dataDeNascimento" ? "dataNascimento" : field);
    if (el) setFieldError(el, String(message));
  });
}

// -------- SUBMIT: valida e OTP/start --------
if (form) {
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (isSubmitting) return;
    setAlert("");
    // limpa erros
    [nome, email, cpf, telefone, senha, confirmar].forEach((el) => setFieldError(el, ""));

    // 1) Descobre canal/destino
    const canalUI = canalSelecionado();       // "email" | "sms"
    const canal   = canalUI.toUpperCase();    // "EMAIL" | "SMS"
    const destino = getDestino(canalUI);

    // 2) Validações
    let ok = true;
    if (!nome?.value?.trim()) { setFieldError(nome, MSG.nome); ok = false; } else setFieldError(nome);
    if (!isEmail(email?.value)) { setFieldError(email, MSG.email); ok = false; } else setFieldError(email);
    if (!isCpf(cpf?.value)) { setFieldError(cpf, MSG.cpf); ok = false; } else setFieldError(cpf);

    const telefoneInformado = Boolean(telefone?.value?.trim());
    if (canal === "SMS" || telefoneInformado) {
      if (!isPhone(telefone?.value)) { setFieldError(telefone, MSG.telefone); ok = false; }
      else setFieldError(telefone);
    } else {
      setFieldError(telefone, "");
    }

    if (!senha?.value) { setFieldError(senha, MSG.senhaVazia); ok = false; }
    else if (!senhaOk(senha.value)) { setFieldError(senha, MSG.senhaPattern); ok = false; } else setFieldError(senha);

    if (confirmar?.value !== senha?.value) { setFieldError(confirmar, MSG.confirmar); ok = false; } else setFieldError(confirmar);

    if (!ok) {
      setAlert("Revise os campos destacados para continuar.", "error");
      const firstInvalid = [nome, email, cpf, telefone, senha, confirmar]
        .find((el) => el?.getAttribute("aria-invalid") === "true");
      firstInvalid?.focus();
      return;
    }

    // 3) Dispara OTP
    try {
      isSubmitting = true;
      setLoading(true, "Enviando código…");
      const { header, token } = getCsrf();

      const resp = await fetch("/api/auth/otp/start", {
        method: "POST",
        credentials: "same-origin",
        headers: {
          "Content-Type": "application/json",
          "Accept": "application/json",
          [header]: token || ""
        },
        body: JSON.stringify({
          canal,                 // agora em UPPERCASE
          destino,
          email: email?.value || null,
          telefone: telefone?.value || null
        })
      });

      const data = await safeParse(resp);

      if (!resp.ok) {
        const detail =
          (typeof data === "object" && (pick(data, "detail", "message", "error"))) ||
          "Não foi possível enviar o código.";
        if (typeof data === "object") {
          const fieldErrors = pick(data, "fieldErrors", "errors", "violations");
          if (fieldErrors) applyServerFieldErrors(fieldErrors);
        }
        setAlert(detail, "error");
        return;
      }

      const deliveryId    = pick(data, "deliveryId", "delivery_id");
      const maskedDestino = pick(data, "maskedDestino", "masked_destino") || destino;
      const cooldownSec   = pick(data, "cooldownSec", "cooldown_sec");

      if (otpDeliveryIdInput) otpDeliveryIdInput.value = deliveryId || "";
      if (otpDestinoEl) otpDestinoEl.textContent = maskedDestino;
      if (cooldownSec != null) resendCooldown = Number(cooldownSec) || 60;

      // abre modal OTP (ou prompt fallback)
      clearOtp();
      if (otpDialog?.showModal) {
        otpDialog.showModal();
        otpInputs()[0]?.focus();
      } else {
        const code = window.prompt("Digite o código recebido (6 dígitos):") || "";
        if (/^\d{6}$/.test(code)) {
          await confirmarOtpEContinuar(code);
        } else {
          setAlert("Código inválido.", "error");
          return;
        }
      }
      startCooldown();
      setAlert(MSG.otpEnviado, "info");
    } catch (err) {
      console.error(err);
      setAlert(MSG.network, "error");
    } finally {
      setLoading(false);
      isSubmitting = false;
    }
  });
}

// -------- Confirmar OTP --------
async function confirmarOtpEContinuar(code) {
  try {
    if (otpConfirmBtn) otpConfirmBtn.disabled = true;
    if (otpError) otpError.textContent = "";
    setOtpState({ loading: true });

    const { header, token } = getCsrf();

    const resp = await fetch("/api/auth/otp/verify", {
      method: "POST",
      credentials: "same-origin",
      headers: { "Content-Type": "application/json", "Accept": "application/json", [header]: token || "" },
      body: JSON.stringify({
        deliveryId: otpDeliveryIdInput?.value || "",
        code
      })
    });

    const data = await safeParse(resp);

    if (!resp.ok) {
      setOtpState({ loading: false, valid: false });
      const reason = pick(data, "reason");
      const msg = reason === "expired" ? MSG.otpExpirado : MSG.otpInvalido;
      if (otpError) otpError.textContent = msg;
      setAlert(msg, "error");
      return;
    }

    const verificationToken = pick(data, "token", "verificationToken", "verification_token");
    if (!verificationToken) {
      setOtpState({ loading: false, valid: false });
      if (otpError) otpError.textContent = "Resposta sem token.";
      return;
    }

    if (otpTokenInput) otpTokenInput.value = verificationToken;
    if (otpDialog?.close) otpDialog.close();

    await criarConta(verificationToken);
  } catch (err) {
    console.error(err);
    if (otpError) otpError.textContent = MSG.network;
  } finally {
    setOtpState({ loading: false });
    if (otpConfirmBtn) otpConfirmBtn.disabled = false;
  }
}

// Clique no confirmar do modal
if (otpConfirmBtn) {
  otpConfirmBtn.addEventListener("click", async (e) => {
    e.preventDefault();
    const code = collectOtpCode();
    if (!/^\d{6}$/.test(code)) {
      if (otpError) otpError.textContent = "Digite os 6 dígitos.";
      return;
    }
    await confirmarOtpEContinuar(code);
  });
}

// Auto-submit quando completar 6 dígitos (enter também confirma)
if (otpDialog) {
  document.addEventListener("keyup", async (e) => {
    const target = e.target;
    if (!otpDialog.open) return;
    if (!(target instanceof Element) || !otpDialog.contains(target)) return;
    const code = collectOtpCode();
    if (code.length === 6 && (e.key === "Enter" || e.key === "NumpadEnter")) {
      await confirmarOtpEContinuar(code);
    }
  });

  // se completou 6 dígitos, envia automaticamente após um pequeno delay (UX)
  otpDialog.addEventListener("input", () => {
    const code = collectOtpCode();
    if (code.length === 6) {
      setOtpState({ valid: true });
      setTimeout(() => {
        // confere se ainda está completo antes de enviar
        const again = collectOtpCode();
        if (again.length === 6) confirmarOtpEContinuar(again);
      }, 120);
    }
  });
}

// -------- Reenviar OTP --------
if (otpResendBtn) {
  otpResendBtn.addEventListener("click", async () => {
    const canalUI = canalSelecionado();
    const canal   = canalUI.toUpperCase();
    const destino = getDestino(canalUI);

    try {
      otpResendBtn.disabled = true;
      const { header, token } = getCsrf();

      const resp = await fetch("/api/auth/otp/start", {
        method: "POST",
        credentials: "same-origin",
        headers: { "Content-Type": "application/json", "Accept": "application/json", [header]: token || "" },
        body: JSON.stringify({
          canal,
          destino,
          previousDeliveryId: otpDeliveryIdInput?.value || ""
        })
      });

      const data = await safeParse(resp);
      if (!resp.ok) {
        if (otpError) otpError.textContent =
          (typeof data === "object" && (pick(data, "detail", "message", "error"))) || "Não foi possível reenviar.";
        return;
      }

      const newDelivery = pick(data, "deliveryId", "delivery_id");
      const newMasked   = pick(data, "maskedDestino", "masked_destino") || destino;
      const newCooldown = pick(data, "cooldownSec", "cooldown_sec");

      if (newDelivery && otpDeliveryIdInput) otpDeliveryIdInput.value = newDelivery;
      if (otpDestinoEl) otpDestinoEl.textContent = newMasked;
      if (newCooldown != null) resendCooldown = Number(newCooldown) || resendCooldown;

      clearOtp();
      startCooldown();
      if (otpError) otpError.textContent = "";
    } catch {
      if (otpError) otpError.textContent = MSG.network;
    } finally {
      // reabilita pelo cooldown
    }
  });
}

// -------- Criação da conta --------
async function criarConta(verificationToken) {
  setAlert("");
  if (!criarBtn) return;

  setLoading(true, "Criando…", "Criar conta");

  const formData = new FormData(form); // inclui otpToken hidden
  const { header, token } = getCsrf();

  try {
    const resp = await fetch(form?.action || "/clientes", {
      method: "POST",
      credentials: "same-origin",
      headers: {
        [header]: token || "",
        "X-OTP-TOKEN": verificationToken || "" // se o backend aceitar por header
      },
      body: formData
    });

    if (resp.redirected) {
      window.location.href = resp.url;
      return;
    }

    const contentType = (resp.headers.get("content-type") || "").toLowerCase();
    if (contentType.includes("text/html")) {
      const html = await resp.text();
      const doc = new DOMParser().parseFromString(html, "text/html");
      const firstGlobal = doc.querySelector(".alert-error");
      const firstField = doc.querySelector(".error-message");
      const msg = (firstGlobal?.textContent || firstField?.textContent || "").trim()
        || "Nao foi possivel concluir o cadastro agora.";
      setAlert(msg, "error");
      setLoading(false, "", "Criar conta");
      return;
    }

    const data = await safeParse(resp);

    if (!resp.ok) {
      if (typeof data === "object") {
        const msg = pick(data, "message", "detail", "error") || "Erro ao criar conta.";
        setAlert(msg, "error");
        const fieldErrors = pick(data, "fieldErrors", "errors", "violations");
        if (fieldErrors) applyServerFieldErrors(fieldErrors);
      } else {
        setAlert("Erro ao criar conta.", "error");
      }
      setLoading(false, "", "Criar conta");
      return;
    }

    setAlert(MSG.contaCriada, "info");
    setTimeout(() => { window.location.href = "/login?novo=1"; }, 600);
  } catch (err) {
    console.error(err);
    setAlert(MSG.network, "error");
    setLoading(false, "", "Criar conta");
  }
}

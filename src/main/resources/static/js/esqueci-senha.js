// /static/js/esqueci-senha.js
(() => {
  const form = document.getElementById('esqueciSenhaForm') || document.querySelector('form');
  const emailInput = document.getElementById('emailOuCpf') || document.getElementById('email');
  const alertBox = document.getElementById('alert');
  const enviarBtn = document.getElementById('enviarBtn') || form?.querySelector('button[type="submit"]');

  const otpDialog = document.getElementById('otpDialog');
  const otpForm = document.getElementById('otpForm');
  const otpConfirmBtn = document.getElementById('otpConfirmBtn');
  const otpResendBtn = document.getElementById('otpResendBtn');
  const otpDestinoEl = document.getElementById('otpDestino');
  const otpError = document.getElementById('otpError');
  const otpTimer = document.getElementById('otpTimer');
  const otpInputs = () => Array.from(otpDialog?.querySelectorAll('.otp') || []);

  let currentDeliveryId = null;
  let verifiedToken = null;
  let userExists = null;
  let resendCooldown = 60;
  let cooldownTimer = null;
  let isVerifying = false;

  const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(String(v || '').trim());
  const pick = (obj, ...keys) => {
    if (!obj || typeof obj !== 'object') {
      return undefined;
    }
    const found = keys.find((k) => Object.prototype.hasOwnProperty.call(obj, k));
    return found ? obj[found] : undefined;
  };

  function setAlert(message = '', type = 'error') {
    if (!alertBox) {
      return;
    }

    if (!message) {
      alertBox.hidden = true;
      alertBox.className = 'alert';
      alertBox.textContent = '';
      return;
    }

    let className = 'alert alert-error';
    if (type === 'info') {
      className = 'alert alert-info';
    } else if (type === 'warning') {
      className = 'alert alert-warning';
    } else if (type === 'success') {
      className = 'alert';
    }

    alertBox.hidden = false;
    alertBox.className = className;
    alertBox.textContent = message;
  }

  function setOtpError(message = '') {
    if (otpError) {
      otpError.textContent = message;
    }
  }

  function getCsrf() {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    return { header, token };
  }

  async function parseJson(resp) {
    try {
      return await resp.json();
    } catch {
      return {};
    }
  }

  function resetOtpInputs() {
    otpInputs().forEach((input) => {
      input.value = '';
    });
  }

  function readOtpCode() {
    return otpInputs()
      .map((input) => String(input.value || '').replace(/\D/g, '').slice(0, 1))
      .join('');
  }

  function startCooldown() {
    if (!otpResendBtn || !otpTimer) {
      return;
    }

    clearInterval(cooldownTimer);
    let remaining = Number(resendCooldown) || 60;

    otpResendBtn.disabled = true;
    otpTimer.textContent = remaining > 0 ? `Reenviar em ${remaining}s` : '';

    cooldownTimer = setInterval(() => {
      remaining -= 1;
      if (remaining <= 0) {
        clearInterval(cooldownTimer);
        otpResendBtn.disabled = false;
        otpTimer.textContent = '';
        return;
      }
      otpTimer.textContent = `Reenviar em ${remaining}s`;
    }, 1000);
  }

  async function verifyCode(explicitCode) {
    if (isVerifying) {
      return;
    }

    const code = explicitCode || readOtpCode();
    const email = String(emailInput?.value || '').trim();

    if (!/^\d{6}$/.test(code)) {
      setOtpError('Digite os 6 digitos.');
      return;
    }

    if (!currentDeliveryId) {
      setOtpError('Sessao OTP expirada. Reenvie o codigo.');
      return;
    }

    try {
      isVerifying = true;
      if (otpConfirmBtn) {
        otpConfirmBtn.disabled = true;
      }
      setOtpError('');

      const { header, token } = getCsrf();
      const resp = await fetch('/api/auth/email-claim/verify', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          [header]: token || ''
        },
        body: JSON.stringify({
          delivery_id: currentDeliveryId,
          code,
          email
        })
      });

      const data = await parseJson(resp);
      if (!resp.ok) {
        const reason = data?.reason;
        if (reason === 'expired') {
          setOtpError('Codigo expirado.');
        } else if (reason === 'invalid' || reason === 'too_many_attempts') {
          setOtpError('Codigo incorreto.');
        } else {
          setOtpError(data?.message || 'Nao foi possivel validar o codigo.');
        }
        return;
      }

      verifiedToken = pick(data, 'token');
      userExists = !!pick(data, 'userExists', 'user_exists');

      if (!verifiedToken) {
        setOtpError('Resposta invalida do servidor. Tente novamente.');
        return;
      }

      otpDialog?.close?.();

      if (!userExists) {
        setAlert('E-mail validado, mas nenhuma conta foi encontrada. Faca seu cadastro.', 'warning');
        setTimeout(() => window.location.assign('/auth/cliente/cadastro'), 900);
        return;
      }

      const novaSenha = window.prompt('E-mail confirmado. Digite a nova senha (minimo 8 caracteres):') || '';
      if (novaSenha.length < 8) {
        setAlert('Senha muito curta. Use no minimo 8 caracteres.', 'error');
        return;
      }

      const resetResp = await fetch('/api/auth/password/reset-otp', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          [header]: token || ''
        },
        body: JSON.stringify({
          token: verifiedToken,
          email,
          nova_senha: novaSenha
        })
      });

      const resetData = await parseJson(resetResp);
      if (!resetResp.ok) {
        setAlert(resetData?.message || 'Falha ao redefinir senha.', 'error');
        return;
      }

      setAlert('Senha redefinida com sucesso. Voce ja pode entrar.', 'success');
      setTimeout(() => window.location.assign('/auth/login'), 700);
    } catch {
      setOtpError('Falha de rede. Tente novamente.');
    } finally {
      isVerifying = false;
      if (otpConfirmBtn) {
        otpConfirmBtn.disabled = false;
      }
    }
  }

  form?.addEventListener('submit', async (event) => {
    event.preventDefault();

    const email = String(emailInput?.value || '').trim();
    if (!isEmail(email)) {
      setAlert('Informe um e-mail valido.', 'error');
      emailInput?.focus();
      return;
    }

    try {
      if (enviarBtn) {
        enviarBtn.disabled = true;
        enviarBtn.textContent = 'Enviando codigo...';
      }
      setAlert('');

      const { header, token } = getCsrf();
      const resp = await fetch('/api/auth/email-claim/start', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          [header]: token || ''
        },
        body: JSON.stringify({ email })
      });

      const data = await parseJson(resp);
      if (!resp.ok) {
        setAlert(data?.message || 'Nao foi possivel enviar o codigo.', 'error');
        return;
      }

      currentDeliveryId = pick(data, 'deliveryId', 'delivery_id') || null;
      resendCooldown = Number(pick(data, 'cooldownSec', 'cooldown_sec')) || 60;

      if (!currentDeliveryId) {
        setAlert('Nao foi possivel iniciar a validacao. Reenvie o codigo.', 'error');
        return;
      }

      resetOtpInputs();
      setOtpError('');
      if (otpDestinoEl) {
        otpDestinoEl.textContent = pick(data, 'maskedDestino', 'masked_destino') || email;
      }

      if (typeof otpDialog?.showModal === 'function') {
        otpDialog.showModal();
        otpInputs()[0]?.focus();
        startCooldown();
        setAlert('Codigo enviado. Verifique seu e-mail.', 'info');
        return;
      }

      const fallbackCode = window.prompt('Digite o codigo de 6 digitos enviado por e-mail:') || '';
      await verifyCode(fallbackCode);
    } catch {
      setAlert('Falha de rede. Tente novamente.', 'error');
    } finally {
      if (enviarBtn) {
        enviarBtn.disabled = false;
        enviarBtn.textContent = 'Enviar codigo';
      }
    }
  });

  otpDialog?.addEventListener('input', (event) => {
    const target = event.target;
    if (!(target instanceof HTMLInputElement) || !target.classList.contains('otp')) {
      return;
    }

    target.value = target.value.replace(/\D/g, '').slice(0, 1);
    if (target.value && target.nextElementSibling instanceof HTMLInputElement) {
      target.nextElementSibling.focus();
    }
  });

  document.addEventListener('keydown', (event) => {
    if (!otpDialog?.open) {
      return;
    }

    const target = event.target;
    if (!(target instanceof Element) || !otpDialog.contains(target)) {
      return;
    }

    if (
      event.key === 'Backspace'
      && target instanceof HTMLInputElement
      && target.classList.contains('otp')
      && !target.value
      && target.previousElementSibling instanceof HTMLInputElement
    ) {
      target.previousElementSibling.focus();
    }
  });

  otpConfirmBtn?.addEventListener('click', async (event) => {
    event.preventDefault();
    await verifyCode();
  });

  otpForm?.addEventListener('submit', async (event) => {
    const submitterValue = event.submitter?.getAttribute('value') || '';
    if (submitterValue === 'cancel') {
      return;
    }

    event.preventDefault();
    await verifyCode();
  });

  otpResendBtn?.addEventListener('click', async () => {
    const email = String(emailInput?.value || '').trim();
    if (!isEmail(email)) {
      setOtpError('E-mail invalido.');
      return;
    }

    let cooldownRestarted = false;
    try {
      otpResendBtn.disabled = true;
      const { header, token } = getCsrf();

      const resp = await fetch('/api/auth/email-claim/start', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          [header]: token || ''
        },
        body: JSON.stringify({
          email,
          previous_delivery_id: currentDeliveryId
        })
      });

      const data = await parseJson(resp);
      if (!resp.ok) {
        setOtpError(data?.message || 'Nao foi possivel reenviar.');
        return;
      }

      currentDeliveryId = pick(data, 'deliveryId', 'delivery_id') || currentDeliveryId;
      if (otpDestinoEl) {
        otpDestinoEl.textContent = pick(data, 'maskedDestino', 'masked_destino') || email;
      }

      resendCooldown = Number(pick(data, 'cooldownSec', 'cooldown_sec')) || 60;
      startCooldown();
      cooldownRestarted = true;
      setOtpError('');
    } catch {
      setOtpError('Falha de rede.');
    } finally {
      if (!cooldownRestarted) {
        otpResendBtn.disabled = false;
      }
    }
  });

  otpDialog?.addEventListener('close', () => {
    setOtpError('');
  });
})();

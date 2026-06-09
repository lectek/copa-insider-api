(() => {
  const passwordPattern = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,128}$/;
  const recoveryCard = document.querySelector("[data-recovery-email]");
  const startButton = document.getElementById("passwordRecoveryStart");
  const recoveryAlert = document.getElementById("passwordRecoveryAlert");
  const resetForm = document.getElementById("passwordRecoveryResetForm");
  const newPasswordInput = document.getElementById("passwordRecoveryNovaSenha");
  const confirmPasswordInput = document.getElementById("passwordRecoveryConfirmarSenha");
  const otpDialog = document.getElementById("otpDialog");
  const otpForm = document.getElementById("otpForm");
  const otpConfirmButton = document.getElementById("otpConfirmBtn");
  const otpResendButton = document.getElementById("otpResendBtn");
  const otpDestination = document.getElementById("otpDestino");
  const otpError = document.getElementById("otpError");
  const otpTimer = document.getElementById("otpTimer");

  if (!recoveryCard || !startButton || !resetForm || !newPasswordInput || !confirmPasswordInput) {
    return;
  }

  const recoveryEmail = (recoveryCard.dataset.recoveryEmail || "").trim();
  const recoveryAvailable = recoveryCard.dataset.recoveryAvailable === "true";
  const otpInputs = () => Array.from(document.querySelectorAll("#otpDialog .otp"));

  let currentDeliveryId = null;
  let verifiedToken = null;
  let resendCooldown = 60;
  let resendTimerHandle = null;
  let verifyingOtp = false;

  function getCsrf() {
    return {
      token: document.querySelector('meta[name="_csrf"]')?.content || "",
      header: document.querySelector('meta[name="_csrf_header"]')?.content || "X-CSRF-TOKEN"
    };
  }

  function setAlert(message = "", variant = "alert-info") {
    if (!recoveryAlert) {
      return;
    }
    recoveryAlert.hidden = !message;
    recoveryAlert.className = message ? `alert ${variant}` : "alert";
    recoveryAlert.textContent = message;
  }

  function setOtpError(message = "") {
    if (otpError) {
      otpError.textContent = message;
    }
  }

  function setFieldError(fieldName, message = "") {
    const node = document.querySelector(`[data-recovery-error-for="${fieldName}"]`);
    if (node) {
      node.textContent = message;
    }
  }

  async function parseResponse(response) {
    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
      return response.json().catch(() => ({}));
    }
    return response.text().catch(() => "");
  }

  function resetOtpInputs() {
    otpInputs().forEach((input) => {
      input.value = "";
    });
  }

  function readOtpCode() {
    return otpInputs()
      .map((input) => String(input.value || "").replace(/\D/g, "").slice(0, 1))
      .join("");
  }

  function startCooldown() {
    if (!otpResendButton || !otpTimer) {
      return;
    }

    window.clearInterval(resendTimerHandle);
    let remaining = Number(resendCooldown) || 60;

    otpResendButton.disabled = true;
    otpTimer.textContent = remaining > 0 ? `Reenviar em ${remaining}s` : "";

    resendTimerHandle = window.setInterval(() => {
      remaining -= 1;
      if (remaining <= 0) {
        window.clearInterval(resendTimerHandle);
        otpResendButton.disabled = false;
        otpTimer.textContent = "";
        return;
      }
      otpTimer.textContent = `Reenviar em ${remaining}s`;
    }, 1000);
  }

  async function startRecovery(previousDeliveryId = null) {
    if (!recoveryAvailable || !recoveryEmail) {
      setAlert("Nao foi possivel identificar um e-mail valido para esta conta.", "alert-warning");
      return;
    }

    const { token, header } = getCsrf();
    try {
      startButton.disabled = true;
      setAlert("");
      setOtpError("");

      const response = await fetch("/api/auth/email-claim/start", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          [header]: token
        },
        body: JSON.stringify({
          email: recoveryEmail,
          previous_delivery_id: previousDeliveryId
        })
      });

      const payload = await parseResponse(response);
      if (!response.ok) {
        setAlert(payload?.message || "Nao foi possivel enviar o codigo.", "alert-error");
        return;
      }

      currentDeliveryId = payload.deliveryId || payload.delivery_id || null;
      resendCooldown = Number(payload.cooldownSec || payload.cooldown_sec || 60);
      verifiedToken = null;
      resetOtpInputs();

      if (otpDestination) {
        otpDestination.textContent = payload.maskedDestino || payload.masked_destino || recoveryEmail;
      }

      if (typeof otpDialog?.showModal === "function") {
        otpDialog.showModal();
        otpInputs()[0]?.focus();
      }

      startCooldown();
      setAlert("Codigo enviado. Verifique seu e-mail.", "alert-info");
    } catch (error) {
      window.console?.error?.("Falha ao iniciar recuperacao de senha.", error);
      setAlert("Falha de rede ao enviar o codigo.", "alert-error");
    } finally {
      startButton.disabled = false;
    }
  }

  async function verifyOtpCode(explicitCode) {
    if (verifyingOtp) {
      return false;
    }

    const code = explicitCode || readOtpCode();
    if (!/^\d{6}$/.test(code) || !currentDeliveryId) {
      setOtpError("Digite os 6 digitos do codigo.");
      return false;
    }

    const { token, header } = getCsrf();
    try {
      verifyingOtp = true;
      otpConfirmButton.disabled = true;
      setOtpError("");

      const response = await fetch("/api/auth/email-claim/verify", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          [header]: token
        },
        body: JSON.stringify({
          delivery_id: currentDeliveryId,
          code,
          email: recoveryEmail
        })
      });

      const payload = await parseResponse(response);
      if (!response.ok) {
        setOtpError(payload?.message || "Nao foi possivel validar o codigo.");
        return false;
      }

      verifiedToken = payload.token || "";
      if (!verifiedToken) {
        setOtpError("Resposta invalida do servidor.");
        return false;
      }

      otpDialog?.close?.();
      resetForm.hidden = false;
      newPasswordInput.focus();
      setAlert("Codigo validado. Agora defina a nova senha.", "alert-info");
      return true;
    } catch (error) {
      window.console?.error?.("Falha ao validar codigo OTP.", error);
      setOtpError("Falha de rede. Tente novamente.");
      return false;
    } finally {
      verifyingOtp = false;
      otpConfirmButton.disabled = false;
    }
  }

  function validateResetForm() {
    let valid = true;
    setFieldError("novaSenha", "");
    setFieldError("confirmarNovaSenha", "");

    if (!newPasswordInput.value) {
      setFieldError("novaSenha", "Informe a nova senha.");
      valid = false;
    } else if (!passwordPattern.test(newPasswordInput.value)) {
      setFieldError(
        "novaSenha",
        "Use 8+ caracteres com maiuscula, minuscula, numero e caractere especial."
      );
      valid = false;
    }

    if (!confirmPasswordInput.value) {
      setFieldError("confirmarNovaSenha", "Confirme a nova senha.");
      valid = false;
    } else if (confirmPasswordInput.value !== newPasswordInput.value) {
      setFieldError("confirmarNovaSenha", "As senhas nao conferem.");
      valid = false;
    }

    return valid;
  }

  async function submitReset(event) {
    event.preventDefault();

    if (!verifiedToken) {
      setAlert("Valide o codigo antes de redefinir a senha.", "alert-warning");
      return;
    }

    if (!validateResetForm()) {
      return;
    }

    const submitButton = resetForm.querySelector('button[type="submit"]');
    const originalLabel = submitButton?.textContent || "Redefinir senha";
    const { token, header } = getCsrf();

    try {
      setAlert("");
      if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = "Redefinindo...";
      }

      const response = await fetch("/api/auth/password/reset-otp", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          [header]: token
        },
        body: JSON.stringify({
          token: verifiedToken,
          email: recoveryEmail,
          nova_senha: newPasswordInput.value
        })
      });

      const payload = await parseResponse(response);
      if (!response.ok) {
        setAlert(payload?.message || "Nao foi possivel redefinir a senha.", "alert-error");
        return;
      }

      setAlert("Senha redefinida com sucesso. Faca login novamente.", "alert-info");
      resetForm.hidden = true;
      window.setTimeout(() => {
        window.location.assign("/auth/login");
      }, 900);
    } catch (error) {
      window.console?.error?.("Falha ao redefinir senha com OTP.", error);
      setAlert("Falha de rede ao redefinir a senha.", "alert-error");
    } finally {
      if (submitButton) {
        submitButton.disabled = false;
        submitButton.textContent = originalLabel;
      }
    }
  }

  function wireOtpInputs() {
    const inputs = otpInputs();
    inputs.forEach((input, index) => {
      input.addEventListener("input", () => {
        input.value = input.value.replace(/\D/g, "").slice(0, 1);
        if (input.value && index < inputs.length - 1) {
          inputs[index + 1].focus();
        }
      });

      input.addEventListener("keydown", (event) => {
        if (event.key === "Backspace" && !input.value && index > 0) {
          inputs[index - 1].focus();
        }
      });
    });
  }

  startButton.addEventListener("click", () => startRecovery(currentDeliveryId));
  resetForm.addEventListener("submit", submitReset);
  otpConfirmButton?.addEventListener("click", async (event) => {
    event.preventDefault();
    await verifyOtpCode();
  });
  otpForm?.addEventListener("submit", async (event) => {
    event.preventDefault();
    await verifyOtpCode();
  });
  otpResendButton?.addEventListener("click", () => startRecovery(currentDeliveryId));
  otpDialog?.addEventListener("close", () => setOtpError(""));

  wireOtpInputs();
})();

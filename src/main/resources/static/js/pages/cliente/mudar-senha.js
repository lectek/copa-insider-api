// ===========================
// mudar-senha.js
// Página: Alterar senha
// ===========================

(() => {
  const form = document.querySelector('form[th\\:action], form[action*="/auth/mudar-senha"]') || document.querySelector('main form');
  const atual = document.getElementById('senhaAtual');
  const nova = document.getElementById('novaSenha');
  const confirmar = document.getElementById('confirmarNovaSenha');

  const MSG = {
    requiredAtual: 'Informe sua senha atual.',
    requiredNova: 'Informe a nova senha.',
    pattern: 'A nova senha precisa ter: 8+ caracteres, letra maiúscula, minúscula, número e caractere especial.',
    confirm: 'As senhas não conferem.',
    capsOn: 'Atenção: Caps Lock ligado.',
    saved: 'Senha alterada com sucesso!',
    wrongAtual: 'Senha atual incorreta.',
    rateLimited: 'Muitas tentativas. Aguarde e tente novamente.',
    server: 'Não foi possível alterar a senha agora.',
    network: 'Falha de rede. Verifique sua conexão.',
  };

  if (!form || !atual || !nova || !confirmar) return;

  // ===== utilitários de UI =====
  const ensureAlert = () => {
    let box = document.getElementById('alert');
    if (!box) {
      box = document.createElement('output');
      box.id = 'alert';
      box.className = 'alert';
      const title = document.querySelector('.title, .form-title, h1');
      (title?.parentElement || form.parentElement).insertBefore(box, form);
    }
    return box;
  };

  const setAlert = (msg = '', type = '') => {
    const box = ensureAlert();
    box.textContent = msg || '';
    box.className = msg ? `alert ${type || 'error'}` : 'alert';
    if (msg) {
      box.setAttribute('aria-live', type === 'success' ? 'polite' : 'assertive');
      box.setAttribute('aria-atomic', 'true');
    } else {
      box.removeAttribute('aria-live');
      box.removeAttribute('aria-atomic');
    }
  };

  const findOrMakeErrBelow = (input) => {
    // tenta p.error-message logo após o campo
    let p = input.parentElement?.querySelector('.error-message');
    if (!p) {
      p = document.createElement('p');
      p.className = 'error-message';
      // se houver um wrapper (ex: .form-group), coloca dentro dele
      const grp = input.closest('.form-group') || input.parentElement || form;
      grp.appendChild(p);
    }
    return p;
  };

  const setFieldError = (input, msg = '') => {
    if (!input) return;
    input.setAttribute('aria-invalid', msg ? 'true' : 'false');
    const p = findOrMakeErrBelow(input);
    p.textContent = msg || '';
  };

  // ===== toggles de mostrar/ocultar =====
  const addToggle = (input) => {
    const wrap = input.parentElement?.classList.contains('input-with-action')
      ? input.parentElement
      : (() => {
          const div = document.createElement('div');
          div.className = 'input-with-action';
          input.parentElement?.insertBefore(div, input);
          div.appendChild(input);
          return div;
        })();

    if (wrap.querySelector('.js-toggle-visibility')) return;

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'btn btn--ghost btn--icon js-toggle-visibility';
    btn.setAttribute('aria-pressed', 'false');
    btn.setAttribute('aria-label', 'Mostrar senha');
    btn.textContent = '👁';
    btn.addEventListener('click', () => {
      const showing = input.type === 'text';
      input.type = showing ? 'password' : 'text';
      btn.setAttribute('aria-pressed', showing ? 'false' : 'true');
      btn.setAttribute('aria-label', showing ? 'Mostrar senha' : 'Ocultar senha');
      input.focus();
    });
    wrap.appendChild(btn);
  };

  [atual, nova, confirmar].forEach(addToggle);

  // ===== checagens =====
const senhaPattern = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,128}$/;

  const validate = () => {
    let ok = true;
    setAlert('');

    // senha atual
    if (!atual.value) {
      setFieldError(atual, MSG.requiredAtual);
      ok = false;
    } else {
      setFieldError(atual, '');
    }

    // nova senha
    if (!nova.value) {
      setFieldError(nova, MSG.requiredNova);
      ok = false;
    } else if (!senhaPattern.test(nova.value)) {
      setFieldError(nova, MSG.pattern);
      ok = false;
    } else {
      setFieldError(nova, '');
    }

    // confirmar
    if (!confirmar.value || confirmar.value !== nova.value) {
      setFieldError(confirmar, MSG.confirm);
      ok = false;
    } else {
      setFieldError(confirmar, '');
    }
    return ok;
  };

  // feedback ao digitar
  const instant = (input, fn) => input?.addEventListener('input', fn);
  instant(atual, () => setFieldError(atual, atual.value ? '' : MSG.requiredAtual));
  instant(nova, () => {
    if (!nova.value) return setFieldError(nova, MSG.requiredNova);
    if (!senhaPattern.test(nova.value)) return setFieldError(nova, MSG.pattern);
    setFieldError(nova, '');
    // sincroniza confirmação, se já preenchida
    if (confirmar.value) {
      setFieldError(confirmar, confirmar.value === nova.value ? '' : MSG.confirm);
    }
  });
  instant(confirmar, () => setFieldError(confirmar, confirmar.value === nova.value ? '' : MSG.confirm));

  // Caps Lock detection
  const attachCapsWarning = (input) => {
    let hint = input.parentElement?.querySelector('.hint-caps');
    if (!hint) {
      hint = document.createElement('small');
      hint.className = 'hint hint-caps';
      hint.style.display = 'none';
      const grp = input.closest('.form-group') || input.parentElement || form;
      grp.appendChild(hint);
    }
    const show = (on) => {
      hint.textContent = on ? MSG.capsOn : '';
      hint.style.display = on ? 'block' : 'none';
    };
    input.addEventListener('keydown', (e) => show(e.getModifierState && e.getModifierState('CapsLock')));
    input.addEventListener('keyup', (e) => show(e.getModifierState && e.getModifierState('CapsLock')));
  };
  [atual, nova, confirmar].forEach(attachCapsWarning);

  // ===== submit =====
  const saveBtn = form.querySelector('button[type="submit"], .btn.btn--primary');

  const setLoading = (loading) => {
    saveBtn && (saveBtn.disabled = loading, saveBtn.textContent = loading ? 'Salvando…' : 'Salvar');
  };

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!validate()) {
      // foca no primeiro inválido
      if (!atual.value) return atual.focus();
      if (!nova.value || !senhaPattern.test(nova.value)) return nova.focus();
      if (!confirmar.value || confirmar.value !== nova.value) return confirmar.focus();
      return;
    }

    setLoading(true);
    setAlert('');

    try {
      const fd = new FormData(form); // inclui _csrf se existir no form
      const resp = await fetch(form.action || '/auth/mudar-senha', {
        method: 'POST',
        body: fd,
      });

      // se o servidor redirecionar (padrão Spring MVC), seguimos
      if (resp.redirected) {
        window.location.assign(resp.url);
        return;
      }

      const ct = resp.headers.get('content-type') || '';
      const data = ct.includes('application/json') ? await resp.json().catch(() => ({})) : await resp.text().catch(() => '');

      if (resp.ok) {
        setAlert(MSG.saved, 'success');
        // limpa campos
        [atual, nova, confirmar].forEach((i) => (i.value = ''));
        // volta para a área do cliente (ajuste se quiser)
        setTimeout(() => { window.location.assign('/cliente'); }, 900);
        return;
      }

      // mapeamento básico de erros
      if (resp.status === 400 || resp.status === 422) {
        // tentamos ler mensagens de validação vindas do backend
        const val = typeof data === 'object' ? (data.validationErrors || data.errors) : null;
        let shownAny = false;
        if (val) {
          if (val.senhaAtual) { setFieldError(atual, String(val.senhaAtual)); shownAny = true; }
          if (val.novaSenha) { setFieldError(nova, String(val.novaSenha)); shownAny = true; }
          if (val.confirmarNovaSenha) { setFieldError(confirmar, String(val.confirmarNovaSenha)); shownAny = true; }
        }
        setAlert(shownAny ? '' : (typeof data === 'object' ? (data.message || MSG.server) : (data || MSG.server)));
        return;
      }

      if (resp.status === 401) {
        setFieldError(atual, MSG.wrongAtual);
        atual.focus();
        atual.select?.();
        return;
      }

      if (resp.status === 429) {
        setAlert(MSG.rateLimited);
        return;
      }

      setAlert(typeof data === 'string' && data ? data : MSG.server);
    } catch (err) {
      console.error(err);
      setAlert(MSG.network);
    } finally {
      setLoading(false);
    }
  });

  // ===== acessibilidade extra: Enter no confirmar envia se válido =====
  confirmar.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      if (!validate()) e.preventDefault();
    }
  });

  // ===== pequena integração com password-meter.js =====
  // Se o seu password-meter usa data-meter e #meterMsg, ele já atualiza sozinho.
  // Aqui, garantimos uma dica mínima caso o script não esteja disponível.
  const meterMsg = document.querySelector('#meterMsg');
  if (meterMsg && !window.__passwordMeterInitialized) {
    nova.addEventListener('input', () => {
      if (!nova.value) { meterMsg.textContent = ''; return; }
      meterMsg.textContent = senhaPattern.test(nova.value) ? 'Boa senha.' : 'Senha fraca (siga os requisitos).';
    });
  }
})();

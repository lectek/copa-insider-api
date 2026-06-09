// =====================
// login.js (Spring Security formLogin)
// =====================

// --- helpers para encontrar elementos com IDs antigos ou novos ---
function pickId(...ids){ for (const id of ids){ const el = document.getElementById(id); if (el) return el; } return null; }

const form        = pickId('loginForm','f');
const usuarioEl   = pickId('usuario');
const senhaEl     = pickId('senha');
const lembrarEl   = pickId('lembrarMe','lembrar');
const alertBox    = pickId('alert','out');
const toggleSenha = pickId('toggleSenha','show');
const forgotBtn   = pickId('forgotBtn');        // opcional
const entrarBtn   = pickId('entrarBtn','btn');
const capsBox     = pickId('caps');             // opcional
const msgBox      = pickId('msg');              // opcional

const MSG = {
  requiredUsuario: 'Informe um e-mail/CPF/usuário.',
  requiredSenha:   'Informe sua senha.',
  invalidPattern:  (senhaEl?.dataset?.patternMsg) || 'Senha fora do padrão.',
};

function setAlert(msg, type = 'error'){
  if (!alertBox) return;
  alertBox.textContent = msg || '';
  alertBox.className = msg ? `alert ${type}` : (alertBox.id === 'out' ? '' : 'alert');
}

function setMsg(msg){ if (msgBox) msgBox.textContent = msg || ''; }

function isEmail(v){
  const s = String(v || '').trim();
  return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(s);
}

// --------- Hardening: contrato do formulário para Spring Security ---------
if (form){
  if (form.getAttribute('action') !== '/login') form.setAttribute('action','/login');
  if ((form.getAttribute('method')||'').toLowerCase() !== 'post') form.setAttribute('method','post');
}
if (usuarioEl && usuarioEl.getAttribute('name') !== 'usuario') usuarioEl.setAttribute('name','usuario');
if (senhaEl   && senhaEl.getAttribute('name')   !== 'senha')   senhaEl.setAttribute('name','senha');
usuarioEl?.setAttribute('autocomplete','username');
senhaEl?.setAttribute('autocomplete','current-password');

// ---------- UX enquanto digita ----------
usuarioEl?.addEventListener('input', () => setAlert(''));
senhaEl?.addEventListener('input', () => {
  const pat = senhaEl.getAttribute('pattern');
  if (!pat) return setAlert('');
  const ok = new RegExp(pat).test(senhaEl.value || '');
  setAlert(ok ? '' : MSG.invalidPattern);
});

// ---------- Mostrar/ocultar senha ----------
toggleSenha?.addEventListener('click', () => {
  if (!senhaEl) return;
  const isPass = senhaEl.type === 'password';
  senhaEl.type = isPass ? 'text' : 'password';
  toggleSenha.textContent = isPass ? 'Ocultar' : 'Mostrar';
  toggleSenha.setAttribute('aria-pressed', String(isPass));
  toggleSenha.setAttribute('aria-label', isPass ? 'Ocultar senha' : 'Mostrar senha');
  senhaEl.focus();
});

// ---------- Aviso de Caps Lock ----------
function capsListener(e){
  const on = e.getModifierState && e.getModifierState('CapsLock');
  if (capsBox) capsBox.style.display = on ? 'block' : 'none';
}
senhaEl?.addEventListener('keyup', capsListener);
senhaEl?.addEventListener('keydown', capsListener);

// ---------- Lembrar último usuário ----------
try {
  const remembered = localStorage.getItem('remember_usuario');
  if (remembered && usuarioEl) {
    usuarioEl.value = remembered;
    if (lembrarEl) lembrarEl.checked = true;
  }
} catch { /* ignore */ }

// ---------- Submit normal do form (sem fetch) ----------
form?.addEventListener('submit', (e) => {
  setAlert('');
  setMsg('Autenticando…');

  const u = (usuarioEl?.value || '').trim();
  const p = senhaEl?.value || '';

  if (!u){
    e.preventDefault();
    setAlert(MSG.requiredUsuario);
    setMsg('Campos obrigatórios.');
    usuarioEl?.focus();
    return;
  }
  if (!p){
    e.preventDefault();
    setAlert(MSG.requiredSenha);
    setMsg('Campos obrigatórios.');
    senhaEl?.focus();
    return;
  }

  // lembra usuário (apenas UX local)
  try {
    if (lembrarEl?.checked && u) localStorage.setItem('remember_usuario', u);
    else localStorage.removeItem('remember_usuario');
  } catch { /* ignore */ }

  // Evita duplo clique
  if (entrarBtn){
    entrarBtn.disabled = true;
    entrarBtn.dataset.loading = '1';
    entrarBtn.textContent = 'Entrando…';
  }
});

// ---------- Atalho Enter (compatível com HTML antigo) ----------
document.addEventListener('keydown', (e) => {
  if (e.key === 'Enter' && form) {
    const a = document.activeElement;
    if (a === senhaEl || a === usuarioEl) form.requestSubmit();
  }
});

// ---------- Esqueci minha senha (opcional) ----------
forgotBtn?.addEventListener('click', (e) => {
  e.preventDefault();
  setAlert('');
  const user = (usuarioEl?.value || '').trim();
  if (!isEmail(user)) {
    setAlert('Digite um e-mail válido para recuperar a senha.');
    usuarioEl?.focus(); usuarioEl?.select?.();
    return;
  }
  const ok = window.confirm(`Vamos abrir a página de alteração de senha para:\n${user}\n\nDeseja continuar?`);
  if (!ok) return;
  window.location.href = `/auth/mudar-senha?email=${encodeURIComponent(user)}`;
});

document.addEventListener('input', (e) => {
  const el = e.target;
  if (el.matches('input[data-meter="true"]')) {
    const msg = document.querySelector(el.getAttribute('data-meter-target'));
    if (!msg) return;

    const v = el.value || '';
    const okLen = v.length >= 8;
    const hasLower = /[a-z]/.test(v);
    const hasUpper = /[A-Z]/.test(v);
    const hasDigit = /\d/.test(v);
    const hasSpecial = /[^A-Za-z0-9]/.test(v);

    const ok = okLen && hasLower && hasUpper && hasDigit && hasSpecial;
    const parts = [];
    if (!okLen) parts.push('8+ chars');
    if (!hasLower) parts.push('minúscula');
    if (!hasUpper) parts.push('MAIÚSCULA');
    if (!hasDigit) parts.push('número');
    if (!hasSpecial) parts.push('especial');

    msg.textContent = ok ? 'Senha forte ✓' : `Falta: ${parts.join(', ')}`;
  }
});

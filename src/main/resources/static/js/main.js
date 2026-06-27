/* main.js — utilitários partilhados pelas páginas de auth */
(function () {
  'use strict';

  /* Loading state em botões com data-loading-text ao submeter o form */
  document.querySelectorAll('form').forEach(function (form) {
    form.addEventListener('submit', function () {
      var btn = form.querySelector('button[type="submit"][data-loading-text]');
      if (!btn) return;
      btn.disabled = true;
      btn.textContent = btn.dataset.loadingText;
    });
  });
}());

// sidebar-toggle.js
(() => {
  const html = document.documentElement;
  const body = document.body;
  const sidebar = document.querySelector('.sidebar');
  if (!sidebar) return;

  // botões que abrem/fecham a sidebar (use .sidebar-toggle no HTML)
  const toggles = Array.from(document.querySelectorAll('.sidebar-toggle, [data-sidebar-toggle]'));

  const open = () => {
    sidebar.classList.add('is-open');
    body.classList.add('no-scroll');   // já existe no seu CSS global
  };

  const close = () => {
    sidebar.classList.remove('is-open');
    body.classList.remove('no-scroll');
  };

  const toggle = () => (sidebar.classList.contains('is-open') ? close() : open());

  toggles.forEach(btn => btn.addEventListener('click', toggle));

  // fecha com ESC
  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && sidebar.classList.contains('is-open')) close();
  });

  // clique fora (área escura gerada pelo ::after cobre 84% → aqui usamos borda da viewport)
  document.addEventListener('click', (e) => {
    if (!sidebar.classList.contains('is-open')) return;
    const clickedInside = sidebar.contains(e.target) || toggles.some(btn => btn.contains(e.target));
    if (!clickedInside) close();
  });
})();

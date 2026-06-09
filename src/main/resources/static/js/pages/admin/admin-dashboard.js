// src/main/resources/static/js/admin-dashboard.js
(() => {
  function ready(fn) {
    if (document.readyState !== 'loading') fn();
    else document.addEventListener('DOMContentLoaded', fn);
  }

  ready(() => {
    const el = document.getElementById('grafico-vendas-dia');
    if (!el || typeof Chart === 'undefined') return;

    const payload = window.RMF_CHART_STATUS || {};
    const labels  = Array.isArray(payload.labels) ? payload.labels : [];
    const data    = Array.isArray(payload.data)   ? payload.data   : [];

    const pretty = {
      ABERTO: 'Aberto',
      AGUARDANDO_PAGAMENTO: 'Aguardando pgto',
      PAGO: 'Pago',
      ENVIADO: 'Enviado',
      ENTREGUE: 'Entregue',
      CANCELADO: 'Cancelado'
    };
    const labelsPretty = labels.map(l => pretty[l] ?? l);

    const ctx = el.getContext('2d');
    new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labelsPretty,
        datasets: [{
          label: 'Pedidos',
          data,
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: { y: { beginAtZero: true, ticks: { precision: 0 } } },
        plugins: {
          legend: { display: false },
          tooltip: { mode: 'index', intersect: false }
        }
      }
    });
  });
})();

(() => {
  function ready(fn) {
    if (document.readyState !== "loading") {
      fn();
      return;
    }
    document.addEventListener("DOMContentLoaded", fn);
  }

  ready(() => {
    const canvas = document.getElementById("grafico-receita-mensal");
    if (!canvas || typeof Chart === "undefined") {
      return;
    }

    const payload = window.RMF_SALES_REPORT || {};
    const labels = Array.isArray(payload.labels) ? payload.labels : [];
    const total = Array.isArray(payload.total) ? payload.total : [];
    const dinheiro = Array.isArray(payload.dinheiro) ? payload.dinheiro : [];
    const pix = Array.isArray(payload.pix) ? payload.pix : [];
    const debito = Array.isArray(payload.debito) ? payload.debito : [];
    const credito = Array.isArray(payload.credito) ? payload.credito : [];

    const money = new Intl.NumberFormat("pt-BR", {
      style: "currency",
      currency: "BRL"
    });

    const chart = new Chart(canvas.getContext("2d"), {
      type: "line",
      data: {
        labels,
        datasets: [
          {
            label: "Total que entrou",
            data: total,
            borderColor: "#0f172a",
            backgroundColor: "rgba(15, 23, 42, 0.08)",
            borderWidth: 3,
            tension: 0.28,
            pointRadius: 2,
            pointHoverRadius: 4
          },
          {
            label: "Dinheiro",
            data: dinheiro,
            borderColor: "#16a34a",
            backgroundColor: "rgba(22, 163, 74, 0.12)",
            borderWidth: 2,
            tension: 0.28,
            pointRadius: 2
          },
          {
            label: "PIX",
            data: pix,
            borderColor: "#0891b2",
            backgroundColor: "rgba(8, 145, 178, 0.12)",
            borderWidth: 2,
            tension: 0.28,
            pointRadius: 2
          },
          {
            label: "Debito",
            data: debito,
            borderColor: "#f59e0b",
            backgroundColor: "rgba(245, 158, 11, 0.12)",
            borderWidth: 2,
            tension: 0.28,
            pointRadius: 2
          },
          {
            label: "Credito",
            data: credito,
            borderColor: "#ef4444",
            backgroundColor: "rgba(239, 68, 68, 0.12)",
            borderWidth: 2,
            tension: 0.28,
            pointRadius: 2
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          mode: "index",
          intersect: false
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback(value) {
                return money.format(Number(value || 0));
              }
            }
          }
        },
        plugins: {
          legend: {
            position: "bottom"
          },
          tooltip: {
            callbacks: {
              label(context) {
                return `${context.dataset.label}: ${money.format(context.parsed.y || 0)}`;
              }
            }
          }
        }
      }
    });

    if (chart) {
      canvas.dataset.chartReady = "true";
    }
  });
})();

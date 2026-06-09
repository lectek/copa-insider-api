document.title = "RedeMaisFarma — API Docs";

// (Opcional) abrir algumas tags automaticamente
setTimeout(() => {
  document.querySelectorAll('.opblock-tag').forEach(t => {
    if (/Produtos|Clientes|Pedidos/i.test(t.textContent)) t.click();
  });
}, 600);

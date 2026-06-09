const API_BASE = "/api/admin/produtos";

document.addEventListener("DOMContentLoaded", () => {
  carregarProdutos();

  document.getElementById("form-produto").addEventListener("submit", async (e) => {
    e.preventDefault();

    const produto = {
      nome: document.getElementById("nome").value,
      descricao: document.getElementById("descricao").value,
      preco: parseFloat(document.getElementById("preco").value),
      imagem: document.getElementById("imagem").value
    };

    const response = await fetch(API_BASE, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(produto)
    });

    if (response.ok) {
      alert("Produto cadastrado!");
      e.target.reset();
      carregarProdutos();
    } else {
      alert("Erro ao cadastrar.");
    }
  });
});

async function carregarProdutos() {
  const container = document.getElementById("lista-produtos");
  container.innerHTML = "<p>Carregando...</p>";

  const response = await fetch(API_BASE);
  const data = await response.json();
  const produtos = Array.isArray(data) ? data : (data.content || []);

  container.innerHTML = produtos.map(prod => `
    <div class="card">
      <img src="${prod.imagem}" alt="${prod.nome}" style="width:100%; max-height:200px; object-fit:cover;" />
      <h2>${prod.nome}</h2>
      <p>${prod.descricao}</p>
      <strong>R$ ${prod.preco.toFixed(2)}</strong>
    </div>
  `).join("");
}

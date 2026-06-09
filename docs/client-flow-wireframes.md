<!-- Makes sense to keep this doc in UTF-8 -->
# Front-End Cliente – Wireframes & Markup Guidance

Este documento sintetiza a estrutura esperada para cada tela do fluxo do cliente (landing → produto → carrinho → checkout → área logada). Use as sugestões quando for moldar componentes/templates e mantenha os mesmos padrões de nomenclatura, grids e blocos reutilizáveis.

## 1. Fundamentos visuais

- **Grid base**: use um container central (`.page-container`) com `max-width: 1200px` e `padding: 0 1.5rem`. Dentro deste container, combine `display: grid` com `grid-template-columns: repeat(auto-fit, minmax(280px, 1fr))` nos blocos multi-coluna.
- **Cartões**: encapsule conteúdo em `.card` com `border-radius: 16px`, `box-shadow: 0 10px 30px rgba(0,0,0,0.05)` e `background: var(--surface)`.
- **Botões**: padronize nos estilos `.btn`, `.btn-primary`, `.btn-secondary` e mantenha estados (hover/focus) com transições suaves.

## 2. Landing / Homepage

### Estrutura
```html
<section class="hero">
  <div class="hero__content">
    <p class="badge">Ofertas da semana</p>
    <h1>Cuide da saúde com economia</h1>
    <p class="lead">Assinaturas, prescrições e atendimento digital em um só lugar.</p>
    <div class="hero__actions">
      <button class="btn btn-primary">Ver ofertas</button>
      <button class="btn btn-secondary">Como funciona</button>
    </div>
  </div>
  <div class="hero__visual">
    <!-- imagem/ilustração -->
  </div>
</section>

<section class="category-grid">
  <article class="card">
    <h3>Medicamentos</h3>
    <p>Farmácia de confiança com entrega agendada.</p>
    <a href="#produtos" class="link-link">Explorar</a>
  </article>
  <!-- repetir para outras categorias -->
</section>
```

## 3. Página de Produto

- Imagens em carrossel (`.product-gallery`), badges de estoque/promoção e seção de atributos. Insira resumo e CTA “Adicionar ao carrinho” fixo no topo quando o usuário rolar.

### Marcação sugerida
```html
<main class="product-page">
  <div class="product-page__media">
    <!-- gallery -->
  </div>
  <div class="product-page__details">
    <p class="product-page__sku">SKU: xxxx</p>
    <h1>Nome do Produto</h1>
    <div class="product-page__price">R$ 29,90 <span>ou 3x de 9,97</span></div>
    <p class="product-page__status">Em estoque</p>
    <div class="product-page__actions">
      <button class="btn btn-primary">Adicionar ao carrinho</button>
      <button class="btn btn-secondary">Comprar agora</button>
    </div>
    <dl class="product-specs">
      <div>
        <dt>Tipagem</dt>
        <dd>Suplemento</dd>
      </div>
      <!-- mais especificações -->
    </dl>
  </div>
</main>
```

## 4. Carrinho

- Use duas colunas: à esquerda a lista de itens (`.cart-items`), à direita o resumo+CTA (`.cart-summary`). Cada item mostra imagem, nome, quantidade e subtotal com botões de ajuste.

### Exemplo
```html
<section class="cart">
  <div class="cart-items">
    <article class="card cart-item">
      <img src="/static/img/produtos/placeholder-generico.png" alt="Produto">
      <div>
        <h2>Produto X</h2>
        <p>Subtotal: R$ 29,90</p>
        <div class="qty-controls">
          <button>-</button>
          <span>2</span>
          <button>+</button>
        </div>
      </div>
      <button class="link-link">Remover</button>
    </article>
    <!-- repetir -->
  </div>
  <aside class="cart-summary card">
    <p>Frete estimado: R$ 12,00</p>
    <p>Total: <strong>R$ 59,90</strong></p>
    <button class="btn btn-primary btn-block">Continuar para pagamento</button>
    <p class="muted">Frete grátis acima de R$ 199</p>
  </aside>
</section>
```

## 5. Checkout

- Separe em cartões sequenciais: (1) Endereço, (2) Pagamento, (3) Revisão. Mantenha um resumo lateral fixo se possível.

### Layout esboço
```html
<form class="checkout">
  <div class="checkout__group card">
    <header><h3>Entrega</h3></header>
    <label>
      CEP
      <input type="text" name="cep" required>
    </label>
    <label>
      Endereço
      <input type="text" name="logradouro" required>
    </label>
  </div>

  <div class="checkout__group card">
    <header><h3>Pagamento</h3></header>
    <div class="payment-options">
      <label class="payment-option">
        <input type="radio" name="pagamento" value="pix" checked>
        PIX
      </label>
      <!-- outros -->
    </div>
    <div class="checkout__installments">
      <label>
        Número do cartão
        <input type="text" name="cartaoNumero">
      </label>
      <!-- validade e cvv -->
    </div>
  </div>

  <button class="btn btn-primary btn-block" type="submit">
    Finalizar pedido
  </button>
</form>

<aside class="checkout-summary card">
  <p>Subtotal</p>
  <p><strong>R$ 59,90</strong></p>
  <p>Frete</p>
  <p><strong>R$ 12,00</strong></p>
  <p>Total</p>
  <p><strong>R$ 71,90</strong></p>
</aside>
```

## 6. Área do Cliente

- Inclua blocos para pedidos recentes, endereços salvos, formas de pagamento e notificações. Use abas ou chips para filtrar pelo status do pedido.

### Exemplos de bloco
```html
<section class="dashboard">
  <div class="dashboard__cards">
    <article class="card">
      <h3>Pedidos recentes</h3>
      <ul class="status-list">
        <li>
          <span class="status status--success">Pago</span>
          <p>#12345 · 2 itens · R$ 89,00</p>
          <a href="#">Ver detalhes</a>
        </li>
        <!-- outros -->
      </ul>
    </article>
    <article class="card">
      <h3>Endereços</h3>
      <p>Casa · Rua ...</p>
      <button class="btn btn-secondary">Adicionar novo</button>
    </article>
  </div>
  <div class="dashboard__extras">
    <article class="card">
      <h3>Notificações personalizadas</h3>
      <p>Ganhe 10% no primeiro pedido do mês.</p>
    </article>
  </div>
</section>
```

## Próximos passos
- Baseado nessas marcas, podemos desenhar componentes reais dentro de `src/main/resources/templates` ou gerar novos blocos em `static/js/pages/cliente`.
- Quando estiver pronto, posso gerar snippets específicos dos templates (Thymeleaf) para substituir os atuais e integrar a nova linguagem visual ao CSS existente.

# Adicionar Nova Secção ao Site Público

Guia para adicionar uma nova secção a uma página existente do site Copa Insider,
mantendo o visual consistente com o design system do `landing.css`.

O utilizador vai indicar: em que página adicionar, o que deve mostrar, e onde na página.

---

## Antes de escrever CSS — verificar se já existe

Lê o ficheiro CSS relevante antes de criar estilos novos:
- `src/main/resources/static/css/landing.css` — estilos globais do site
- `src/main/resources/static/css/<nome-pagina>.css` — estilos específicos da página

Se a classe já existe, reutiliza. Só cria CSS novo se não existir nada equivalente.

---

## Padrões de secção disponíveis

### Secção de conteúdo genérica
```html
<section class="section-block">
    <div class="container">
        <div class="section-header">
            <h2>Título da Secção</h2>
            <p class="section-sub">Subtítulo opcional</p>
        </div>
        <!-- conteúdo -->
    </div>
</section>
```

### Grid de cards (produtos / conteúdo)
```html
<div class="products-grid">
    <div class="product-card">
        <h3 class="product-card__title">Título</h3>
        <p class="product-card__desc">Descrição</p>
        <span class="product-card__price">€X,XX</span>
        <a href="..." class="btn-primary">Comprar</a>
    </div>
</div>
```

### Lista de features / benefícios
```html
<ul class="features-list">
    <li class="feature-item">
        <span class="feature-icon">✓</span>
        <span>Descrição do benefício</span>
    </li>
</ul>
```

### CTA (call to action)
```html
<section class="cta-section">
    <div class="container">
        <h2 class="cta-title">Título apelativo</h2>
        <p class="cta-sub">Subtítulo</p>
        <a href="https://pay.hotmart.com/G106266908X" class="btn-primary btn-lg"
           target="_blank" rel="noopener">
            Obter Copa Pass — €7,99
        </a>
    </div>
</section>
```

---

## Variáveis CSS obrigatórias (landing.css)

Quando criares CSS novo para o site público, usa **sempre** estas variáveis:

```css
.nova-secao {
    background: var(--surface);       /* fundo de secção */
    border: 1px solid var(--border);  /* borda subtil */
    border-radius: var(--radius);     /* 12px */
    color: var(--text);               /* texto principal */
}

.nova-secao__titulo { color: var(--gold); }
.nova-secao__sub    { color: var(--text-muted); }
.nova-secao__btn    { background: var(--gold); color: #000; }
```

**Nunca** escrever `#e8b930` directamente — usar `var(--gold)`.
**Nunca** escrever `#141414` directamente — usar `var(--surface)`.

---

## Onde adicionar CSS novo

Se a secção é específica de uma página:
→ Adicionar ao CSS dessa página (`css/<nome-pagina>.css`)

Se a secção vai aparecer em várias páginas:
→ Adicionar ao `landing.css`

Se é um componente reutilizável (card, badge, etc.):
→ Criar em `css/components/<nome>.css` e importar no `main.css`

---

## Responsive

O site usa mobile-first. Adiciona sempre breakpoint:

```css
@media (max-width: 768px) {
    .nova-secao {
        flex-direction: column;
        padding: 1rem;
    }
}
```

---

## Verificação

1. Ver a página no browser com DevTools → Mobile (375px) e Desktop (1280px)
2. Confirmar que as variáveis CSS estão a ser aplicadas (não valores hardcoded)
3. Confirmar que não há conflito com classes existentes

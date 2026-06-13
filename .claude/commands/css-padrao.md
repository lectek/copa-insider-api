# Padrões CSS — Copa Insider

Regras e padrões obrigatórios para qualquer trabalho de CSS neste projecto.
Lê isto antes de escrever qualquer estilo.

---

## Regra 1 — Nunca escrever `style=""` inline no HTML

**Proibido:**
```html
<div style="background:#0d0d0d;padding:52px 24px;border-bottom:1px solid #1a1a1a;">
```

**Correcto:**
```html
<div class="live-banner">
```
```css
/* landing.css */
.live-banner { background: #0d0d0d; padding: 52px 24px; border-bottom: 1px solid #1a1a1a; }
```

Excepção única: ajustes de layout de uma linha que são mesmo específicos de um elemento e nunca vão mudar (ex: `style="width:36px;height:36px"` numa imagem com dimensões fixas).

---

## Regra 2 — Nunca usar `<style>` dentro de templates HTML

**Proibido:**
```html
<head>
    <style>
        .featured-card { ... }
        .prod-card { ... }
    </style>
</head>
```

**Correcto:**
1. Criar `src/main/resources/static/css/<nome-pagina>.css`
2. Passar o caminho via fragment: `head('Título', null, null, 'loja', '/css/nome-pagina.css')`

CSS em `<style>` não é cacheável pelo browser separadamente do HTML.

---

## Regra 3 — Sempre usar variáveis CSS do sistema

**Site público** — variáveis de `landing.css`:
```css
/* ✅ correcto */
.minha-classe { background: var(--surface); color: var(--text); border: 1px solid var(--border); }

/* ❌ proibido */
.minha-classe { background: #141414; color: #ffffff; border: 1px solid #2a2a2a; }
```

**Admin** — sem variáveis (valores directos de `ci-admin.css`):
```css
/* ✅ correcto no admin — os valores estão definidos em ci-admin.css */
.ci-minha-classe { background: rgba(255,255,255,.04); color: #e2e8f0; }
```

---

## Regra 4 — Onde colocar o CSS novo

| Tipo de estilo | Onde vai |
|---|---|
| Global do site público | `landing.css` |
| Específico de `/loja` | `css/loja.css` |
| Específico de `/calendario` | `css/calendario.css` |
| Específico de `/comparar` | Criar `css/comparar.css` |
| Componente reutilizável (ex: paywall) | `landing.css` na secção correcta |
| Admin global | `ci-admin.css` |
| Admin específico de página | inline no template (páginas admin são simples, não justificam ficheiro extra) |

---

## Regra 5 — Como usar o fragment `public-head`

Todas as páginas públicas novas usam este fragment no `<head>`:

```html
<head th:replace="~{fragments/public-head :: head(
    'Título da Página',
    'Descrição SEO com 120-160 caracteres.',
    '/img/og-copa.webp',
    'identificador-pagina',
    '/css/nome-pagina.css'
)}"></head>
```

- **título** → aparece na tab do browser e no Google
- **descrição** → aparece no Google (120-160 chars)
- **ogImage** → imagem partilhada no WhatsApp/Twitter (usar `null` para default)
- **pagina** → identifica a página para o link activo na navbar (`'loja'`, `'calendario'`, `'selecoes'`, `'comparar'`, `'ranking'`, `'doacao'`)
- **cssExtra** → CSS específico da página, ou `null` se não houver

Não duplicar manualmente: `<link href="fonts.googleapis.com/...">`, `<meta charset>`, `<link landing.css>`.
Tudo isso está no fragment.

---

## Regra 6 — Mobile first, sempre

```css
/* ✅ correcto — escreve primeiro para mobile, depois para desktop */
.cards {
    display: grid;
    grid-template-columns: 1fr;   /* mobile: 1 coluna */
    gap: 16px;
}

@media (min-width: 768px) {
    .cards { grid-template-columns: repeat(2, 1fr); }
}

@media (min-width: 1024px) {
    .cards { grid-template-columns: repeat(3, 1fr); }
}
```

---

## Regra 7 — Nunca hardcodar cores sem token equivalente

Antes de escrever um valor de cor directo, verifica se existe token:

```
Dourado       → var(--gold)          #e8b930 / #f5a623
Fundo página  → var(--bg)            #0a0a0a
Superfície    → var(--surface)       #141414
Card          → var(--card)          #161616
Texto         → var(--text)          #ffffff
Texto apagado → var(--text-muted)    #8a8a8a
Borda         → var(--border)        #2a2a2a
Radius        → var(--radius)        12px
Radius sm     → var(--radius-sm)     8px
```

Se precisas de uma cor que não existe — adiciona-a como token no topo de `landing.css` primeiro.

---

## Checklist antes de fazer commit de CSS

- [ ] Nenhum `style=""` inline novo (excepto dimensões fixas de imagens)
- [ ] Nenhum `<style>` dentro de template HTML
- [ ] Todas as cores usam variáveis CSS quando existe token equivalente
- [ ] Breakpoint mobile adicionado se o layout muda abaixo de 768px
- [ ] CSS colocado no ficheiro correcto (não em `landing.css` o que é específico de uma página)

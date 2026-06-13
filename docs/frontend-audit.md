# Auditoria Frontend — Copa Insider
> Perspectiva de dev frontend profissional · 2026-06-13

---

## Diagnóstico geral

O visual está bem — tema escuro coerente, gold como acento, tipografia Inter limpa.
O problema é de **arquitectura**: cada página gere o seu próprio `<head>`, estilos e navbar
de forma independente. Isso torna manutenção cara e introdução de bugs fácil.

---

## Problemas por prioridade

### 🔴 P1 — Crítico (afecta manutenção e consistência)

#### 1.1 — Sem layout partilhado para páginas públicas
- `landing.html`, `loja.html`, `calendario.html`, `comparar.html`, `factos.html`, etc.
  gerem cada uma o seu `<!DOCTYPE>`, `<head>`, font import e `<body>` separadamente
- O fragment `layout.html` só é usado pelo admin
- **Impacto:** mudar a fonte, adicionar um meta tag ou um CSS global implica editar 10+ ficheiros
- **Fix:** criar `fragments/public-head.html` com o `<head>` partilhado

#### 1.2 — Inter carregada 10+ vezes em paralelo
- Cada página pública tem a sua própria tag `<link href="fonts.googleapis.com/...Inter..."/>`
- Em vez de carregar uma vez e cachear, o browser pede permissão ao Google em cada página
- `landing.css` já define `font-family: 'Inter'` — a font só precisava de estar num ficheiro CSS base
- **Fix:** mover import da Inter para `landing.css` (ou um `site-base.css`), remover das templates

#### 1.3 — 21 inline `style=""` em `landing.html`
- A secção "BANNER AO VIVO" está completamente em `style=""` inline
- Impossível reutilizar, impossível fazer override, não aparece no DevTools como classe
- **Fix:** extrair para classes em `landing.css`

#### 1.4 — CSS da loja em `<style>` dentro do `<head>`
- `loja.html` tem ~120 linhas de CSS num `<style>` tag no início do ficheiro
- Não é cacheável pelo browser separadamente do HTML
- **Fix:** mover para `css/loja.css`

#### 1.5 — Duas navbars diferentes
- `landing.html` usa `<nav class="nav">` com classes `.nav__link`, `.nav__cta`
- `navbar.html` (fragment) usa `<nav class="ci-navbar">` com classes `ci-navbar__link`
- As restantes páginas (calendario, comparar, factos...) têm cada uma a sua própria navbar inline
- O utilizador vê navbars visualmente diferentes consoante a página
- **Fix:** uma navbar, um fragment, usado em todo o lado

---

### 🟡 P2 — Importante (UX e profissionalismo)

#### 2.1 — Menu mobile sem comportamento correcto
- O toggle `onclick="this.closest('nav').classList.toggle('nav--open')"` abre o menu
- Mas não fecha ao clicar fora, não tem animação de transição, não bloqueia scroll
- Em mobile, a navbar ocupa quase todo o ecrã mas sem fundo/overlay
- **Fix:** 10 linhas de JS num ficheiro `js/nav.js`

#### 2.2 — Sem estado activo na navbar pública
- O admin sidebar tem `is-active` via `#request.requestURI`
- A navbar pública não tem nenhum indicador de página actual
- Utilizador não sabe onde está dentro do site
- **Fix:** passar `paginaActual` no model ou usar Thymeleaf para detectar URL

#### 2.3 — `<title>` genérico em quase todas as páginas
- `loja.html`: `<title>Guias — Copa Insider</title>` (hardcoded, sem Thymeleaf)
- `comparar.html`: título genérico sem contexto
- Mau para SEO e para as tabs do browser

#### 2.4 — Favicons incompletos
- `layout.html` referencia `favicon.svg` mas não há `.ico` nem `apple-touch-icon`
- iOS e Android têm comportamento imprevisível sem os meta tags correctos
- **Fix:** 4 linhas no `<head>` partilhado

---

### 🟢 P3 — Melhorias (polish)

#### 3.1 — Sem transições de página
- Navegação entre páginas é abrupta — sem fade, sem skeleton loader
- Fácil de simular com `opacity` transition no `body`

#### 3.2 — Imagens sem `width`/`height` declarados
- `<img src="logo-sm.webp"/>` sem dimensões → layout shift (CLS alto)
- Penaliza Core Web Vitals (LCP, CLS)

#### 3.3 — JS carregado no `<body>` sem `defer`
- Alguns templates carregam scripts sem `defer` ou `async`
- Bloqueia rendering em conexões lentas

#### 3.4 — Sem `og:image` na maioria das páginas
- `landing.html` tem Open Graph completo
- `loja.html`, `calendario.html`, etc. não têm — link partilhado no WhatsApp aparece sem preview

---

## Arquitectura alvo

```
Páginas públicas Copa
└── <head> partilhado (public-head.html fragment)
    ├── meta charset, viewport
    ├── title dinâmico
    ├── Open Graph base
    ├── favicon
    ├── Inter (uma vez, em landing.css ou site-base.css)
    └── landing.css

Navbar pública
└── navbar.html (um único fragment, usado em todas as páginas)
    ├── .nav__brand
    ├── .nav__links (com estado activo)
    └── .nav__toggle (JS partilhado em nav.js)

CSS público
├── landing.css       — reset, variáveis, nav, hero, cards, footer
├── css/loja.css      — página /loja
├── css/calendario.css — página /calendario
├── css/comparar.css  — página /comparar
└── css/[pagina].css  — cada página com os seus overrides

Admin (já correcto)
├── ci-admin.css      — sistema ci-* completo
└── admin-sidebar.html — fragment partilhado
```

---

## Plano de implementação

| Fase | O que fazer | Ficheiros | Impacto |
|---|---|---|---|
| **Agora** | Criar `public-head.html` fragment | 1 ficheiro novo | Elimina duplicação do `<head>` |
| **Agora** | Mover CSS da loja para `loja.css` | mover + apagar `<style>` | Cacheável pelo browser |
| **Agora** | Extrair banner ao vivo de `landing.html` para `landing.css` | 2 ficheiros | 21 inline styles eliminados |
| **Semana** | Unificar navbar pública | 1 fragment + actualizar templates | UX consistente |
| **Semana** | Estado activo na navbar | Thymeleaf + CSS | Orientação do utilizador |
| **Próximo** | `defer` em todos os scripts | todos os templates | Performance |
| **Próximo** | `width`/`height` em todas as imagens | todos os templates | CLS / Core Web Vitals |

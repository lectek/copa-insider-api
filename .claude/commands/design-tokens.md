# Referência — Design Tokens Copa Insider

Quando precisares de cores, tipografia, espaçamento ou sombras no CSS, usa sempre os tokens abaixo.
Nunca escreva valores hardcoded quando existe um token equivalente.

---

## Site público — `landing.css`

Estas variáveis estão disponíveis em todas as páginas do site Copa (home, loja, calendário, etc.).

```css
/* Fundos */
--bg:           #0a0a0a   /* fundo geral da página */
--surface:      #141414   /* containers / secções */
--card:         #161616   /* cards de produto */
--card-hl:      #1a3320   /* card destacado (verde escuro) */
--card-hl-bdr:  #2d6a40   /* borda do card destacado */

/* Cores principais */
--gold:         #e8b930   /* dourado — cor de acção principal */
--gold-hover:   #f0c840   /* dourado hover */
--green:        #2a5c3a   /* verde escuro — sucesso / destaque */
--green-badge:  #1f4a2e   /* fundo de badge verde */
--tag:          #c98a14   /* laranja/âmbar — etiquetas */

/* Texto */
--text:         #ffffff   /* texto principal */
--text-muted:   #8a8a8a   /* texto secundário / subtítulos */
--text-sub:     #b0b0b0   /* texto terciário */

/* Bordas & botões */
--border:       #2a2a2a   /* borda padrão */
--btn-dark:     #1e1e1e   /* fundo botão escuro */
--btn-dark-bdr: #3a3a3a   /* borda botão escuro */

/* Radius */
--radius:       12px
--radius-sm:    8px
```

**Fonte do site público:** `Inter`, system-ui

---

## Admin — `ci-admin.css`

Disponíveis nas páginas `/admin/**`. Sem variáveis CSS — usa valores directos.

| Elemento | Valor |
|---|---|
| Fundo body | `#0b1220` |
| Fundo sidebar / header | `#0f172a` |
| Gold accent (activo, valores) | `#e8b930` |
| Texto principal | `#e2e8f0` |
| Texto secundário | `#94a3b8` |
| Texto labels/secções | `#64748b` |
| Bordas | `rgba(255,255,255,.07)` |
| Hover row tabela | `rgba(255,255,255,.03)` |

---

## Design system base — `tokens.css`

Disponível em **todas** as páginas via `main.css`. Usa em componentes do sistema (farmácia/base).

```css
/* Cores brand */
--color-primary:    #1f6feb
--color-secondary:  #0ea5a4
--color-success:    #16a34a
--color-error:      #ef4444
--color-warning:    #f59e0b

/* Neutros */
--color-neutral-1 a --color-neutral-5  (branco → cinza)
--color-dark:       #0f172a

/* Tipografia */
--font-sans:        'Source Sans 3', system-ui
--font-display:     'DM Sans', system-ui
--font-size-xs a --font-size-2xl  (.75rem → 1.5rem)

/* Espaçamento */
--gap-4 a --gap-64  (.25rem → 4rem)

/* Radius */
--radius-sm: 8px  --radius-md: 12px  --radius-lg: 16px  --radius-full: 999px

/* Sombras */
--shadow-1 a --shadow-3
```

---

## Regras de uso

1. **Página site público** → usa variáveis `landing.css` (`--gold`, `--bg`, `--surface`, etc.)
2. **Página admin** → usa classes `ci-*` e valores directos do `ci-admin.css`
3. **Componente base/shared** → usa tokens de `tokens.css` (`--color-primary`, `--gap-16`, etc.)
4. **Nunca** misturar os dois sistemas na mesma página
5. Se precisas de uma cor que não existe em nenhum token → adiciona o token primeiro, não hardcode

---

## Cores mais usadas no site Copa (cheat sheet)

| Uso | Classe / Valor |
|---|---|
| Botão principal | `background: var(--gold); color: #000` |
| Botão secundário | `background: var(--btn-dark); border: 1px solid var(--btn-dark-bdr)` |
| Texto destaque | `color: var(--gold)` |
| Card padrão | `background: var(--card); border-radius: var(--radius)` |
| Card destacado | `background: var(--card-hl); border: 1px solid var(--card-hl-bdr)` |
| Borda subtil | `border: 1px solid var(--border)` |
| Texto apagado | `color: var(--text-muted)` |

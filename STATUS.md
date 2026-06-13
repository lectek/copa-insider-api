# Copa Insider — Estado Actual do Sistema
> Actualizado: 2026-06-13

---

## Produtos no ar (Hotmart) — TODOS ACTIVOS

| Produto | Slug | ID Hotmart | Preço EUR | Ativo |
|---|---|---|---|---|
| Copa Pass — Dossier Copa 2026 | `copa-pass` | 7915316 | €7,99 | ✅ |
| Copa em 20 Factos | `copa-em-20-factos` | 7915301 | €3,90 | ✅ |
| Guia Portugal | `guia-selecao-portugal` | 7907347 | €3,99 | ✅ |
| Guia Brasil | `guia-selecao-brasil` | 7909721 | €3,99 | ✅ |
| Histórico do Confronto | `historico-confronto` | 7910328 | €1,99 | ✅ |
| Calendário & Comparador | `acesso-calendario-comparador` | — | €3,99 | ❌ descontinuado |

**Copa Pass inclui automaticamente:** acesso a todos os outros.

---

## Fluxo de compra (end-to-end) — FUNCIONAL

```
Utilizador → /loja → clica "Comprar" → pay.hotmart.com/...
  → Hotmart processa → POST /webhooks/hotmart
    → Valida token hottok
    → Guarda em copa_compra
    → Cria conta (se email novo) com senha temporária
    → Grava acessos em copa_acesso (por slug)
    → Envia email (template: mail/compra-confirmada.html)
  → Utilizador recebe email com credenciais
  → Login → acede a /calendario, /comparar, /factos, /guia/{slug}
```

---

## Páginas públicas

| URL | Acesso | Estado |
|---|---|---|
| `/` | Público | ✅ |
| `/loja` | Público | ✅ |
| `/guia/{slug}` | Público (compra via Hotmart) | ✅ |
| `/factos` | Público | ✅ |
| `/selecoes` | Público | ✅ |
| `/selecoes/{slug}` | Público | ✅ |
| `/partida/{id}` | Público | ✅ |
| `/comparar` | Requer Copa Pass | ✅ (gate funcional) |
| `/calendario` | Requer Copa Pass | ✅ (gate funcional) |
| `/doacao` | Público | ✅ |
| `/auth/login` | Público | ✅ |
| `/cadastro` | Público | ✅ |
| `/conta/acessos` | Autenticado | ❌ por implementar |

---

## Páginas admin

| URL | Estado | Notas |
|---|---|---|
| `/admin/dashboard` | ✅ Funcional | |
| `/admin/usuarios` | ✅ Funcional | |
| `/admin/notificacoes` | ✅ Funcional | |
| `/admin/marketing` | ✅ Funcional | |
| `/admin/configuracoes` | ✅ Funcional | |
| `/admin/copa/produtos` | ✅ Funcional | Toggle ativo, editar URL |
| `/admin/copa/compras` | ✅ Funcional | Lista paginada read-only |
| `/admin/copa/acessos` | ✅ Funcional | Pesquisa por email, revogar |

---

## Arquitectura CSS / Frontend

| Ficheiro | Função |
|---|---|
| `landing.css` | Landing page (index) + loja — design de marketing |
| `site-base.css` | Base partilhada: navbar, footer, paywall, variáveis |
| `site-nav.html` | Fragment navbar unificado para páginas secundárias |
| `site-footer.html` | Fragment footer unificado |
| `public-head.html` | Fragment `<head>` com SEO, fonts, css |
| `calendario.css` | Estilos específicos de /calendario |
| `comparar.css` | Estilos específicos de /comparar |
| `factos.css` | Estilos específicos de /factos |
| `selecoes.css` | Estilos específicos de /selecoes |
| `partida.css` | Estilos específicos de /partida |
| `ranking.css` | Estilos específicos de /ranking |
| `rivalidades.css` | Estilos específicos de /rivalidades |
| `loja.css` | Estilos específicos de /loja |
| `ci-admin.css` | Admin completo (classes ci-*) |

---

## Skills Claude Code disponíveis

| Skill | Ficheiro |
|---|---|
| `/copa-status` | `.claude/commands/copa-status.md` |
| `/novo-produto` | `.claude/commands/novo-produto.md` |
| `/acesso-manual` | `.claude/commands/acesso-manual.md` |
| `/debug-webhook` | `.claude/commands/debug-webhook.md` |
| `/nova-pagina-admin` | `.claude/commands/nova-pagina-admin.md` |
| `/design-tokens` | `.claude/commands/design-tokens.md` |
| `/nova-pagina-site` | `.claude/commands/nova-pagina-site.md` |
| `/componentes-admin` | `.claude/commands/componentes-admin.md` |
| `/nova-secao-landing` | `.claude/commands/nova-secao-landing.md` |
| `/email-template` | `.claude/commands/email-template.md` |
| `/css-padrao` | `.claude/commands/css-padrao.md` |

---

## Variáveis de ambiente (Railway)

| Variável | Obrigatória | Estado |
|---|---|---|
| `SPRING_DATASOURCE_URL` | Sim | ✅ |
| `SPRING_DATASOURCE_USERNAME` | Sim | ✅ |
| `SPRING_DATASOURCE_PASSWORD` | Sim | ✅ |
| `HOTMART_HOTTOK` | Sim | ✅ |
| `HOTMART_CLIENT_ID` | Sync API | ⚠️ Verificar |
| `HOTMART_CLIENT_SECRET` | Sync API | ⚠️ Verificar |
| `SPRING_MAIL_HOST` | Email | ✅ |
| `SPRING_MAIL_USERNAME` | Email | ✅ |
| `SPRING_MAIL_PASSWORD` | Email | ✅ |

---

## Commits desde o início da Copa (12 Jun 2026)

| Hash | Descrição |
|---|---|
| `a6c0a7d` | docs: guia completo dos 5 produtos |
| `fe092e1` | refactor: unifica navbar/footer/CSS — 750 linhas removidas |
| `60a3cba` | refactor: frontend — elimina inline styles, nav.js |
| `4162d05` | docs: skills de estilização frontend |
| `1c3dd0d` | docs: skills de desenvolvimento + erros conhecidos |
| `ce2cffa` | feat: admin Copa — páginas produtos/compras/acessos + fix paywalls |
| `2bd6b3d` | fix: dashboard admin em branco |
| `4cdae72` | fix: ROLE_DEVELOPER aceite como admin |

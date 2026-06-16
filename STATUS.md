# Copa Insider — Estado Actual do Sistema
> Actualizado: 2026-06-15

---

## Produtos no ar (Hotmart) — TODOS ACTIVOS

| Produto | Slug | Preço EUR | Ativo |
|---|---|---|---|
| Copa Pass — Dossier Copa 2026 | `copa-pass` | €7,99 | ✅ |
| Copa em 20 Factos | `copa-em-20-factos` | €3,90 | ✅ |
| Guia Portugal | `guia-selecao-portugal` | €3,99 | ✅ |
| Guia Brasil | `guia-selecao-brasil` | €3,99 | ✅ |
| Histórico do Confronto | `historico-confronto` | €1,99 | ✅ |

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
  → Login → redireccionado para /conta/acessos (se tem acessos) ou /cliente/conta
  → /conta/acessos: email de boas-vindas enviado na primeira visita
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
| `/comparar` | Requer Copa Pass | ✅ |
| `/calendario` | Requer Copa Pass | ✅ |
| `/ao-vivo` | Público | ✅ redesenhado (fontes + chat) |
| `/onde-assistir` | Público | ✅ |
| `/doacao` | Público | ✅ |
| `/jogo/{id}/resumo` | Público | ✅ resumo pós-jogo |
| `/jogo/{id}/sala` | Autenticado | ✅ chat por jogo (SSE) |
| `/conta/acessos` | Autenticado | ✅ dashboard de conteúdos |
| `/cliente/conta` | Autenticado | ✅ card premium se tem acessos |
| `/auth/login` | Público | ✅ |
| `/cadastro` | Público | ✅ |

---

## Páginas admin

| URL | Estado |
|---|---|
| `/admin/dashboard` | ✅ |
| `/admin/usuarios` | ✅ |
| `/admin/notificacoes` | ✅ |
| `/admin/marketing` | ✅ |
| `/admin/configuracoes` | ✅ |
| `/admin/copa/produtos` | ✅ toggle ativo, editar URL |
| `/admin/copa/compras` | ✅ lista paginada read-only |
| `/admin/copa/acessos` | ✅ pesquisa por email, revogar |

---

## Features Copa 2026 (em produção)

| Feature | Estado | Notas |
|---|---|---|
| Dados ao vivo | ✅ | ESPN scoreboard + fallback por tempo |
| Detecção de jogo ao vivo | ✅ | fix midnight crossover |
| `/ao-vivo` com fontes gratuitas | ✅ | FIFA+, RTP, CazéTV, Globo, BBC, ITV |
| `/classificacao` fase de grupos | ✅ | tabela por grupo, auto-refresh ao vivo |
| Chat por jogo (`/jogo/{id}/sala`) | ✅ | SSE, requer login |
| Resumo pós-jogo (`/jogo/{id}/resumo`) | ✅ | SofaScore (sem chave API) |
| Experiência pós-compra | ✅ | redirect inteligente + email boas-vindas |
| Countdown próximo jogo em `/ao-vivo` | ✅ | JS em tempo real |
| `/bracket` fase a eliminação | ✅ | colunas por ronda, mata-mata começa 29 Jun |
| `/classificacao` com bracket | ✅ | tabelas por grupo + link bracket |
| Alertas pré-jogo por email | ✅ | scheduler 5 min, janela 30-35 min |
| Novos guias de seleções | ✅ | França, Argentina, Inglaterra, Espanha |
| Palpites + ranking `/palpites` | ✅ | 3 pts exacto, 1 pt resultado |
| Destaques do jogo | ✅ | link YouTube pré-formatado em `/jogo/{id}/resumo` |

---

## Features a implementar (Roadmap Julho 2026)

| Feature | Prioridade | Estado |
|---|---|---|
| Classificação dos grupos | 🔴 Alta | ✅ concluído |
| Bracket fase a eliminação | 🔴 Alta | ✅ concluído |
| Alerta pré-jogo por email | 🟡 Média | ✅ concluído |
| Novos guias de seleções | 🟡 Média | ✅ concluído (França, Argentina, Inglaterra, Espanha) |
| Previsões de resultado | 🟢 Normal | ✅ concluído (palpites + ranking /palpites) |
| Destaques do jogo | 🟢 Normal | ✅ concluído (link YouTube em /resumo) |

Ver detalhes em `PLANO.md`.

---

## Arquitectura CSS / Frontend

| Ficheiro | Função |
|---|---|
| `landing.css` | Landing page (index) + loja |
| `site-base.css` | Base partilhada: navbar, footer, variáveis |
| `ao-vivo.css` | `/ao-vivo` — jogo ao vivo + countdown + fontes |
| `calendario.css` | `/calendario` |
| `comparar.css` | `/comparar` |
| `factos.css` | `/factos` |
| `selecoes.css` | `/selecoes` |
| `partida.css` | `/partida/{id}` |
| `produto.css` | `/guia/{slug}` |
| `conta.css` | `/cliente/conta` + `/conta/acessos` |
| `ci-admin.css` | Admin completo (classes ci-*) |
| `classificacao.css` | `/classificacao` |
| `bracket.css` | `/bracket` |
| `palpites.css` | `/palpites` — ranking de palpites |

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

| Variável | Estado |
|---|---|
| `SPRING_DATASOURCE_URL` | ✅ |
| `SPRING_DATASOURCE_USERNAME` | ✅ |
| `SPRING_DATASOURCE_PASSWORD` | ✅ |
| `HOTMART_HOTTOK` | ✅ |
| `SPRING_MAIL_HOST` | ✅ |
| `SPRING_MAIL_USERNAME` | ✅ |
| `SPRING_MAIL_PASSWORD` | ✅ |
| `APP_WEB_BASE_URL` | ✅ `https://allaboutworldcup2026.com` |

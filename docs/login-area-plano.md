# Área de Login — Inventário e Plano

> Auditoria feita em 2026-06-23. Estado actual do código + bugs + próximos passos.

---

## 1. Mapa de rotas existentes

| Rota | Método | Controller | Template | Estado |
|------|--------|------------|----------|--------|
| `/auth/login` | GET | `LoginController` | `pages/auth/login.html` | ✅ Funcional |
| `/auth/login` | POST | Spring Security | — | ✅ Funcional |
| `/login`, `/entrar` | GET | `AuthWebController` | — (redirect) | ✅ Funcional |
| `/cadastro` | GET/POST | `CadastroController` | `pages/auth/cadastro.html` | ✅ Funcional |
| `/verificar-email` | GET/POST | `VerificarEmailController` | `pages/auth/verificar-email.html` | ✅ Funcional |
| `/verificar-email/reenviar` | POST | `VerificarEmailController` | — (redirect) | ✅ Funcional |
| `/resgatar-codigo` | GET/POST | `ResgateCodigoController` | `pages/auth/resgatar-codigo.html` | ✅ Funcional |
| `/mudar-senha` | GET/POST | `ChangePasswordController` | — | ✅ Funcional (requer login) |
| `/conta/acessos` | GET | `ContaAcessosController` | `pages/site/conta-acessos.html` | ✅ Funcional |
| `/auth/esqueci-senha` | GET/POST | `AuthController` | `auth/esqueci-senha` | ❌ Template ausente |
| `/auth/resetar-senha` | GET/POST | `AuthController` | `auth/resetar-senha` | ❌ Template ausente |
| `/oauth2/authorization/google` | GET | Spring Security OAuth2 | — | ⚠️ Condicional (env var) |
| `/auth/cliente/cadastro` | GET/POST | `AuthRegistrationController` | `pages/auth/cadastro-cliente.html` | 🗂️ Legacy farmácia |

### API REST de autenticação (usada pelo frontend JS)

| Rota | Descrição | Estado |
|------|-----------|--------|
| `POST /api/auth/otp/start` | Inicia OTP (qualquer canal) | ✅ |
| `POST /api/auth/otp/verify` | Verifica código OTP | ✅ |
| `POST /api/auth/email-claim/start` | Inicia OTP + devolve `userExists` | ✅ |
| `POST /api/auth/email-claim/verify` | Verifica + devolve `userExists` | ✅ |
| `POST /api/auth/password/reset-otp` | Reset de senha por OTP | ✅ |
| `POST /api/auth/register/complete-otp` | Registo completo por OTP | ✅ |
| `GET /api/auth/validar-token` | Valida token de reset | ✅ |
| `POST /api/auth/api/esqueci-senha` | Solicita reset (API) | ✅ |

---

## 2. Fluxos concretos

### Fluxo A — Login com email/senha
```
/auth/login (GET)
  → form → POST /auth/login (Spring Security)
    → sucesso → successHandler por role:
        ADMIN      → /admin/dashboard
        CAIXA      → /admin/vendas/rapida
        Copa user  → /conta/acessos
        outros     → /cliente/conta
    → falha → /auth/login?error
```

### Fluxo B — Login com Google
```
/auth/login (GET) → botão Google
  → /oauth2/authorization/google
    → Google consent
    → GoogleOAuth2UserService:
        utilizador existente → actualiza emailVerificado=true
        utilizador novo → cria conta com ROLE_CLIENTE + senha aleatória
    → successHandler (mesmo que Fluxo A)
```
> Activo apenas se `spring.security.oauth2.client.registration.google.client-id` estiver definido.

### Fluxo C — Registo
```
/cadastro (GET) → form (nome, email, senha, [código de acesso])
  → POST /cadastro
    → validações (senhas coincidem, email único)
    → cria conta
    → se código de acesso → tenta resgatar acesso Copa
    → OTP email → /verificar-email
        → POST /verificar-email (código 6 dígitos)
            → activa emailVerificado=true
            → redirect /auth/login
        → reenviar → POST /verificar-email/reenviar
    → se OTP falhar → activa sem verificação + redirect /auth/login
```

### Fluxo D — Esqueci a senha (QUEBRADO)
```
/auth/login → link "Esqueci minha senha"
  → APONTA PARA /esqueci-senha  ← ❌ rota não existe
  → DEVERIA SER /auth/esqueci-senha
    → GET retorna template "auth/esqueci-senha"  ← ❌ pasta errada
    → template real está em pages/auth/ (mas não existe ainda)
```

### Fluxo E — Resgatar código pós-login
```
/resgatar-codigo (GET) → form (código 16 chars)
  → POST /resgatar-codigo
    → CopaAcessoService.resgatar(email, codigo)
    → flash success/error
```

---

## 3. Bugs críticos

### Bug 1 — Link "Esqueci minha senha" aponta para rota errada
**Ficheiro:** `pages/auth/login.html:46`
```html
<!-- actual (errado) -->
<a th:href="@{/esqueci-senha}">Esqueci minha senha</a>

<!-- correcto -->
<a th:href="@{/auth/esqueci-senha}">Esqueci minha senha</a>
```
**Impacto:** Utilizador clica e recebe 404.

### Bug 2 — AuthController aponta para templates inexistentes
**Ficheiro:** `AuthController.java:49,83,98,113,120`
```java
// actual (errado) — pasta templates/auth/ não existe
return "auth/esqueci-senha";
return "auth/resetar-senha";

// correcto
return "pages/auth/esqueci-senha";
return "pages/auth/resetar-senha";
```
**Impacto:** GET `/auth/esqueci-senha` e GET `/auth/resetar-senha` lançam `TemplateInputException` (erro 500).

### Bug 3 — Templates de esqueci/resetar senha não existem
Mesmo corrigindo o Bug 2, os templates `pages/auth/esqueci-senha.html` e `pages/auth/resetar-senha.html` precisam de ser criados.

---

## 4. O que está por fazer (plano)

### P0 — Corrigir bugs críticos (fluxo quebrado)

- [x] Corrigir link em `login.html`: `/esqueci-senha` → `/auth/esqueci-senha`
- [x] Corrigir paths em `AuthController.java`: `"auth/..."` → `"pages/auth/..."` (extraídos em constantes)
- [x] Criar template `pages/auth/esqueci-senha.html` (form: campo email/CPF + submit)
- [x] Criar template `pages/auth/resetar-senha.html` (form: nova senha + confirmar)

### P1 — Melhorias de UX

- [x] Redirects duplos corrigidos: `AuthController` usava `redirect:/login` em vez de `redirect:/auth/login`
- [x] "Continuar com Google" no cadastro — botão condicional igual ao login (`oauth2Enabled`)
- [x] Link "Ativar código de acesso" no estado vazio de `/conta/acessos`
- [x] Loading state nos botões de submit (login, cadastro, esqueci, resetar) via `data-loading-text`
- [x] Criado `main.js` (estava referenciado nos templates mas não existia)

### P2 — Segurança e robustez

- [x] Rate limiting em `/auth/login` — `LoginAttemptService` (10 falhas → bloqueio 15 min por IP) + `LoginRateLimitFilter`
- [x] Honeypot no login — campo `website` oculto; bots que o preenchem são bloqueados silenciosamente
- [x] Sessão concorrente: `maximumSessions(-1)` → `10`
- [x] Rate limiting em `/auth/esqueci-senha` — reutiliza `LoginAttemptService` no `AuthController` (por IP)

### P3 — Funcionalidades futuras

- [x] Login por OTP (sem senha) — `OtpLoginController` (`POST /auth/otp-login`) + tab "Sem senha" no `login.html` com JS inline
- [x] Página de conta Copa — links "Mudar senha / Suporte / Sair" adicionados ao rodapé de `/conta/acessos`
- [ ] "Lembrar dispositivo" (remember-me) — requer migração DB (`persistent_logins`)
- [ ] Magic link por email (alternativa ao OTP numérico)

---

## 5. Código duplicado / legacy a resolver

O `AuthRegistrationController` em `/auth/cliente/cadastro` é da plataforma SaudeMaisFarma — exige CPF, telefone, data de nascimento e OTP obrigatório. O Copa usa o `CadastroController` simples em `/cadastro`. Os dois coexistem sem conflito, mas o `/auth/cliente/cadastro` não deve aparecer no Copa.

A `SecurityMvcConfig` permite as duas rotas publicamente — ok por enquanto.

---

## 6. Ficheiros chave

| Responsabilidade | Ficheiro |
|---|---|
| Configuração Spring Security | `adapters/inbound/web/security/SecurityMvcConfig.java` |
| Login com Google | `adapters/inbound/web/security/GoogleOAuth2UserService.java` |
| Página de login | `adapters/inbound/web/controller/auth/LoginController.java` |
| Registo Copa | `application/controller/CadastroController.java` |
| Esqueci/Resetar senha | `adapters/inbound/web/controller/AuthController.java` |
| Verificar email | `application/controller/VerificarEmailController.java` |
| Resgatar código | `application/controller/ResgateCodigoController.java` |
| Mudar senha | `application/controller/ChangePasswordController.java` |
| Conta/acessos | `adapters/inbound/web/ContaAcessosController.java` |
| Template login | `templates/pages/auth/login.html` |
| Template cadastro Copa | `templates/pages/auth/cadastro.html` |
| Template verificar email | `templates/pages/auth/verificar-email.html` |
| Template resgatar código | `templates/pages/auth/resgatar-codigo.html` |
| Nav links (login/logout) | `templates/fragments/site-nav.html` (fragment `sessionLinks`) |

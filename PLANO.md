# Copa Insider — Plano de Trabalho

## Stack

- Spring Boot 3.x + Thymeleaf + MySQL + Spring Security + Flyway
- Produção: Railway (`prod` profile)
- Repositório: `/home/alex/Área de trabalho/CopadoMundo`
- URL produção: `https://allaboutworldcup2026.com`
- Email: `lektecjava@gmail.com` (Gmail App Password: `josijvrcbzhjqpdf`)

---

## Estado actual das melhorias

### ✅ 1. Reenviar OTP
**Concluído.**

- `VerificarEmailController.java` — reescrito com constantes (`SESS_DELIVERY_ID`, `SESS_EMAIL`, `SESS_MASKED`, `REDIRECT_CADASTRO`, `ERROR_MSG`) e endpoint `@PostMapping("/reenviar")` que chama `otpService.start("email", email, deliveryId)` passando o `previousDeliveryId` (bypass cooldown).
- `verificar-email.html` — substituiu link "Criar conta novamente" por form POST para `/verificar-email/reenviar` com CSRF + botão `ci-btn--ghost`, e link "começar de novo" → `/cadastro`.

---

### ⚠️ 2. Partilhar palpite (Twitter / WhatsApp)
**HTML feito, CSS em falta.**

**O que foi feito:**
Em `partida.html`, na secção de resultado do palpite (após `.palpite-resultado__pts`), foi adicionado:

```html
<th:block th:with="txtShare=${'🎯 Palpitei '
    + meuPalpite.golsCasa + '–' + meuPalpite.golsVisitante
    + ' em ' + partida.bandeiraCasa() + ' ' + partida.selecaoCasa()
    + ' × ' + partida.bandeiraVisitante() + ' ' + partida.selecaoVisitante()
    + ' e ganhei ' + pontosGanhos + ' pts! Copa 2026 → allaboutworldcup2026.com'}">
    <div class="palpite-share">
        <span class="palpite-share__label">Partilhar:</span>
        <a th:href="'https://twitter.com/intent/tweet?text=' + ${#uris.encodeQueryParam(txtShare)}"
           target="_blank" rel="noopener" class="palpite-share__btn palpite-share__btn--twitter">
            𝕏 Twitter
        </a>
        <a th:href="'https://wa.me/?text=' + ${#uris.encodeQueryParam(txtShare)}"
           target="_blank" rel="noopener" class="palpite-share__btn palpite-share__btn--whatsapp">
            WhatsApp
        </a>
    </div>
</th:block>
```

**O que falta — adicionar em `partida.css`** (após `.palpite-ranking-link:hover`):

```css
.palpite-share { display:flex; align-items:center; gap:.6rem; margin-top:1rem; flex-wrap:wrap; }
.palpite-share__label { font-size:.8rem; color:var(--muted); }
.palpite-share__btn {
    display:inline-flex; align-items:center; gap:.35rem;
    padding:.35rem .75rem; border-radius:6px;
    font-size:.82rem; font-weight:600; text-decoration:none;
    transition:opacity .15s;
}
.palpite-share__btn:hover { opacity:.85; }
.palpite-share__btn--twitter  { background:#000; color:#fff; }
.palpite-share__btn--whatsapp { background:#25d366; color:#fff; }
```

---

### ⚠️ 3. Google OAuth2 ("Entrar com Google")
**Backend parcialmente feito. Falta ligar no security config, expor flag, templates e CSS.**

#### O que já foi feito

**`V20260615_02__usuario_cpf_nullable.sql`** (criado):
```sql
ALTER TABLE usuario MODIFY COLUMN cpf VARCHAR(14) NULL;
```

**`UsuarioEntity.java`** — campo `cpf` tornado nullable (linha ~55):
```java
@Size(min = 11, max = 14)
@Column(nullable = true, length = 14, unique = true)
private String cpf;
```

**`GoogleOAuth2UserService.java`** (criado em `adapters/inbound/web/security/`):
- Delega no `DefaultOAuth2UserService` para obter dados do Google
- Procura utilizador por email; se não existe, cria com senha aleatória BCrypt, `emailVerificado=true`, `cpf=null`, role `ROLE_CLIENTE`
- Se existe mas não verificado, activa email e guarda
- Retorna `DefaultOAuth2User` com `email` como principal name (para `authentication.getName()` == email em toda a app)

#### O que falta fazer

**A) `SecurityMvcConfig.java`** — adicionar campo, construtor e `oauth2Login()`:

Adicionar campo:
```java
@Value("${app.security.oauth2.enabled:false}")
private boolean oauth2Enabled;

private final GoogleOAuth2UserService googleOAuth2UserService;
```

Actualizar construtor (adicionar parâmetro `@Autowired(required = false) GoogleOAuth2UserService googleOAuth2UserService`):
```java
public SecurityMvcConfig(Environment env, SessionRegistry sessionRegistry,
                         @Lazy CopaAcessoJPARepository acessoRepo,
                         @Autowired(required = false) GoogleOAuth2UserService googleOAuth2UserService) {
    this.env = env;
    this.sessionRegistry = sessionRegistry;
    this.acessoRepo = acessoRepo;
    this.googleOAuth2UserService = googleOAuth2UserService;
}
```

No método `webChain()`, antes de `http.addFilterAfter(...)`:
```java
if (oauth2Enabled && googleOAuth2UserService != null) {
    http.oauth2Login(oauth -> oauth
        .loginPage("/auth/login")
        .userInfoEndpoint(u -> u.userService(googleOAuth2UserService))
        .successHandler(successHandler())
        .failureUrl("/auth/login?error=oauth")
    );
}
```

No CSP (dentro do `.headers(...)`), actualizar `connect-src` e adicionar `frame-src`:
```java
// trocar a linha connect-src por:
"connect-src 'self' https://accounts.google.com; " +
"frame-src 'self' https://accounts.google.com; " +
```

**B) `application-oauth2.yml`** — adicionar `user-name-attribute: email` ao provider Google:
```yaml
provider:
  google:
    issuer-uri: https://accounts.google.com
    user-name-attribute: email   # ← adicionar esta linha
```

**C) `BrandingModelAdvice.java`** — expor `googleAuthEnabled` a todos os templates:

Adicionar campo (junto aos outros):
```java
@Value("${app.security.oauth2.enabled:false}")
private boolean googleAuthEnabled;
```

No método `addBranding()`, antes do `log.debug(...)`:
```java
model.addAttribute("googleAuthEnabled", googleAuthEnabled);
```

**D) `login.html`** — adicionar antes de `<p class="ci-auth-card__switch">`:
```html
<div th:if="${googleAuthEnabled}" class="ci-oauth-divider">
    <span>ou</span>
</div>
<a th:if="${googleAuthEnabled}"
   href="/oauth2/authorization/google"
   class="ci-btn ci-btn--google ci-btn--full">
    <img src="/images/google-icon.svg" alt="" width="18" height="18"/>
    Entrar com Google
</a>
```

**E) `cadastro.html`** — adicionar antes de `<p class="ci-auth-card__switch">`:
```html
<div th:if="${googleAuthEnabled}" class="ci-oauth-divider">
    <span>ou</span>
</div>
<a th:if="${googleAuthEnabled}"
   href="/oauth2/authorization/google"
   class="ci-btn ci-btn--google ci-btn--full">
    <img src="/images/google-icon.svg" alt="" width="18" height="18"/>
    Criar conta com Google
</a>
```

**F) Ícone Google** — criar `src/main/resources/static/images/google-icon.svg`:
```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48">
  <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
  <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
  <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
  <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.18 1.48-4.97 2.31-8.16 2.31-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
  <path fill="none" d="M0 0h48v48H0z"/>
</svg>
```

**G) `login.css`** — adicionar estilos do botão Google e divisor:
```css
.ci-btn--google {
    background: #fff;
    color: #3c4043;
    border: 1px solid #dadce0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: .5rem;
    font-weight: 500;
}
.ci-btn--google:hover { background: #f8f9fa; }

.ci-oauth-divider {
    display: flex;
    align-items: center;
    gap: .75rem;
    margin: 1rem 0;
    color: var(--muted);
    font-size: .8rem;
}
.ci-oauth-divider::before,
.ci-oauth-divider::after {
    content: '';
    flex: 1;
    height: 1px;
    background: var(--border);
}
```

**H) Variáveis de ambiente a adicionar ao Railway e `.env.local`:**

```
OAUTH_GOOGLE_CLIENT_ID=<client-id-do-google-cloud>
OAUTH_GOOGLE_CLIENT_SECRET=<client-secret-do-google-cloud>
```

E activar profile `oauth2`:
- Railway: `SPRING_PROFILES_ACTIVE=prod,oauth2`
- Local: `SPRING_PROFILES_ACTIVE=docker,oauth2`

**Como obter credenciais Google (gratuito):**
1. Ir a https://console.cloud.google.com
2. Criar projeto ou usar existente
3. APIs & Services → Credentials → Create Credentials → OAuth 2.0 Client ID
4. Application type: **Web application**
5. Authorized redirect URIs:
   - `http://localhost:18090/login/oauth2/code/google`
   - `https://allaboutworldcup2026.com/login/oauth2/code/google`
6. Guardar o **Client ID** e **Client Secret** e colocar nas vars acima

---

### ❌ 4. Email de reset de senha com template
**Não iniciado.**

`PasswordResetController.java` existe. Verificar se usa `MailService.sendTemplate()` ou HTML inline. Se inline, substituir por template `mail/reset-senha.html` com o mesmo estilo de `mail/verificacao-email.html` (código ouro, branding Copa Insider). Variáveis: `link` (URL completo de reset), `ttlMinutos`.

---

### ❌ 5. Cleanup referências farmácia no .env
**Não iniciado.**

Variáveis a corrigir em `.env` e no Railway:

| Variável | Valor actual | Valor correcto |
|---|---|---|
| `JWT_ISSUER` | `RedeMaisFarma` | `CopaInsider` |
| `APP_COMPANY_NAME` | `Rede Mais Farma` | `Copa Insider` |
| `APP_COMPANY_SITE` | `https://local.redemaisfarma` | `https://allaboutworldcup2026.com` |
| `MYSQL_DATABASE` | `redemaisfarma` | `copainsider` (só local) |

Em `application.yml`, logging level:
```yaml
LOGGING_LEVEL_BR_COM_REDEMAISFARMA=TRACE
# → mudar para:
LOGGING_LEVEL_BR_COM_LECTEK_COPAINSIDER=TRACE
```

---

### ❌ 6. Auto-refresh na classificação
**Não iniciado.**

Quando há jogo ao vivo, a tabela de classificação actualiza sem reload. Implementar com `setInterval` a cada 60s que faz fetch a `/api/classificacao` (endpoint JSON a criar) e actualiza a DOM.

---

### ❌ 7. Open Graph dinâmico por partida
**Não iniciado.**

Em `PartidaController.java`, adicionar ao model: `ogTitle`, `ogDescription`, `ogImage`.
No `<head>` da página `partida.html` (ou no layout), adicionar:
```html
<meta property="og:title"       th:content="${ogTitle}"/>
<meta property="og:description" th:content="${ogDescription}"/>
<meta property="og:image"       th:content="${ogImage}"/>
<meta property="og:type"        content="website"/>
<meta property="og:url"         th:content="@{/partida/__${partida.id()}__}"/>
```

---

### ❌ 8. Schema.org para jogos
**Não iniciado.**

Em `partida.html`, adicionar `<script type="application/ld+json">` com `SportsEvent`:
```json
{
  "@context": "https://schema.org",
  "@type": "SportsEvent",
  "name": "Brasil × Portugal",
  "startDate": "2026-06-18T21:00:00-03:00",
  "location": { "@type": "Place", "name": "MetLife Stadium" },
  "competitor": [
    { "@type": "SportsTeam", "name": "Brasil" },
    { "@type": "SportsTeam", "name": "Portugal" }
  ]
}
```
Usar Thymeleaf inline: `[[${partida.selecaoCasa()}]]` dentro do bloco JSON.

---

### ❌ 9. Dashboard de palpites no admin
**Não iniciado.**

Nova página `/admin/palpites`:
- Tabela completa utilizadores + pontos (emails não mascarados)
- Filtro por jogo
- Contagem de palpites por jogo
- Botão exportar CSV
- Ficheiros: novo controller `AdminPalpitesController.java`, template `templates/pages/admin/palpites.html`

---

## Arquivos-chave do projecto

| Arquivo | Papel |
|---|---|
| `SecurityMvcConfig.java` | Cadeia de segurança MVC principal (`@Order(3)`) |
| `GoogleOAuth2UserService.java` | NOVO — serviço OAuth2 Google (find-or-create utilizador) |
| `UsuarioEntity.java` | Entidade utilizador (`cpf` agora nullable) |
| `UsuarioRepository.java` | Repositório com `findByEmailIgnoreCase`, `save` |
| `UsuarioJpaRepository.java` | Repositório com `ativarEmailVerificado`, `existsByEmail` |
| `DbUserDetailsService.java` | `UserDetailsService` para form login |
| `BrandingModelAdvice.java` | `@ControllerAdvice` — atributos globais de modelo (logo, branding, etc.) |
| `application-oauth2.yml` | Config OAuth2 Google (activado com profile `oauth2`) |
| `application.yml` | Config base; grupo `prod-oauth2: prod,docker,oauth2` |
| `OtpServiceJpa.java` | Serviço OTP produção (BD) — usa `MailService.sendTemplate()` |
| `OtpService.java` | Serviço OTP dev/docker (memória) |
| `VerificarEmailController.java` | Verificação OTP + reenvio |
| `Copa2026DataService.java` | Cache in-memory de partidas (refresh 60s da ESPN) |
| `PartidaController.java` | Página jogo individual + palpite |
| `ResumoPartidaController.java` | Resumo pós-jogo + link YouTube destaques |
| `CopaLojaController.java` | Loja + próximo jogo por seleção |
| `PalpiteController.java` | Submissão e listagem de palpites |

## Padrões de código

- **CSS tokens:** `var(--gold)`, `var(--bg2)`, `var(--bg3)`, `var(--muted)`, `var(--border)`, `var(--text)`, `var(--gold-dim)`
- **Botões:** `ci-btn`, `ci-btn--primary`, `ci-btn--ghost`, `ci-btn--full`
- **Flash messages:** `ci-flash--error`, `ci-flash--info`, `ci-flash--success`
- **Forms:** `ci-form`, `ci-form__field`, `ci-form__label`, `ci-form__input`, `ci-form__hint`
- **Auth cards:** `ci-auth-card`, `ci-auth-card__title`, `ci-auth-card__sub`, `ci-auth-card__switch`
- **Sonar:** sem strings duplicadas (usar constantes `private static final String`), sem imports não usados, sem blocos CSS vazios, logs com variável pré-calculada (não como argumento de método)
- **Perfis Spring:** `dev`, `docker`, `prod`, `oauth2` — para Google OAuth2 adicionar `oauth2` ao `SPRING_PROFILES_ACTIVE`

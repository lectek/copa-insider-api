# Criar Nova Página Pública (Site Copa)

Guia para criar uma nova página no site público Copa Insider (`/pages/site/`).

O utilizador vai indicar: URL da página, o que deve mostrar, se requer login ou acesso pago.

---

## 1. Estrutura do template

Cria em `src/main/resources/templates/pages/site/<nome>.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="pt-PT">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title th:text="${tituloPagina + ' — Copa Insider'}">Título — Copa Insider</title>
    <link rel="stylesheet" th:href="@{/css/main.css}"/>
    <link rel="stylesheet" th:href="@{/css/landing.css}"/>
    <!-- Opcional: CSS específico desta página -->
    <!-- <link rel="stylesheet" th:href="@{/css/<nome-pagina>.css}"/> -->
</head>
<body>

<div th:replace="~{fragments/navbar :: navbar}"></div>

<main>
    <!-- conteúdo aqui -->
</main>

<div th:replace="~{fragments/footer :: footer}"></div>

<script th:src="@{/js/main.js}" defer></script>
</body>
</html>
```

---

## 2. Padrão de secções do site Copa

### Hero / cabeçalho de página
```html
<section class="hero-section">
    <div class="container">
        <h1 class="hero-title">Título</h1>
        <p class="hero-sub">Subtítulo descritivo</p>
    </div>
</section>
```

### Card de produto (mesma estrutura da loja)
```html
<div class="product-card">
    <div class="product-card__badge">DESTAQUE</div>
    <h3 class="product-card__title">Nome do produto</h3>
    <p class="product-card__desc">Descrição</p>
    <div class="product-card__price">€7,99</div>
    <a href="https://pay.hotmart.com/..." class="btn-primary" target="_blank">Comprar</a>
</div>
```

### Paywall (acesso pago)
```html
<section class="paywall-section" th:if="${precisaComprar}">
    <div class="paywall-bg">
        <div class="paywall-overlay">
            <div class="paywall-icon">⭐</div>
            <div class="paywall-title">Conteúdo exclusivo Copa Pass</div>
            <div class="paywall-sub">Disponível com o Copa Pass (€7,99).</div>
            <a href="https://pay.hotmart.com/G106266908X" class="paywall-btn"
               target="_blank" rel="noopener">Obter Copa Pass — €7,99</a>
            <span class="paywall-login-link">
                Já compraste? <a th:href="@{/auth/login}">Faz login →</a>
            </span>
        </div>
    </div>
</section>
```

### Paywall (requer login)
```html
<section class="paywall-section" th:if="${precisaLogin}">
    <div class="paywall-bg">
        <div class="paywall-overlay">
            <div class="paywall-icon">🔒</div>
            <div class="paywall-title">Faz login para aceder</div>
            <a th:href="@{/auth/login}" class="paywall-btn">Fazer login</a>
            <span class="paywall-login-link">
                Sem conta? <a th:href="@{/loja}">Obtém acesso →</a>
            </span>
        </div>
    </div>
</section>
```

---

## 3. Controller

Cria ou adiciona a um controller existente em:
`src/main/java/br/com/lectek/copainsider/application/controller/`

```java
@GetMapping("/<rota>")
public String <nome>(Authentication authentication, Model model) {
    // se página com acesso controlado:
    if (authentication == null || "anonymousUser".equals(authentication.getName())) {
        model.addAttribute("precisaLogin", true);
        return "pages/site/<nome>";
    }
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                    || "ROLE_DEVELOPER".equals(a.getAuthority()));
    if (!isAdmin && !acessoService.temAcesso(authentication.getName(), "slug-do-produto")) {
        model.addAttribute("precisaComprar", true);
        return "pages/site/<nome>";
    }
    // popular model com dados...
    return "pages/site/<nome>";
}
```

---

## 4. CSS específico de página (opcional)

Se a página tem estilos próprios, cria `src/main/resources/static/css/<nome-pagina>.css`
e inclui no template. Sempre começa com:

```css
/* ─── <Nome Página> ─────────────────────────────────────── */
/* Usa as variáveis de landing.css: --bg, --gold, --surface, --card, etc. */
```

---

## 5. Segurança

- Páginas públicas: não precisam de configuração — são abertas por omissão
- Páginas com acesso pago: o controlo é feito no controller (ver passo 3)
- Páginas admin: usar `/nova-pagina-admin` em vez desta skill

---

## 6. Verificação

1. `mvn compile -q` — sem erros
2. Confirmar que a rota está acessível publicamente (ou requer as condições certas)
3. Verificar que navbar e footer carregam (`th:replace` correctos)

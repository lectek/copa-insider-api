# Criar Nova Página Admin

Cria uma nova página no painel admin do Copa Insider seguindo o padrão existente.

O utilizador vai indicar: URL da página (ex: `/admin/copa/relatorios`) e o que deve mostrar.

## Padrão a seguir

### Controller

Cria ou adiciona ao controller adequado em:
`src/main/java/br/com/lectek/copainsider/adapters/inbound/web/`

Padrão mínimo:
```java
@GetMapping("/admin/copa/<rota>")
public String <metodo>(Model model) {
    // popular model
    return "pages/admin/copa/<rota>";
}
```

A segurança é automática — `/admin/**` exige `ROLE_ADMIN` ou `ROLE_DEVELOPER`
(ver `SecurityConfig.java`).

### Template

Cria em:
`src/main/resources/templates/pages/admin/copa/<rota>.html`

Cabeçalho obrigatório:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="pt-BR">
<head th:replace="~{fragments/layout :: head('<Título>')}"></head>
<body class="ci-body ci-admin">
<div th:replace="~{fragments/admin-sidebar :: sidebar}"></div>
<div class="ci-admin-main">
    <div th:replace="~{fragments/admin-header :: header('<Título>')}"></div>
    <div th:replace="~{fragments/flash :: flash}"></div>
    <main class="ci-admin-content">
        <!-- conteúdo aqui -->
    </main>
</div>
<script th:src="@{/js/main.js}"></script>
</body>
</html>
```

### Classes CSS disponíveis (ci-admin.css)

- `ci-stats-grid` + `ci-stat-card` + `ci-stat-card__label` + `ci-stat-card__value` — cards de métricas
- `ci-table` — tabela estilizada
- `ci-btn`, `ci-btn--primary`, `ci-btn--ghost`, `ci-btn--sm`, `ci-btn--xs` — botões
- `ci-badge`, `ci-badge--green`, `ci-badge--gray` — etiquetas de estado
- `ci-empty` — mensagem de lista vazia
- `ci-flash`, `ci-flash--success`, `ci-flash--error` — mensagens flash

### Adicionar à sidebar (opcional)

Se a página deve aparecer no menu, editar:
`src/main/resources/templates/fragments/admin-sidebar.html`

Adicionar dentro da secção correcta:
```html
<a th:href="@{/admin/copa/<rota>}" class="ci-sidebar__link"
   th:classappend="${#strings.startsWith(#request.requestURI, '/admin/copa/<rota>')} ? 'is-active'">
    <Nome na sidebar>
</a>
```

## Verificação final

Corre `mvn compile -q` para confirmar que não há erros de compilação.

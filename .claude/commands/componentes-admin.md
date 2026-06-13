# Referência de Componentes Admin (ci-*)

Referência completa das classes `ci-*` disponíveis em `ci-admin.css`.
Usa esta skill quando precisares de construir ou modificar qualquer página do painel admin.

---

## Estrutura base de qualquer página admin

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="pt-BR">
<head th:replace="~{fragments/layout :: head('Título da Página')}"></head>
<body class="ci-body ci-admin">

<div th:replace="~{fragments/admin-sidebar :: sidebar}"></div>

<div class="ci-admin-main">
    <div th:replace="~{fragments/admin-header :: header('Título da Página')}"></div>
    <div th:replace="~{fragments/flash :: flash}"></div>

    <main class="ci-admin-content">
        <!-- CONTEÚDO AQUI -->
    </main>
</div>

<script th:src="@{/js/main.js}"></script>
</body>
</html>
```

---

## Cards de estatística

```html
<div class="ci-stats-grid">
    <div class="ci-stat-card">
        <p class="ci-stat-card__label">Total de X</p>
        <p class="ci-stat-card__value" th:text="${total}">0</p>
    </div>
    <!-- repete para mais cards -->
</div>
```
`ci-stats-grid` usa `auto-fit minmax(180px, 1fr)` — cresce automaticamente.

---

## Tabela

```html
<table class="ci-table">
    <thead>
        <tr>
            <th>Coluna A</th>
            <th>Coluna B</th>
        </tr>
    </thead>
    <tbody>
        <tr th:each="item : ${lista}">
            <td th:text="${item.campoA}">—</td>
            <td th:text="${item.campoB}">—</td>
        </tr>
    </tbody>
</table>

<p th:if="${lista == null or lista.empty}" class="ci-empty">
    Nenhum resultado.
</p>
```

---

## Botões

| Classe | Visual | Uso |
|---|---|---|
| `ci-btn ci-btn--primary` | Dourado sólido | Acção principal |
| `ci-btn ci-btn--ghost` | Transparente | Acção secundária |
| `ci-btn ci-btn--outline` | Borda fina | Alternativa ao ghost |
| `+ ci-btn--sm` | Padding reduzido | Em tabelas e headers |
| `+ ci-btn--xs` | Mínimo | Inline em células |

```html
<button type="submit" class="ci-btn ci-btn--primary">Guardar</button>
<a th:href="@{/admin/copa/compras}" class="ci-btn ci-btn--ghost ci-btn--sm">Ver todos</a>
<button class="ci-btn ci-btn--ghost ci-btn--xs" style="color:#ef4444;border-color:#ef4444">Revogar</button>
```

---

## Badges de estado

```html
<!-- Verde: activo, enviado, sim -->
<span class="ci-badge ci-badge--green">Ativo</span>

<!-- Cinza: inativo, não, desconhecido -->
<span class="ci-badge ci-badge--gray">Inativo</span>

<!-- Amarelo/dourado: pendente, atenção -->
<span class="ci-badge ci-badge--yellow">Pendente</span>

<!-- Dinâmico com Thymeleaf -->
<span class="ci-badge"
      th:classappend="${item.ativo} ? 'ci-badge--green' : 'ci-badge--gray'"
      th:text="${item.ativo} ? 'Ativo' : 'Inativo'">—</span>
```

---

## Flash messages (automáticas via fragment)

O fragment `flash` lê automaticamente `success`, `error` e `info` dos `RedirectAttributes`:

```java
// no controller:
ra.addFlashAttribute("success", "Operação concluída.");
ra.addFlashAttribute("error", "Algo correu mal.");
```

No template já incluído via:
```html
<div th:replace="~{fragments/flash :: flash}"></div>
```

---

## Secção com header e conteúdo

```html
<section class="ci-admin-section">
    <div class="ci-admin-section__header">
        <h2 class="ci-admin-section__title">Título da Secção</h2>
        <a th:href="@{/admin/outra-pagina}" class="ci-btn ci-btn--ghost ci-btn--sm">Ver todos</a>
    </div>
    <!-- tabela ou conteúdo dentro da secção -->
    <table class="ci-table">...</table>
</section>
```

---

## Formulário de pesquisa

```html
<form th:action="@{/admin/copa/acessos}" method="get"
      style="display:flex;gap:.5rem;margin-bottom:1.5rem;align-items:center">
    <input type="text" name="q" th:value="${filtro}"
           placeholder="Pesquisar…"
           style="flex:1;max-width:360px;padding:.5rem .75rem;border-radius:4px;
                  border:1px solid #334155;background:#0f172a;color:#fff;font-size:.9rem"/>
    <button type="submit" class="ci-btn ci-btn--primary ci-btn--sm">Pesquisar</button>
    <a th:if="${filtro != ''}" th:href="@{/admin/copa/acessos}" class="ci-btn ci-btn--ghost ci-btn--sm">Limpar</a>
</form>
```

---

## Paginação

```html
<div th:if="${pagina.totalPages > 1}" style="display:flex;gap:.5rem;margin-top:1.5rem;flex-wrap:wrap">
    <a th:if="${paginaAtual > 0}"
       th:href="@{/admin/copa/lista(page=${paginaAtual - 1})}"
       class="ci-btn ci-btn--ghost ci-btn--sm">← Anterior</a>
    <span th:each="i : ${#numbers.sequence(0, pagina.totalPages - 1)}">
        <a th:href="@{/admin/copa/lista(page=${i})}"
           class="ci-btn ci-btn--sm"
           th:classappend="${i == paginaAtual} ? 'ci-btn--primary' : 'ci-btn--ghost'"
           th:text="${i + 1}">1</a>
    </span>
    <a th:if="${paginaAtual < pagina.totalPages - 1}"
       th:href="@{/admin/copa/lista(page=${paginaAtual + 1})}"
       class="ci-btn ci-btn--ghost ci-btn--sm">Próxima →</a>
</div>
```

O controller passa `Page<T>` como `pagina` e `paginaAtual` (int) no Model.

---

## Modal simples (sem biblioteca externa)

```html
<!-- Botão que abre -->
<button onclick="abrirModal('modal-x')" class="ci-btn ci-btn--ghost ci-btn--sm">Editar</button>

<!-- Modal -->
<div id="modal-x" style="display:none;position:fixed;inset:0;background:rgba(0,0,0,.6);
     z-index:1000;align-items:center;justify-content:center">
    <div style="background:#1e293b;padding:2rem;border-radius:8px;width:480px;max-width:95%">
        <h3 style="margin:0 0 1rem;color:#e8b930">Título do Modal</h3>
        <form method="post" th:action="@{/admin/copa/rota}">
            <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
            <!-- campos aqui -->
            <div style="display:flex;gap:.5rem;margin-top:1rem;justify-content:flex-end">
                <button type="button" onclick="fecharModal('modal-x')" class="ci-btn ci-btn--ghost ci-btn--sm">Cancelar</button>
                <button type="submit" class="ci-btn ci-btn--primary ci-btn--sm">Guardar</button>
            </div>
        </form>
    </div>
</div>

<script>
function abrirModal(id) { document.getElementById(id).style.display = 'flex'; }
function fecharModal(id) { document.getElementById(id).style.display = 'none'; }
// fechar ao clicar fora
document.querySelectorAll('[id^="modal-"]').forEach(function(m) {
    m.addEventListener('click', function(e) { if (e.target === this) fecharModal(this.id); });
});
</script>
```

---

## CSRF em formulários POST

**Todos** os formulários que fazem POST precisam do token CSRF:

```html
<form method="post" th:action="@{/admin/copa/rota}">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
    ...
</form>
```

Sem este campo o Spring Security retorna 403 Forbidden.

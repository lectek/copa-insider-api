# Guia rápido para padronizar telas do Admin

Alvo: aplicar uma casca única (admin-shell + sidebar + header) e componentes consistentes (`admin.css`) em todas as páginas dentro de `templates/pages/admin`.

## Layout base
- Inclua `admin.css` além de `main.css` no `<head>`.
- Use `body` com `admin-page`, envolva o conteúdo em `.admin-shell`, coloque a sidebar via fragmento e um `<main>` com classes `admin-main admin-container stack`, `tabindex="-1"` e `aria-label` descritivo.
- Mantenha a toolbar de página: título (`.section-title`) + descrição `.muted` e ações alinhadas à direita.

Exemplo de esqueleto:
```html
<body class="body-flex admin-page">
<header th:replace="~{fragments/header :: header}"></header>
<div class="admin-shell">
  <aside th:replace="~{fragments/sidebar :: sidebar('SECAO')}"></aside>
  <main id="main-content" class="admin-main admin-container stack" tabindex="-1" aria-label="...">
    <div class="toolbar">
      <div>
        <h1 class="section-title">Título</h1>
        <p class="muted">Descrição curta.</p>
      </div>
      <!-- ações -->
    </div>
    <!-- conteúdo -->
  </main>
</div>
<footer th:replace="~{fragments/footer :: footer}"></footer>
</body>
```

## Componentes e estilos
- Use os componentes de `static/css/admin/components/*` (botões, tabelas, badges, cards, forms, toast). Evite estilizar inline.
- Sidebar: fragmento já atualizado sem ícones quebrados; ative o item correto passando a chave (`admin`, `pedidos`, `produtos`, etc.). Para nova seção, adicione a rota no fragmento e a classe ativa correspondente.
- Tabelas: envolva em `.table-responsive` e use `<table class="table" aria-label="...">`; forneça linha de estado vazio com `.muted`.
- Modais: reaproveite `fragments/modal :: confirm(...)`; ao abrir via JS, mover foco para o botão de confirmar e voltar ao trigger no fechamento.
- Formularios: associe `label for` + `id`; coloque `role="search"` em filtros; mensagens de erro em texto simples e próximos do input.

## Acessibilidade e UX
- Defina `lang="pt-BR"`, título específico e `aria-label` em `<main>`.
- Botões e links interativos devem ter tamanho mínimo de 44px (já coberto pelos estilos). Não deixar ações primárias somente em ícone.
- Toasts: usar `#toast-stack` e JS compartilhado (próxima etapa: criar helper em `static/js/admin/ui.js`).
- Gráficos: usar uma única versão de `admin-dashboard.js`; injetar dados via `window.*` e fornecer `aria-label` nos `<canvas>`.

## Ordem sugerida para migrar o restante
1) Financeiro (assinaturas, pagamentos, gateways) e Relatórios: aplicar shell + toolbar, tabelas com filtros e estados vazios.
2) Marketing/E-mails: corrigir nomes de templates, usar grid/tabelas + editor com preview; manter barra de filtro com `role="search"`.
3) Configurações/agendamentos: agrupar em cards ou acordeões, salvar com feedback de toast; validar campos obrigatórios.
4) Clientes/Usuários/Imagens/Estoque: alinhar com a experiência de produtos (cards, badges de status, ações primárias à direita).

## Limpezas pendentes
- Remover scripts duplicados (já eliminado `pages/admin/admin-dashboard.js`); concentrar JS comum em um módulo.
- Converter qualquer texto corrompido/encoding legado para UTF-8 e revisar labels.
- Unificar uso de cores: preferir tokens em `base/tokens.css` e `admin/theme.css`; evitar cores literais novas.

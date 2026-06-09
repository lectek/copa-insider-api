# Handoff - Embalando (2026-03-26)

## Objetivo do projeto
- Rebrand de farmácia para **Embalando Soluções em Embalagens e Bomboniere**.
- Foco atual: frontend (logo, cores, home/login, conteúdo visual) e publicação em ambiente Railway isolado.

## Decisões tomadas
- Cor principal: **azul**.
- Cor secundária: **branca**.
- Mascote: **Livoninho**.
- Sistema de receita/tarja: desativado para o contexto da Embalando.
- Railway deve ficar isolado do projeto SaudeMaisFarma.

## Dados da empresa (confirmados)
- WhatsApp: `+55 88 9 9732-7916`
- Endereço: `Rua Rosalvo Maia, 137, Centro, Missão Velha`
- Instagram: `@embaland0`

## Status técnico atual
- Ajustes visuais já aplicados em múltiplas telas (logo e identidade Embalando).
- Login foi ajustado para reduzir vermelho e priorizar azul.
- Responsividade foi ajustada com mais uso de unidades relativas.
- Deploy no Railway foi executado, mas houve confusão de vínculo entre projetos em alguns momentos.

## Projeto Railway correto (Embalando)
- Project ID: `94b1087b-2531-4e3f-8b86-88e1866229e5`
- Project Name: `embalando-solucoes-api`
- Service ID: `979cccb0-9c58-4675-a7ff-b9ca5c55573b`
- Service Name: `embalando-solucoes-api`
- URL: `https://embalando-solucoes-api-production.up.railway.app`

## Problema funcional pendente (P0)
- Ainda aparecem produtos mesmo sem cadastro no banco.
- Regra desejada:
  - Se banco vazio -> **não exibir produtos**.
  - Exibir estado vazio amigável.
  - Categorias devem vir de cadastro admin e relacionar produtos reais.

## Diagnóstico inicial de fallback/mock
- Pontos críticos encontrados para auditoria/remoção:
  - `src/main/resources/static/js/home-fallback.js`
  - `src/main/resources/static/js/pages/cliente/index.js`
  - `src/main/java/br/com/redemaisfarma/application/core/produto/ProdutoVitrineService.java` (uso de fallback)

## Próxima execução (Sprint sugerida)
1. Sprint 1: auditoria completa de fallback (arquivo:linha, prioridade P0/P1/P2).
2. Sprint 2: remover fallback no backend e garantir API vazia quando banco vazio.
3. Sprint 3: remover fallback no frontend e renderizar estado vazio.
4. Sprint 4: consolidar categorias no admin + vínculo com produto.
5. Sprint 5: migrations/integração e testes.

## Observação de arquitetura
- Recomendação de médio prazo: modelo **core + tenant** (um código-base com identidade por configuração).
- Alternativa atual: manter 2 repositórios sincronizados por merge/upstream (mais sujeito a conflito).


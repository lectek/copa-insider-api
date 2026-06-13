# Copa Insider — Estado do Sistema

Analisa o estado actual do projeto Copa Insider e produz um relatório conciso.

Faz o seguinte:

1. Lê `PLANO.md` e `STATUS.md` na raiz do projecto para contexto.
2. Corre `git log --oneline -10` para ver os commits mais recentes.
3. Corre `git status` para ver se há alterações locais não commitadas.
4. Corre `git log origin/main..HEAD --oneline` para ver commits por deployar.
5. Verifica se existem os ficheiros críticos:
   - `src/main/resources/static/css/ci-admin.css`
   - `src/main/resources/templates/pages/admin/copa/produtos.html`
   - `src/main/resources/templates/pages/admin/copa/compras.html`
   - `src/main/resources/templates/pages/admin/copa/acessos.html`
6. Verifica se existem controllers para `/conta/acessos` (pesquisa por `ContaController` ou `/conta/acessos` no código).

Apresenta o resultado neste formato:

```
## Estado Copa Insider — <data de hoje>

### Deploy
- Commits locais por deployar: X
- Últimos commits: ...

### Páginas
✅/❌ /admin/dashboard
✅/❌ /admin/copa/produtos
✅/❌ /admin/copa/compras
✅/❌ /admin/copa/acessos
✅/❌ /conta/acessos

### A fazer (por prioridade)
1. ...
2. ...
```

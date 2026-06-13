# Erros Conhecidos — Copa Insider

Registo dos bugs mais chatos que já encontrámos, com causas e como resolver.
Actualizar sempre que aparecer um erro novo relevante.

---

## 1. Admin dashboard em branco após login

**Sintoma:** Login funciona, redirige para `/admin/dashboard`, mas a página aparece
quase vazia — só aparece "Copa Insider / Admin / Visão Geral" e mais nada.

**Causa:** `#httpServletRequest` não é um objecto válido no Thymeleaf 3.x.
O utility object correcto é `#request`. Quando o Thymeleaf encontra `#httpServletRequest`
lança um `TemplateProcessingException` a meio do rendering do `admin-sidebar.html`,
o servidor fecha a resposta HTTP no meio — o browser recebe HTML truncado.
Os 3 elementos que aparecem são exactamente os que estão **antes** do primeiro
`th:classappend` que usa `#httpServletRequest`.

**Fix aplicado:** Commit `2bd6b3d` — substituídas todas as ocorrências por `#request.requestURI`
em `fragments/admin-sidebar.html`.

**Como detectar no futuro:** Se uma página admin aparecer truncada (elementos do topo
visíveis, resto em branco), suspeitar de uma expressão Thymeleaf que lança excepção
a meio do fragment. Ver logs do Railway — vai aparecer `TemplateProcessingException`.

---

## 2. Página admin sem estilo visual (CSS ausente)

**Sintoma:** Página admin carrega mas está sem estilos — sem cores, sem layout, sem sidebar visível.
Todas as classes `ci-*` (ci-sidebar, ci-stat-card, ci-btn, etc.) sem efeito.

**Causa:** O ficheiro `ci-admin.css` não existia. O `main.css` importa `layout/sidebar.css`
que tem classes `.sidebar` (da base farmácia), mas nenhuma das classes `ci-*` usadas
no admin Copa Insider.

**Fix aplicado:** Commit `2bd6b3d` — criado `src/main/resources/static/css/ci-admin.css`
com todas as classes `ci-*` e link adicionado em `fragments/layout.html`.

**Como detectar no futuro:** DevTools do browser → Network → verificar se `ci-admin.css`
carrega com 200. Se der 404, o ficheiro não existe ou não está no classpath correcto.

---

## 3. Controller e template com nomes de variáveis incompatíveis

**Sintoma:** Página admin carrega mas está vazia ou mostra `0` em tudo, mesmo havendo dados.

**Causa:** O controller mete no `Model` variáveis com um nome (ex: `painelAdmin`)
e o template tenta ler variáveis com outro nome (ex: `totalUsuarios`).
O Thymeleaf não lança erro — simplesmente avalia como `null` e mostra o valor por omissão.

**Exemplo concreto:** `dashboard.html` esperava `totalUsuarios`, `totalCompras`, etc.,
mas o controller metia `painelAdmin`, `ultimosPedidos`, `metaVendaPainel`.

**Fix aplicado:** Commit `2bd6b3d` — controller e template reescritos em sincronia.

**Como detectar no futuro:** Se valores aparecem como `0` ou `—` mas há dados na BD,
verificar com DevTools → Response se o HTML tem os valores correctos.
Se não tiver, o problema é no controller (não passa os dados).
Se tiver, o problema é no CSS/JS (não renderiza).

---

## 4. Utilizador comprou mas não tem acesso

**Sintoma:** Utilizador pagou no Hotmart, recebeu email de confirmação (ou não),
mas ao fazer login não consegue aceder ao conteúdo — continua a ver o paywall.

**Causas possíveis (por ordem de probabilidade):**

### 4a. `HOTMART_HOTTOK` errado no Railway
O webhook chega mas a validação do token falha silenciosamente — o controller
rejeita o pedido e não grava nada.

**Como verificar:** Railway → Logs → pesquisar por `hottok` ou `webhook` na janela
temporal da compra.

**Fix:** Railway → Variables → corrigir `HOTMART_HOTTOK` para coincidir com o valor
no painel Hotmart → Webhooks → Configuração.

### 4b. Webhook não chegou
Hotmart não enviou o webhook (timeout, URL errada, ou modo teste activo).

**Como verificar:** Painel Hotmart → Ferramentas → Webhooks → ver histórico de envios.

**Fix:** Re-enviar o webhook manualmente pelo painel Hotmart, ou usar `/acesso-manual`.

### 4c. Acesso concedido mas para slug errado
O `produtoNome` que Hotmart envia no webhook pode não coincidir exactamente com
o slug que o sistema espera. Ver `HotmartWebhookController.java` — lógica de mapeamento
nome → slug.

**Como verificar:**
```sql
SELECT * FROM copa_compra WHERE comprador_email = 'email@exemplo.com';
SELECT * FROM copa_acesso WHERE email = 'email@exemplo.com';
```
Se `copa_compra` tem registo mas `copa_acesso` está vazio, é um erro a gravar o acesso.

---

## 5. Paywall do Calendário/Comparador manda para loja sem produto visível

**Sintoma:** Utilizador sem Copa Pass clica "Ver na loja" no paywall do calendário,
vai para `/loja`, mas não vê nenhum produto de "acesso ao calendário" para comprar.

**Causa:** O produto `acesso-calendario-comparador` está com `ativo = 0` (não aparece na loja).
O paywall mandava para `/loja?tipo=ACESSO_FERRAMENTAS` que filtrava por esse produto inativo.

**Fix aplicado:** Commit `ce2cffa` — botão do paywall agora aponta directamente
para `https://pay.hotmart.com/G106266908X` (Copa Pass), sem passar pela loja.

---

## 6. App não arranca localmente — "Nenhuma URL de banco foi definida"

**Sintoma:** Ao correr `mvn spring-boot:run` localmente, a app falha com erro de datasource.

**Causa:** `MySqlDataSourceConfig` lê a variável `SPRING_DATASOURCE_URL`.
Localmente essa variável não existe — só existe no Railway.

**Fix (local):** Criar ficheiro `.env` ou exportar as variáveis antes de correr:
```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/copainsider
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=...
mvn spring-boot:run
```
Ou usar perfil de teste com H2: `mvn spring-boot:run -Dspring-boot.run.profiles=test`
(verificar se existe `application-test.properties` com H2 configurado).

---

## 7. Sidebar admin mostra link activo errado (ou nunca activo)

**Sintoma:** Ao navegar no admin, o link da sidebar que deveria estar destacado
não fica destacado (ou o errado fica).

**Causa:** A expressão `th:classappend` usa `#request.requestURI` que devolve o path
sem query string. Se o URL tiver parâmetros (ex: `/admin/copa/acessos?email=x`),
o `startsWith` ainda funciona correctamente. O problema típico é usar o prefixo errado
ou esquecer a barra inicial.

**Fix:** Garantir que o prefixo no `startsWith` corresponde exactamente ao início da rota,
com barra, ex: `'/admin/copa/produtos'`.

---

## Regras gerais de diagnóstico

1. **Página truncada / em branco** → procurar `TemplateProcessingException` nos logs
2. **Valores a zero sem razão** → variáveis do Model têm nome diferente do que o template espera
3. **CSS sem efeito** → verificar se o ficheiro `.css` existe e é referenciado no `layout.html`
4. **Acesso não concedido** → ver `copa_compra` e `copa_acesso` na BD; verificar hottok
5. **App não arranca** → variáveis de ambiente em falta; ver `application.properties`

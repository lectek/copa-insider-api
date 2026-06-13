# Debug — Webhook Hotmart não processou compra

Diagnóstico guiado quando uma compra no Hotmart não gerou acesso no Copa Insider.

## Passo 1 — Verificar se o webhook chegou

O controller está em:
`src/main/java/br/com/lectek/copainsider/adapters/inbound/web/webhook/HotmartWebhookController.java`

Pede ao utilizador: **qual é o email do comprador e/ou o ID de transação Hotmart**.

## Passo 2 — Gerar SQL de verificação

Produz este SQL para o utilizador correr no Railway:

```sql
-- Ver se a compra foi registada
SELECT * FROM copa_compra WHERE comprador_email = '<email>' ORDER BY criado_em DESC LIMIT 5;

-- Ver se o acesso foi concedido
SELECT * FROM copa_acesso WHERE email = '<email>' ORDER BY concedido_em DESC;

-- Ver se o utilizador foi criado
SELECT id, nome, email, created_at FROM usuario WHERE email = '<email>';
```

## Passo 3 — Interpretar resultados

| Situação | Causa provável | Solução |
|---|---|---|
| `copa_compra` vazia | Webhook não chegou ou hottok errado | Verificar `HOTMART_HOTTOK` no Railway; re-enviar o webhook no painel Hotmart |
| `copa_compra` tem registo mas `copa_acesso` vazio | Erro ao gravar acesso (ver logs) | Usar `/acesso-manual` para conceder manualmente |
| Tudo existe mas utilizador não consegue entrar | Senha temporária expirou ou email errado | Reset de senha em `/auth/reset` |
| `copa_compra` existe mas `email_enviado = 0` | Falha no envio de email | Verificar `SPRING_MAIL_*` no Railway; email pode estar na pasta spam |

## Passo 4 — Se precisar re-processar

Lê `HotmartWebhookController.java` e mostra como re-enviar o webhook manualmente via curl,
ou usa `/acesso-manual` para conceder o acesso directamente.

## Causa mais comum

O `HOTMART_HOTTOK` no Railway não coincide com o configurado no painel Hotmart → Webhooks.
Verificar em: Railway → Variables → `HOTMART_HOTTOK`.

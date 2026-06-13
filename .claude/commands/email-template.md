# Editar Template de Email

Guia para modificar ou criar templates de email transacional no Copa Insider.

---

## Ficheiro existente

O único template de email está em:
`src/main/resources/templates/mail/compra-confirmada.html`

É enviado após cada compra processada pelo webhook Hotmart.
O serviço de envio está em `HotmartWebhookController.java` (ou `CopaEmailService`).

---

## Variáveis disponíveis no template de compra

Antes de editar, lê o controller/service que chama o template para saber exatamente
quais variáveis são passadas. As típicas são:

| Variável | Conteúdo |
|---|---|
| `${nomeComprador}` | Nome do comprador |
| `${emailComprador}` | Email do comprador |
| `${nomeProduto}` | Nome do produto comprado |
| `${senhaTempOraria}` | Senha gerada automaticamente (se conta nova) |
| `${loginUrl}` | URL de login |

---

## Estrutura padrão de email HTML

O email usa CSS inline (obrigatório para compatibilidade com Gmail/Outlook):

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width"/>
</head>
<body style="margin:0;padding:0;background:#0a0a0a;font-family:Arial,sans-serif;">

<table width="100%" cellpadding="0" cellspacing="0" style="background:#0a0a0a;padding:40px 16px;">
    <tr>
        <td align="center">
            <table width="600" cellpadding="0" cellspacing="0"
                   style="background:#161616;border-radius:12px;overflow:hidden;max-width:600px;width:100%">

                <!-- Header -->
                <tr>
                    <td style="background:#0f172a;padding:24px 32px;text-align:center;
                                border-bottom:1px solid #1e293b;">
                        <span style="font-size:20px;font-weight:700;color:#e8b930;">Copa Insider</span>
                    </td>
                </tr>

                <!-- Corpo -->
                <tr>
                    <td style="padding:32px;">
                        <h1 style="color:#ffffff;font-size:22px;margin:0 0 16px;">
                            Olá, <span th:text="${nomeComprador}">Nome</span>!
                        </h1>
                        <p style="color:#94a3b8;font-size:15px;line-height:1.6;margin:0 0 24px;">
                            <!-- mensagem principal -->
                        </p>

                        <!-- Botão CTA -->
                        <div style="text-align:center;margin:32px 0;">
                            <a href="..." style="background:#e8b930;color:#000000;
                               padding:14px 32px;border-radius:8px;font-weight:700;
                               font-size:16px;text-decoration:none;display:inline-block;">
                                Aceder ao conteúdo →
                            </a>
                        </div>
                    </td>
                </tr>

                <!-- Footer -->
                <tr>
                    <td style="padding:20px 32px;border-top:1px solid #1e293b;text-align:center;">
                        <p style="color:#64748b;font-size:12px;margin:0;">
                            © 2026 Copa Insider · allaboutworldcup2026.com
                        </p>
                    </td>
                </tr>

            </table>
        </td>
    </tr>
</table>

</body>
</html>
```

---

## Regras para emails HTML

1. **Todo o CSS é inline** — clientes de email ignoram `<style>` tags
2. **Usa tabelas** para layout — `flexbox` e `grid` não funcionam no Outlook
3. **Sem fontes externas** — Google Fonts não carregam em muitos clientes
4. **Imagens com `width` e `alt`** — sempre definir dimensões
5. **Testar** em Gmail, Outlook e Apple Mail antes de ir para produção

---

## Testar o template localmente

Para ver o email renderizado sem fazer uma compra real, cria uma rota temporária:

```java
@GetMapping("/admin/test-email")
public String testEmail(Model model) {
    model.addAttribute("nomeComprador", "Utilizador Teste");
    model.addAttribute("nomeProduto", "Copa Pass");
    return "mail/compra-confirmada";
}
```

Apaga depois de verificar.

---

## Adicionar link directo para o conteúdo

Após compra, o utilizador precisa de saber exactamente onde ir.
Adiciona esta secção ao email de confirmação:

```html
<table width="100%" cellpadding="0" cellspacing="0"
       style="background:#0f172a;border-radius:8px;margin:16px 0;">
    <tr>
        <td style="padding:20px;">
            <p style="color:#e2e8f0;font-size:14px;font-weight:600;margin:0 0 12px;">
                Os teus acessos:
            </p>
            <p style="margin:6px 0;">
                <a href="https://allaboutworldcup2026.com/calendario"
                   style="color:#e8b930;font-size:14px;">📅 Calendário Copa 2026</a>
            </p>
            <p style="margin:6px 0;">
                <a href="https://allaboutworldcup2026.com/comparar"
                   style="color:#e8b930;font-size:14px;">⚽ Comparador de Seleções</a>
            </p>
        </td>
    </tr>
</table>
```

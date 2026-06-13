# Adicionar Novo Produto Copa Insider

Guia passo a passo para adicionar um novo produto digital ao Copa Insider.

O utilizador vai indicar: nome do produto, slug, tipo, preço EUR, e URL Hotmart (opcional).

Faz o seguinte:

## 1. Verificar o tipo correcto

Os tipos disponíveis estão em `TipoCopaProduto.java`. Lê o ficheiro e lista os valores disponíveis.

## 2. Criar migration SQL

Cria um novo ficheiro em `src/main/resources/db/migration/USER/` com o nome no formato:
`V<AAAAMMDD>_<NN>__<descricao_slug>.sql`

Usa a data de hoje. Verifica qual é o número sequencial seguinte para esse dia.

O SQL deve:
```sql
INSERT INTO copa_produto (slug, tipo, preco, nome_pt_pt, nome_pt_br, nome_en,
                          hotmart_url, hotmart_product_id, ativo, ordem, preco_eur)
VALUES ('<slug>', '<TIPO>', <preco_brl>, '<nome pt>', '<nome br>', '<nome en>',
        '<hotmart_url_ou_null>', <hotmart_id_ou_null>, 1, <proxima_ordem>, <preco_eur>)
ON DUPLICATE KEY UPDATE
    nome_pt_pt = VALUES(nome_pt_pt),
    hotmart_url = VALUES(hotmart_url),
    ativo = VALUES(ativo);
```

Para saber a próxima `ordem`, lê a última migration de produtos para ver o valor mais alto.

## 3. Se for incluído no Copa Pass

Adicionar o slug ao `Set<String> COPA_PASS_INCLUI` em:
`src/main/java/br/com/lectek/copainsider/application/service/CopaAcessoService.java`

## 4. Confirmar

Mostra o ficheiro SQL criado e pergunta se deve fazer commit.

# Cancelamento de pedidos no admin

## Regra operacional

- O botao `Cancelar pedido` aparece apenas para pedidos ainda nao pagos.
- Consideramos "nao pago" os status `ABERTO` e `AGUARDANDO_PAGAMENTO`.
- Antes de cancelar, o admin recebe a confirmacao `Tem certeza que deseja cancelar este pedido?`.
- Depois da confirmacao, o admin precisa informar um motivo obrigatorio.

## Motivos salvos

- `CLIENTE_SOLICITOU` -> Cliente solicitou
- `PRODUTO_FORA_DO_PADRAO` -> Produto fora do padrao
- `ESTOQUE_MENOR_QUE_1` -> Estoque menor que 1

## Dados persistidos

- `pedido.cancelamento_motivo`
- `pedido.cancelado_em`

Esses campos ficam salvos para consulta futura, pesquisa operacional e melhoria do processo de pedidos/estoque.

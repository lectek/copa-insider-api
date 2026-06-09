# OSRM local

Coloque nesta pasta o dataset processado do OSRM usado no ambiente de desenvolvimento.

Arquivos esperados:

- `infra/osrm/data/paraiba-latest.osrm`
- arquivos auxiliares gerados por `osrm-extract`, `osrm-partition` e `osrm-customize`

O `docker-compose` nao baixa nem gera esses arquivos automaticamente.

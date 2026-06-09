# Roteirizacao Open Source

## Stack adotada

- `Nominatim` self-hosted para transformar endereco em latitude/longitude.
- `OSRM` self-hosted para obter matriz real de distancia e duracao por ruas.
- `DeliveryRouteOptimizer` no backend para ordenar ate 12 entregas por menor custo acumulado.

## Como a aplicacao usa essa stack

- `DeliveryRouteService` geocodifica origem e destinos.
- Quando `app.route.engine=auto` ou `osrm`, ele tenta a matriz do `OSRM Table API`.
- Se o `OSRM` nao estiver disponivel, faz fallback para distancia geografica.
- `PublicDeliveryEstimateService` usa a rota planejada, o horario de saida e o tempo por parada para montar o ETA publico.

## Variaveis de ambiente

- `APP_ROUTE_ENGINE=auto`
- `APP_ROUTE_OSRM_BASE_URL=http://osrm:5000`
- `APP_ROUTE_NOMINATIM_BASE_URL=https://seu-nominatim/search`
- `APP_ROUTE_NOMINATIM_USER_AGENT=RedeMaisFarma/1.0`

No `docker-compose` de desenvolvimento o app sobe com `APP_ROUTE_ENGINE=geo` por padrao, para nao travar ETA quando o profile `routing` ainda nao foi ativado.

Quando o stack self-hosted estiver ativo, sobrescreva:

- `APP_ROUTE_ENGINE=auto`
- `APP_ROUTE_OSRM_BASE_URL=http://osrm:5000`
- `APP_ROUTE_NOMINATIM_BASE_URL=http://nominatim:8080/search`

## Subindo o OSRM no dev

O `docker-compose` traz um servico `osrm`, mas ele fica em profile separado para nao quebrar o ambiente padrao.

1. Baixe o extrato `.osm.pbf` da area desejada.
2. Gere os arquivos `.osrm` dentro de `infra/osrm/data`.
3. Suba o profile de roteirizacao:

```bash
docker compose -f docker-compose.dev.yml --profile routing up -d nominatim osrm
```

## Preparacao do dataset OSRM

Exemplo com Paraiba:

```bash
docker run --rm -v ${PWD}/infra/osrm/data:/data ghcr.io/project-osrm/osrm-backend osrm-extract -p /opt/car.lua /data/paraiba-latest.osm.pbf
docker run --rm -v ${PWD}/infra/osrm/data:/data ghcr.io/project-osrm/osrm-backend osrm-partition /data/paraiba-latest.osrm
docker run --rm -v ${PWD}/infra/osrm/data:/data ghcr.io/project-osrm/osrm-backend osrm-customize /data/paraiba-latest.osrm
```

Depois disso, mantenha `OSRM_DATA_FILE=paraiba-latest.osrm`.

## Nominatim

- O compose espera o arquivo `infra/nominatim/data/${NOMINATIM_PBF_FILE}`.
- Exemplo de nome: `paraiba-latest.osm.pbf`.
- O primeiro startup faz a importacao completa e pode demorar bastante.
- O endpoint interno da aplicacao passa a ser `http://nominatim:8080/search`.
- Nao use `nominatim.openstreetmap.org` em producao.
- Para uso comercial sem custo de API, a instancia precisa ser sua.

Exemplo de subida:

```bash
docker compose -f docker-compose.dev.yml --profile routing up -d nominatim
```

Se quiser reprocessar do zero, remova os volumes `nominatim_db_dev` e `nominatim_flatnode_dev`.

## Observacoes operacionais

- O otimizador atual resolve exatamente ate 12 paradas; acima disso o custo cresce demais.
- O mapa publico continua opcional; para o cliente final, o MVP usa apenas texto de ETA.
- Vale persistir cache de geocodificacao em banco para reduzir chamadas repetidas.
- `Nominatim` exige bem mais disco e memoria que `OSRM`; por isso ele ficou em profile opcional.

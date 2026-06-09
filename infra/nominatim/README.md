# Nominatim local

Coloque nesta pasta o extrato `.osm.pbf` usado para a importacao inicial do `Nominatim`.

Arquivo esperado por padrao:

- `infra/nominatim/data/paraiba-latest.osm.pbf`

Variaveis uteis:

- `NOMINATIM_PBF_FILE`
- `NOMINATIM_THREADS`
- `NOMINATIM_UPDATE_MODE`
- `HOST_NOMINATIM_PORT`

O banco e o flatnode ficam em volumes Docker nomeados para evitar reimportar a cada subida.

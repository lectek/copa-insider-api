# Exemplos de Configuracao por Tenant (Branding e Paginas)

## Regra de chave
- Global: `branding.cor_primaria`
- Por tenant: `tenant.<tenantId>.branding.cor_primaria`

Quando a chave do tenant nao existir, o sistema cai automaticamente na chave global.

## Exemplo 1 - Cores institucionais
- `branding.cor_primaria = #1f6feb`
- `branding.cor_secundaria = #0ea5a4`
- `branding.cor_acento = #0ea5e9`

Tenant `embalando`:
- `tenant.embalando.branding.cor_primaria = #0b5fff`
- `tenant.embalando.branding.cor_secundaria = #ffffff`
- `tenant.embalando.branding.cor_acento = #0ea5e9`

## Exemplo 2 - Tipografia por tenant
- Global: `cliente.frontend.font_family = 'Source Sans 3', system-ui, Arial, sans-serif`
- Tenant `embalando`: `tenant.embalando.cliente.frontend.font_family = 'DM Sans', 'Source Sans 3', system-ui, sans-serif`

## Exemplo 3 - Hero da home por tenant
- Global:
  - `branding.home_hero_url = /images/hero-default.webp`
  - `branding.home_hero_texto = Bem-vindo`
- Tenant `embalando`:
  - `tenant.embalando.branding.home_hero_url = /images/hero-embalando.webp`
  - `tenant.embalando.branding.home_hero_texto = Embalagens e bomboniere para seu negocio`

## Exemplo 4 - Modo de uso no admin
1. Abrir `/admin/configuracoes/branding?tenantId=embalando`
2. Alterar cores/logo/fonte.
3. Salvar: o sistema persiste com prefixo `tenant.embalando.*`.

Sem `tenantId`, salva no escopo global.

## Exemplo 5 - Pagina por empresa (chave JSON)
Sugestao de chave:
- `tenant.embalando.page.home.config`

Valor JSON:
```json
{
  "hero": {
    "title": "Embalando Solucoes",
    "subtitle": "Entrega local com agilidade"
  },
  "sectionsOrder": ["categorias", "destaques", "promocoes"],
  "showTestimonials": true
}
```

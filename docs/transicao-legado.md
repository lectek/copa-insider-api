# Copa Insider — Transição do Legado SaudeMaisFarma

Este documento registra o histórico de decisões arquiteturais, adaptações do legado e direção técnica do projeto.
É a memória viva do projeto — deve ser atualizado sempre que uma decisão relevante for tomada.

---

## Contexto da origem

O Copa Insider foi iniciado a partir da base da plataforma **SaudeMaisFarma** (também conhecida como **RedeMaisFarma API**), um sistema de e-commerce para farmácias com gestão de produtos, pedidos, clientes, campanhas de email, integração com Firebird legado e deploy na Railway.

### Por que reusar essa base?
- Estrutura Spring Boot madura com auth, JPA, Flyway, Thymeleaf já funcionando
- Redução de retrabalho: infraestrutura de autenticação, admin, e-mail e jobs já existente
- Velocidade de MVP: foco no conteúdo novo, não em reinventar infraestrutura

### O que o Copa Insider será
- Plataforma de conteúdo digital sobre Copa do Mundo
- Foco: contexto pré-jogo, estatísticas, rivalidades, histórias, guias rápidos para fãs
- Evolução gradual da base existente

---

## Estado atual do legado (2026-06-09)

### O que ainda é legado ativo
| Área | Status | Observação |
|------|--------|------------|
| Estrutura Spring Boot / MVC / Security | Ativo, reaproveitado | Base sólida, mantida |
| Autenticação JWT / OAuth2 | Ativo, adaptado | Funcional |
| Admin dashboard | Ativo, adaptando | Templates ainda com branding antigo em partes |
| Tabelas de produto, pedido, cliente | Legado presente | Serão adaptadas ao modelo Copa Insider |
| Campanhas de email | Legado presente | Estrutura reutilizável, contexto a adaptar |
| Firebird / legado sync | Desabilitado | `LEGACY_SYNC_ENABLED=false`; pode ser removido quando não houver mais necessidade |
| Endpoints `/api/v2/*`, `/api/app/*` | Legado presente | Manter por enquanto para compatibilidade |
| `.env` com nomes "redemaisfarma" | Legado em limpeza | Substituir gradualmente |
| Templates Thymeleaf com branding antigo | Em adaptação | Substituir conforme páginas forem redesenhadas |

### O que foi migrado/renomeado
- Pacote Java: `br.com.redemaisfarma` → `br.com.lectek.copainsider` ✅
- Entrypoint: `RedeMaisFarmaApiApplication` → `CopaInsiderApplication` ✅
- GroupId Maven: `br.com.lectek.copainsider` ✅
- ArtifactId: `copa-insider-api` ✅

---

## Decisões arquiteturais registradas

### 2026-06-09 — Remoção da lógica de detecção Railway
**Decisão**: Remover a lógica de detecção de ambiente Railway de `MySqlDataSourceConfig` e `CopaInsiderApplication`.

**Motivo**: O projeto migrou a infraestrutura de produção da Railway para a **Hostinger**. A lógica de detecção Railway (variáveis `RAILWAY_ENVIRONMENT_NAME`, `RAILWAY_MYSQL_URL`, etc.) não é mais necessária e adiciona complexidade desnecessária.

**O que foi mudado**:
- `MySqlDataSourceConfig.java`: removidas as variáveis de fallback Railway (`RAILWAY_MYSQL_URL`, `DATABASE_URL`, `MYSQL_PRIVATE_URL`); a URL do banco é resolvida diretamente de `SPRING_DATASOURCE_URL`
- `CopaInsiderApplication.java`: removida detecção `isRunningOnRailway()` e lógica de forçar perfil `prod` por ambiente Railway

**Como configurar em produção (Hostinger)**:
```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://<host-hostinger>:3306/<banco>?useSSL=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<senha>
```

---

### 2026-06-09 — Documentação como memória viva
**Decisão**: Os arquivos `.md` passam a ser a fonte primária de contexto do projeto.

**Regra estabelecida**: Sempre atualizar os `.md` quando:
- Uma decisão técnica for tomada
- Uma estrutura legada for adaptada ou removida
- Uma nova direção arquitetural surgir
- Um fluxo importante for definido

**Arquivos de referência**:
- `README.md` — visão geral e configuração
- `README_DEV.md` — guia de desenvolvimento
- `AGENTS.md` — guia para agentes de IA
- `docs/transicao-legado.md` — este arquivo

---

## Roadmap de migração do legado

### Prioridade alta (próximos)
- [ ] Adaptar templates de home e landing para identidade Copa Insider
- [ ] Substituir nomes "RedeMaisFarma" restantes em templates e variáveis
- [ ] Definir modelo de dados específico Copa Insider (conteúdo, times, partidas)

### Prioridade média
- [ ] Rever estrutura de "produtos" — adaptar ou substituir por "conteúdos"
- [ ] Avaliar se campanhas de email fazem sentido no contexto Copa Insider
- [ ] Remover `app/api/v2` quando não houver mais dependência

### Baixa prioridade / pode ficar
- [ ] Firebird: manter desabilitado até decisão de remoção completa
- [ ] Kafka/Redis: manter na config mas desabilitados até necessidade real

---

## Notas para futuras decisões
- Ao adaptar uma área legada, registrar aqui o que foi mudado e por quê
- Ao remover código legado, confirmar que não há mais dependência antes de deletar
- Manter compatibilidade com o banco existente via Flyway migrations (não recriar do zero)

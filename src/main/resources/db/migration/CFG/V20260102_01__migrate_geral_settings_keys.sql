-- Migrate legacy GERAL.* keys to the new standardized keys.
-- Only insert when the new key does not exist yet.

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'loja.nome', s.setting_value, 'Nome publico da loja', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.nome_sistema'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'loja.nome');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'loja.nome_fantasia', s.setting_value, 'Nome fantasia', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.nome_fantasia'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'loja.nome_fantasia');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'loja.razao_social', s.setting_value, 'Razao social', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.razao_social'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'loja.razao_social');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'loja.nome_exibicao', s.setting_value, 'Nome exibido no site', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.nome_loja_site'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'loja.nome_exibicao');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'loja.slogan', s.setting_value, 'Slogan', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.slogan_loja'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'loja.slogan');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'branding.logo_url', s.setting_value, 'Logo principal', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.logo_inicial_url'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'branding.logo_url');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'branding.favicon_url', s.setting_value, 'Favicon', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.favicon_url'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'branding.favicon_url');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'branding.home_hero_url', s.setting_value, 'Imagem principal da home', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.home_hero_imagem_url'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'branding.home_hero_url');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'branding.home_hero_texto', s.setting_value, 'Texto do destaque', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.home_hero_texto'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'branding.home_hero_texto');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'contato.email', s.setting_value, 'Email principal', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.email'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'contato.email');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'contato.telefone', s.setting_value, 'Telefone principal', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.telefone'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'contato.telefone');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'contato.whatsapp', s.setting_value, 'Whatsapp', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.whatsapp'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'contato.whatsapp');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'contato.instagram', s.setting_value, 'Instagram', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.instagram'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'contato.instagram');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'contato.site_url', s.setting_value, 'Site oficial', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.site_url'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'contato.site_url');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'endereco.logradouro', s.setting_value, 'Endereco', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.endereco'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'endereco.logradouro');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'endereco.cidade', s.setting_value, 'Cidade', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.cidade'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'endereco.cidade');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'endereco.estado', s.setting_value, 'Estado', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.estado'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'endereco.estado');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'endereco.cep', s.setting_value, 'CEP', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.cep'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'endereco.cep');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'endereco.bairro', s.setting_value, 'Bairro', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.bairro'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'endereco.bairro');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'entrega.horario_atendimento', s.setting_value, 'Horario de atendimento', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.horario_atendimento'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'entrega.horario_atendimento');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'entrega.taxa', s.setting_value, 'Taxa de entrega', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.taxa_entrega'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'entrega.taxa');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'entrega.pedido_minimo', s.setting_value, 'Pedido minimo', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.pedido_minimo'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'entrega.pedido_minimo');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'preferencias.cadastro_rapido', s.setting_value, 'Cadastro rapido', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.habilitar_cadastro_rapido'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'preferencias.cadastro_rapido');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'preferencias.assinaturas', s.setting_value, 'Assinaturas', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.habilitar_assinaturas'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'preferencias.assinaturas');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'preferencias.notificacoes', s.setting_value, 'Notificacoes', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.habilitar_notificacoes'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'preferencias.notificacoes');

INSERT INTO app_settings (setting_key, setting_value, description, created_at, updated_at)
SELECT 'preferencias.exibir_destaques_home', s.setting_value, 'Destaques na home', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM app_settings s
 WHERE s.setting_key = 'GERAL.exibir_destaques_home'
   AND NOT EXISTS (SELECT 1 FROM app_settings x WHERE x.setting_key = 'preferencias.exibir_destaques_home');

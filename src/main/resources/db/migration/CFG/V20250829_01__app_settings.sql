CREATE TABLE app_setting (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  category      VARCHAR(50)  NOT NULL,
  skey          VARCHAR(120) NOT NULL UNIQUE,
  svalue        TEXT         NULL,
  stype         VARCHAR(20)  NOT NULL, -- STRING, TEXT, INT, DECIMAL, BOOLEAN, COLOR
  description   VARCHAR(255) NULL,
  updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- LOJA
INSERT INTO app_setting(category, skey, svalue, stype, description) VALUES
('LOJA','loja.nome','RedeMaisFarma','STRING','Nome público da loja'),
('LOJA','loja.logo','/images/logo.png','STRING','URL do logo'),
('LOJA','loja.cor_primaria','#2563eb','COLOR','Cor primária da marca');

-- LAYOUT
INSERT INTO app_setting(category, skey, svalue, stype, description) VALUES
('LAYOUT','layout.itens_por_pagina','24','INT','Quantidade por página'),
('LAYOUT','layout.exibir_indisponiveis','true','BOOLEAN','Mostrar produtos esgotados'),
('LAYOUT','layout.ordenacao_padrao','mais_vendidos','STRING','Ordenação default');

-- SEO
INSERT INTO app_setting(category, skey, svalue, stype, description) VALUES
('SEO','seo.titulo_padrao','RedeMaisFarma','STRING','Título padrão do site'),
('SEO','seo.descricao_padrao','Sua farmácia online.','TEXT','Meta description padrão'),
('SEO','seo.gtm_id','','STRING','Google Tag Manager ID'),
('SEO','seo.robots','index,follow','STRING','Diretiva robots global');

-- CHECKOUT
INSERT INTO app_setting(category, skey, svalue, stype, description) VALUES
('CHECKOUT','checkout.guest_ativo','true','BOOLEAN','Permitir compra sem conta'),
('CHECKOUT','checkout.cpf_obrigatorio','true','BOOLEAN','Exigir CPF'),
('CHECKOUT','checkout.telefone_obrigatorio','true','BOOLEAN','Exigir telefone'),
('CHECKOUT','checkout.politica_url','/politicas/devolucao','STRING','URL da política de devolução');

-- FRETE
INSERT INTO app_setting(category, skey, svalue, stype, description) VALUES
('FRETE','frete.gratis_acima','199.90','DECIMAL','Valor para frete grátis'),
('FRETE','frete.retirada_ativa','true','BOOLEAN','Habilita retirada na loja'),
('FRETE','frete.prazo_extra_dias','2','INT','Dias extras ao prazo');

-- PAGAMENTO
INSERT INTO app_setting(category, skey, svalue, stype, description) VALUES
('PAGAMENTO','pg.gateway','mercado_pago','STRING','Gateway ativo'),
('PAGAMENTO','pg.pix_ativo','true','BOOLEAN','Habilita PIX'),
('PAGAMENTO','pg.desconto_avista_pct','5.0','DECIMAL','% desconto à vista');

-- EMAIL
INSERT INTO app_setting(category, skey, svalue, stype, description) VALUES
('EMAIL','email.remetente','Suporte <no-reply@redemaisfarma.com.br>','STRING','Remetente padrão'),
('EMAIL','email.smtp.host','','STRING','SMTP host'),
('EMAIL','email.smtp.porta','587','INT','SMTP porta'),
('EMAIL','email.smtp.usuario','','STRING','SMTP usuário'),
('EMAIL','email.smtp.senha','','STRING','SMTP senha'),
('EMAIL','email.bcc_admin','false','BOOLEAN','Cópia oculta para admin');

-- INTEGRAÇÕES
INSERT INTO app_setting(category, skey, svalue, stype, description) VALUES
('INTEGRACOES','captcha.site_key','','STRING','reCAPTCHA site key'),
('INTEGRACOES','captcha.secret_key','','STRING','reCAPTCHA secret');

-- OPERAÇÃO
INSERT INTO app_setting(category, skey, svalue, stype, description) VALUES
('OPERACAO','app.manutencao.ativo','false','BOOLEAN','Liga modo manutenção'),
('OPERACAO','app.manutencao.mensagem','Voltamos em breve.','TEXT','Mensagem de manutenção'),
('OPERACAO','auth.bloquear_cadastro','false','BOOLEAN','Desabilita novos cadastros');

-- FLAGS
INSERT INTO app_setting(category, skey, svalue, stype, description) VALUES
('FLAGS','flag.checkout_novo','false','BOOLEAN','Novo checkout'),
('FLAGS','flag.banner_home','true','BOOLEAN','Banner da home');

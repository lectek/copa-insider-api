-- Scripts de apoio local para deixar o carrossel da vitrine com dados.
-- Executar manualmente com:
-- docker compose exec mysql mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE} < mysql_init/seed-destaques.sql

DELIMITER //
CREATE PROCEDURE seed_destaques()
BEGIN
  IF EXISTS (
      SELECT 1
      FROM information_schema.TABLES
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'produto'
  ) THEN
    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Medicamentos',
        'Paracetamol 500mg',
        'Paracetamol 20 comprimidos',
        '7891111111111',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 50,
        12.90, 11.90, 5.00,
        TRUE, NOW(), 'seed-paracetamol'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-paracetamol');

    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Vitaminas',
        'Vitamina C 1000mg',
        'Comprimidos efervescentes com sabor cاًtrico',
        '7892222222222',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 30,
        28.50, 24.99, 12.00,
        TRUE, NOW(), 'seed-vitamina-c'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-vitamina-c');

    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Cuidados pessoais',
        'ا?gua Micelar 200ml',
        'Demaquilante suave para todos os tipos de pele',
        '7893333333333',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 40,
        32.90, 29.90, 15.00,
        TRUE, NOW(), 'seed-agua-micelar'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-agua-micelar');

    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Higiene',
        'Sabonete Neutro 90g',
        'Sabonete para pele sensivel',
        '7894444444444',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 80,
        4.99, 3.99, 1.50,
        FALSE, NOW(), 'seed-sabonete-neutro'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-sabonete-neutro');

    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Higiene',
        'Shampoo Anticaspa 200ml',
        'Shampoo controle de caspa',
        '7895555555555',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 35,
        18.90, 16.90, 8.00,
        FALSE, NOW(), 'seed-shampoo-anticaspa'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-shampoo-anticaspa');

    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Cuidados pessoais',
        'Hidratante Corporal 300ml',
        'Hidratante de uso diario',
        '7896666666666',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 28,
        22.90, 19.90, 10.00,
        FALSE, NOW(), 'seed-hidratante-corporal'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-hidratante-corporal');

    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Medicamentos',
        'Ibuprofeno 400mg',
        'Ibuprofeno 10 comprimidos',
        '7897777777777',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 60,
        19.90, 17.90, 7.50,
        FALSE, NOW(), 'seed-ibuprofeno-400'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-ibuprofeno-400');

    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Medicamentos',
        'Dipirona 500mg',
        'Dipirona 20 comprimidos',
        '7898888888888',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 70,
        11.90, 9.90, 4.00,
        FALSE, NOW(), 'seed-dipirona-500'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-dipirona-500');

    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Vitaminas',
        'Multivitaminico A-Z',
        '30 comprimidos',
        '7899999999999',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 45,
        39.90, 34.90, 18.00,
        FALSE, NOW(), 'seed-multivitaminico-az'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-multivitaminico-az');

    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Infantil',
        'Soro Fisiologico 250ml',
        'Solucao nasal isotonica',
        '7891010101010',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 90,
        8.90, 7.50, 3.00,
        FALSE, NOW(), 'seed-soro-fisiologico'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-soro-fisiologico');

    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Acessorios',
        'Algodao Hidrofilo 50g',
        'Algodao para uso geral',
        '7892020202020',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 120,
        6.50, 5.50, 2.00,
        FALSE, NOW(), 'seed-algodao-50g'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-algodao-50g');

    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Acessorios',
        'Termometro Digital',
        'Termometro de leitura rapida',
        '7893030303030',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 25,
        29.90, 26.90, 12.00,
        FALSE, NOW(), 'seed-termometro-digital'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-termometro-digital');

    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Cuidados pessoais',
        'Protetor Solar FPS 50',
        'Protecao UVA/UVB 120ml',
        '7894040404040',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 22,
        49.90, 42.90, 22.00,
        FALSE, NOW(), 'seed-protetor-solar-50'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-protetor-solar-50');

    INSERT INTO produto (
        categoria, nome, descricao, codigo_barras,
        created_at, updated_at, data_cadastro, version,
        status, disponivel, estoque,
        preco_venda, preco_promocional, preco_custo,
        destaque_carrossel, publicado_em, hash_legado
    )
    SELECT
        'Bem-estar',
        'Cha de Camomila 10 saches',
        'Cha relaxante sem acucar',
        '7895050505050',
        NOW(), NOW(), NOW(), 1,
        'PUBLICADO', TRUE, 55,
        12.50, 10.90, 4.50,
        FALSE, NOW(), 'seed-cha-camomila'
    FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM produto WHERE hash_legado = 'seed-cha-camomila');
  END IF;
END//
CALL seed_destaques()//
DROP PROCEDURE seed_destaques//
DELIMITER ;

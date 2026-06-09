ALTER TABLE produto
  ADD COLUMN fiscal_ncm VARCHAR(8) NULL,
  ADD COLUMN fiscal_cest VARCHAR(7) NULL,
  ADD COLUMN fiscal_cfop VARCHAR(4) NULL,
  ADD COLUMN fiscal_origem INTEGER NULL,
  ADD COLUMN fiscal_icms_cst VARCHAR(3) NULL,
  ADD COLUMN fiscal_csosn VARCHAR(3) NULL,
  ADD COLUMN fiscal_pis_cst VARCHAR(2) NULL,
  ADD COLUMN fiscal_cofins_cst VARCHAR(2) NULL;

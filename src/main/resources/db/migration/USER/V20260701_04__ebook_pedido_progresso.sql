ALTER TABLE ebook_pedido
    ADD COLUMN progresso INT NOT NULL DEFAULT 0 AFTER status;

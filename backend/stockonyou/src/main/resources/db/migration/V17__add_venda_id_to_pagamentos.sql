ALTER TABLE pagamentos
    ADD COLUMN venda_id BIGINT NOT NULL;

ALTER TABLE pagamentos
    ADD CONSTRAINT fk_pagamentos_venda
    FOREIGN KEY (venda_id)
    REFERENCES vendas(id);
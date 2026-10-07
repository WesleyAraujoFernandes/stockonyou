CREATE TABLE pagamentos (
    id BIGSERIAL PRIMARY KEY,
    valor NUMERIC(15,2) NOT NULL,
    forma_pagamento VARCHAR(30) NOT NULL,
    data_pagamento TIMESTAMP NOT NULL,
    usuario_nome VARCHAR(255)
);

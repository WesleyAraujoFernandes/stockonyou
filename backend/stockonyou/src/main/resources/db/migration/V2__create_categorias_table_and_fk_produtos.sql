CREATE TABLE categorias (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(80) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE produtos 
ADD COLUMN categoria_id BIGINT;

ALTER TABLE produtos 
ADD CONSTRAINT fk_produtos_categoria 
FOREIGN KEY (categoria_id) REFERENCES categorias(id);
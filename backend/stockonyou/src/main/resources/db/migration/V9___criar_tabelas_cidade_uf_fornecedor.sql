create table fornecedores(
	id BIGSERIAL PRIMARY KEY,
	nome varchar(120),
	email varchar(255),
	telefone varchar(20),
	cnpj varchar(20),
	endereco varchar(255),
	cidade_id int,
	cep varchar(8)
);

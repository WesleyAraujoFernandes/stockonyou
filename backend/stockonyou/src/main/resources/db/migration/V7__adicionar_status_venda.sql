-- Adiciona a coluna de status na tabela de vendas
ALTER TABLE vendas ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'FINALIZADA';

-- Atualiza as vendas antigas para finalizadas para não quebrar o histórico
UPDATE vendas SET status = 'FINALIZADA';

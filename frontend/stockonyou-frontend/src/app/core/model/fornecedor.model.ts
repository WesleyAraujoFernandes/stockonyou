export interface Fornecedor {
  id: number;
  nome: string;
  email?: string;
  telefone?: string;
  cnpj?: string;
  endereco?: string;
  cidade: {
    id: number;
    nome: string;
  };
  cep?: string;
  observacao?: string;
}

export interface FornecedorRequest {
  nome: string;
  email?: string;
  telefone?: string;
  cnpj?: string;
  endereco?: string;
  cep?: string;
  observacao?: string;
  cidadeId: number;
}

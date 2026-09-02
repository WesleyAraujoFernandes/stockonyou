export interface Categoria {
  id: number;
  nome: string;
  descricao?: string;
  dataCriacao?: string;
}

export interface Produto {
  id: number;
  nome: string;
  codigoBarras: string;
  quantidade: number;
  quantidadeMinima: number;
  preco: number;
  categoria: Categoria;
  dataCriacao?: string;
  dataAtualizacao?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

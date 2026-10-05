import { Categoria } from "./categoria.model";

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

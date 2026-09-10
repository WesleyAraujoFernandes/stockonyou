import { Produto } from "./produto.model";
export interface ItemVendaRequest {
  produtoId: number;
  quantidade: number;
  precoUnitario: number;
}

export interface ItemVendaResponse {
  id: number;
  produto: Produto;
  quantidade: number;
  precoUnitario: number;
  subtotal: number;
}

export interface VendaRequest {
  clienteNome?: string;
  itens: ItemVendaRequest[];
}

export interface VendaResponse {
  id: number;
  dataVenda: string;
  clienteNome?: string;
  clienteId: number,
  valorTotal: number;
  status: 'PAGO' | 'PENDENTE' | 'ABERTA';
  itens: ItemVendaResponse[];
  usuarioNome?: string;
}

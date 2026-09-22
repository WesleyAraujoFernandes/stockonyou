import { Produto } from "./produto.model";

export type StatusVenda = 'ABERTA' | 'PENDENTE' | 'PAGO' | 'CANCELADA'
export interface ItemVendaRequest {
  produtoId: number;
  quantidade: number;
}

export interface ItemVendaResponse {
  id: number;
  produtoId: number;
  produtoNome: string;
  quantidade: number;
  precoUnitario: number;
  subtotal: number;
}

export interface VendaRequest {
  clienteId?: number;
  clienteNome?: string;
  itens: ItemVendaRequest[];
}

export interface VendaResponse {
  id: number;
  dataVenda: string;
  clienteNome?: string;
  valorTotal: number;
  usuarioNome: string;
  status: StatusVenda;
  alertas: string[];
  itens: ItemVendaResponse[];
}

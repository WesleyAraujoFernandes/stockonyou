export type FormaPagamento =
  | 'DINHEIRO'
  | 'PIX'
  | 'CARTAO_CREDITO'
  | 'CARTAO_DEBITO';

  export interface PagamentoRequest {
    valor: number;
    formaPagamento: FormaPagamento;
  }

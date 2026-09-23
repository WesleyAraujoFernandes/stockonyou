import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../model/produto.model';
import { ItemVendaRequest, VendaRequest, VendaResponse } from '../model/venda.model';

@Injectable({
  providedIn: 'root',
})
export class VendaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/vendas';

  atualizarQuantidadeItemComanda(
    comandaId: number,
    produtoId: number,
    quantidade: number
  ): Observable<VendaResponse> {
    const params = new HttpParams()
      .set('quantidade', quantidade.toString());
    return this.http.put<VendaResponse>(`${this.apiUrl}/${comandaId}/itens/${produtoId}`, {}, { params }
    )
  }

  listarComFiltros(
    clienteNome?: string,
    status?: string,
    dataInicio?: string,
    dataFim?: string,
    page: number = 0,
    size: number = 10
  ) : Observable<PageResponse<VendaResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'id,desc')
    if (clienteNome && clienteNome.trim()) {
      params = params.set('clienteNome', clienteNome.trim());
    }
    if (status && status.trim()) {
      params = params.set('status', status.trim());
    }
    if (dataInicio && dataInicio.trim()) {
      params = params.set('dataInicio', dataInicio.trim())
    }
    if (dataFim && dataFim.trim()) {
      params = params.set('dataFim', dataFim.trim())
    }
    return this.http.get<PageResponse<VendaResponse>>(this.apiUrl, { params });
  }

  buscarPorId(id: number): Observable<VendaResponse> {
    return this.http.get<VendaResponse>(`${this.apiUrl}/${id}`);
  }

  realizarVenda(venda: VendaRequest): Observable<VendaResponse> {
    return this.http.post<VendaResponse>(this.apiUrl, venda);
  }

  buscarComandaAbertaPorCliente(clienteId: number): Observable<VendaResponse> {
    return this.http.get<VendaResponse>(`${this.apiUrl}/cliente/${clienteId}/aberta`)
  }

  listarComandasAbertas(): Observable<VendaResponse[]> {
    return this.http.get<VendaResponse[]>(`${this.apiUrl}/comandas`);
  }

  criarComanda(venda: VendaRequest): Observable<VendaResponse> {
    return this.http.post<VendaResponse> (`${this.apiUrl}/comandas`, venda);
  }

  concluirComanda(vendaId: number): Observable<VendaResponse> {
    return this.http.put<VendaResponse> (`${this.apiUrl}/${vendaId}/concluir`,{})
  }

  cancelarComanda(vendaId: number): Observable<VendaResponse> {
    return this.http.put<VendaResponse>(`${this.apiUrl}/${vendaId}/cancelar`, {})
  }

  registrarPagamento(vendaId: number): Observable<VendaResponse> {
    return this.http.put<VendaResponse>(`${this.apiUrl}/${vendaId}/pagamento`, {})
  }

  removerItemComanda(
    comandaId: number,
    produtoId: number
  ): Observable<VendaResponse> {
    return this.http.delete<VendaResponse>(
      `${this.apiUrl}/${comandaId}/itens/${produtoId}`
    )
  }

  adicionarItemComanda(comandaId: number, item: ItemVendaRequest): Observable<VendaResponse> {
    return this.http.put<VendaResponse>(`${this.apiUrl}/${comandaId}/itens`, item)
  }

}

import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../model/produto.model';
import { VendaRequest, VendaResponse } from '../model/venda.model';

@Injectable({
  providedIn: 'root',
})
export class VendaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/vendas';

  listarComFiltros(
    clienteNome?: string,
    dataInicio?: string,
    dataFim?: string,
    page: number = 0,
    size: number = 10
  ) : Observable<PageResponse<VendaResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'id,desc')
    if (clienteNome) params = params.set('clienteNome', clienteNome);
    if (dataInicio) params = params.set('dataInicio', dataInicio);
    if (dataFim) params = params.set('dataFim', dataFim);
    return this.http.get<PageResponse<VendaResponse>>(this.apiUrl, { params })
  }

  buscarPorId(id: number): Observable<VendaResponse> {
    return this.http.get<VendaResponse>(`${this.apiUrl}/${id}`);
  }

  realizarVenda(venda: VendaRequest): Observable<VendaResponse> {
    return this.http.post<VendaResponse>(this.apiUrl, venda);
  }

  finalizarComanda(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/finalizar`, {});
  }

  buscarComandaAbertaPorCliente(clienteId: number): Observable<VendaResponse | null> {
    return this.http.get<VendaResponse | null>(`${this.apiUrl}/cliente/${clienteId}/aberta`)
  }
}

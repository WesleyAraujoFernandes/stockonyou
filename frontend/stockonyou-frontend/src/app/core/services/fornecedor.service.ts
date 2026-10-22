import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../config/api.config';
import { Fornecedor, FornecedorRequest } from '../model/fornecedor.model';
import { PageResponse } from '../model/page-response.model';

@Injectable({
  providedIn: 'root',
})
export class FornecedorService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${API_CONFIG.baseUrl}/fornecedores`;

  listarComFiltros(
    nome?: string,
    page: number = 0,
    size: number =10
  ): Observable<PageResponse<Fornecedor>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'nome,asc');

    if (nome?.trim()) {
      params = params.set('nome', nome.trim());
    }

    return this.http.get<PageResponse<Fornecedor>>(
      this.apiUrl,
      { params }
    )
  }

  buscarPorId(id: number): Observable<Fornecedor> {
    return this.http.get<Fornecedor>(`${this.apiUrl}/${id}`)
  }

  autocomplete(termo: string): Observable<Fornecedor[]> {
    const params = new HttpParams().set('termo', termo);

    return this.http.get<Fornecedor[]>(
      `${this.apiUrl}/autocomplete`,
      { params }
    )
  }

  criar(fornecedor: FornecedorRequest): Observable<Fornecedor> {
    return this.http.post<Fornecedor>(this.apiUrl, fornecedor);
  }

  atualizar(
    id: number,
    fornecedor: FornecedorRequest
  ): Observable<Fornecedor> {
    return this.http.put<Fornecedor>(
      `${this.apiUrl}/${id}`,
      fornecedor
    )
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
  }
}

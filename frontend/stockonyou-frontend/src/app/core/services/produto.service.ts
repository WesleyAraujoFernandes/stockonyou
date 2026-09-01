import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse, Produto } from '../model/produto.model';

@Injectable({
  providedIn: 'root',
})
export class ProdutoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/produtos';

  listarComFiltros(
    nome?: string,
    precoMin?: number,
    precoMax?: number,
    categoriaId?: number,
    page: number = 0,
    size: number = 10
  ): Observable<PageResponse<Produto>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'id,desc');
    if (nome) params = params.set('nome', nome);
    if (precoMin !== undefined && precoMin !== null)
      params = params.set('precoMin', precoMin.toString());
    if (precoMax !== undefined && precoMax !== null)
      params = params.set('precoMax', precoMax.toString());
    if (categoriaId) params = params.set('categoriaId', categoriaId.toString());
    return this.http.get<PageResponse<Produto>>(this.apiUrl, { params });
  }

  criar(produto: Partial<Produto>): Observable<Produto> {
    return this.http.post<Produto>(this.apiUrl, produto);
  }

  atualizar(id: number, produto: Produto): Observable<Produto> {
    return this.http.put<Produto>(`${this.apiUrl}/${id}`, produto);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

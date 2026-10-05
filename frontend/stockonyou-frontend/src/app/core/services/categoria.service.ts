import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Categoria } from '../model/categoria.model';
import { PageResponse } from '../model/page-response.model'; 
import { API_CONFIG } from '../config/api.config';

@Injectable({
  providedIn: 'root',
})
export class CategoriaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${API_CONFIG.baseUrl}/categorias`;

listarTodas(): Observable<Categoria[]> {
    return this.http.get<PageResponse<Categoria>>(this.apiUrl).pipe(
      map((response) => (response.content))
    );
  }

  listarComPaginacao(page: number = 0, size: number = 10): Observable<PageResponse<Categoria>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'id,desc');
    return this.http.get<PageResponse<Categoria>>(this.apiUrl, {params});
  }

  criar(categoria: Partial<Categoria>): Observable<Categoria> {
    return this.http.post<Categoria>(this.apiUrl, categoria);
  }

  atualizar(id: number, categoria: Partial<Categoria>): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.apiUrl}/${id}`, categoria);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

}
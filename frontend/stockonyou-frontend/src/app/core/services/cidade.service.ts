import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_CONFIG } from '../config/api.config';
import { Observable } from 'rxjs';
import { PageResponse } from '../model/page-response.model';
import { Cidade } from '../model/cidade.model';

@Injectable({
  providedIn: 'root',
})
export class CidadeService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${API_CONFIG.baseUrl}/cidades`;

  listarTodas(
    page: number = 0,
    size: number = 100
  ): Observable<PageResponse<Cidade>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'nome,asc');

    return this.http.get<PageResponse<Cidade>>(
      this.apiUrl,
      { params }
    )
  }

  buscarPorId(id: number): Observable<Cidade> {
    return this.http.get<Cidade>(`${this.apiUrl}/${id}`);
  }
}

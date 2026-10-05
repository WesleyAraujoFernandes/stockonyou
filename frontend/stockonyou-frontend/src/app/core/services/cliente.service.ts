import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { PageResponse } from '../model/page-response.model';
import { API_CONFIG } from '../config/api.config';


export interface Cliente {
  id: number,
  nome: string,
  email?: string,
  telefone?: string
}

@Injectable({
  providedIn: 'root',
})
export class ClienteService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${API_CONFIG.baseUrl}/clientes`

  buscarPorTermo(termo: string): Observable<Cliente[]> {
    const params = new HttpParams().set('nome', termo);
    return this.http
      .get<PageResponse<Cliente>>(`${this.apiUrl}/autocomplete`, {params})
      .pipe(
        map(response => response.content ?? [])
      )
  }

  cadastrarRapido(nome: string): Observable<Cliente> {
    return this.http.post<Cliente>(this.apiUrl, {nome});
  }
}

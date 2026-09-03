import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';


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
  private readonly apiUrl = 'http://localhost:8080/api/clientes';

  buscarPorTermo(termo: string): Observable<Cliente[]> {
    const params = new HttpParams().set('termo', termo);
    return this.http.get<Cliente[]>(`${this.apiUrl}/autocomplete`, {params});
  }

  cadastrarRapido(nome: string): Observable<Cliente> {
    return this.http.post<Cliente>(this.apiUrl, {nome});
  }
}

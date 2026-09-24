import { inject, Injectable, signal } from '@angular/core';
import { VendaService } from '../../../core/services/venda.service';
import { VendaResponse } from '../../../core/model/venda.model';
import { PageResponse } from '../../../core/model/produto.model';

@Injectable()
export class HistoricoVendaStore {

  private readonly vendaService = inject(VendaService);

  readonly vendas = signal<VendaResponse[]>([]);
  readonly paginaAtual = signal(0);
  readonly totalPaginas = signal(0);
  readonly totalElementos = signal(0);

  readonly itensPorPagina = 10;

  carregar(
    filtroCliente: string,
    filtroStatus: string,
    filtroDataInicio: string,
    filtroDataFim: string
  ): void {

    this.vendaService
      .listarComFiltros(
        filtroCliente,
        filtroStatus,
        filtroDataInicio,
        filtroDataFim,
        this.paginaAtual(),
        this.itensPorPagina
      )
      .subscribe({
        next: (response: PageResponse<VendaResponse>) => {
          this.vendas.set(response.content ?? []);
          this.totalPaginas.set(response.totalPages ?? 0);
          this.totalElementos.set(response.totalElements ?? 0);
        }
      });
  }

  irParaPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas()) {
      return;
    }

    this.paginaAtual.set(pagina);
  }

  primeiraPagina(): void {
    this.paginaAtual.set(0);
  }
}

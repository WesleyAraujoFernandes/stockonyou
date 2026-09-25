import { inject, Injectable, signal } from '@angular/core';
import { VendaService } from '../../../core/services/venda.service';
import { VendaResponse } from '../../../core/model/venda.model';
import { PageResponse } from '../../../core/model/produto.model';
import { Observable } from 'rxjs';

@Injectable()
export class HistoricoVendaStore {

  private readonly vendaService = inject(VendaService);

  readonly vendas = signal<VendaResponse[]>([]);
  readonly paginaAtual = signal(0);
  readonly totalPaginas = signal(0);
  readonly totalElementos = signal(0);

  readonly itensPorPagina = 10;

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly carregandoDetalhe = signal(false);

  readonly quitando = signal(false);
  readonly erroQuitacao = signal<string | null>(null);

  readonly quitacaoConcluida = signal(0);

  readonly vendaSelecionada = signal<VendaResponse | null>(null);

  carregar(
    filtroCliente: string,
    filtroStatus: string,
    filtroDataInicio: string,
    filtroDataFim: string
  ): void {
    this.loading.set(true);
    this.error.set(null);
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
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Erro ao carregar histórico de vendas:', err);
          this.vendas.set([]);
          this.totalPaginas.set(0);
          this.totalElementos.set(0);
          this.error.set(
            'Falha ao carregar o histórico de vendas.'
          );
          this.loading.set(false);
        }
      });
  }

  carregarDetalhes(vendaId: number): void {
    this.carregandoDetalhe.set(true);
    this.vendaService.buscarPorId(vendaId).subscribe({
      next: (venda) => {
        this.vendaSelecionada.set(venda);
        this.carregandoDetalhe.set(false);
      },
      error: (err) => {
        console.error('Error ao carregar detalhes da venda:', err);
        this.carregandoDetalhe.set(false);
      }
    })
  }

  irParaPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas()) {
      return;
    }

    this.paginaAtual.set(pagina);
  }

  limparVendaSelecionada(): void {
    this.vendaSelecionada.set(null);
  }

  primeiraPagina(): void {
    this.paginaAtual.set(0);
  }

  quitarConta(vendaId: number): void {
    this.quitando.set(true);
    this.erroQuitacao.set(null);
    this.vendaService.registrarPagamento(vendaId).subscribe({
      next: () => {
        this.quitando.set(false);
        this.quitacaoConcluida.update(valor => valor + 1);
      },
      error: (err) => {
        console.error('Erro ao quitar conta:',err);
        this.erroQuitacao.set('Erro ao processar a quitação no servidor.')
        this.quitando.set(false);
      }
    })
  }

  selecionarVenda(venda: VendaResponse): void {
    this.vendaSelecionada.set(venda);
  }

}

import { inject, Injectable, signal } from '@angular/core';
import { VendaService } from '../../../core/services/venda.service';
import { VendaResponse } from '../../../core/model/venda.model';
import { PageResponse } from '../../../core/model/produto.model';
import { finalize, Observable } from 'rxjs';

@Injectable()
export class HistoricoVendaStore {

  private readonly vendaService = inject(VendaService);

  readonly vendas = signal<VendaResponse[]>([]);
  readonly paginaAtual = signal(0);
  readonly totalPaginas = signal(0);
  readonly totalElementos = signal(0);

  readonly itensPorPagina = 10;

  readonly errorLista = signal<string | null>(null);
  readonly carregandoLista = signal(false);
  readonly carregandoDetalhe = signal(false);

  readonly quitando = signal(false);
  readonly erroQuitacao = signal<string | null>(null);

  readonly erroDetalhe = signal<string | null>(null);

  readonly quitacaoConcluida = signal(0);

  readonly vendaSelecionada = signal<VendaResponse | null>(null);

  readonly campoOrdenacao = signal('id');
  readonly direcaoOrdenacao = signal<'asc' | 'desc'>('desc');

  carregar(
    filtroCliente: string,
    filtroStatus: string,
    filtroDataInicio: string,
    filtroDataFim: string
  ): void {
    this.carregandoLista.set(true);
    this.errorLista.set(null);
    this.vendaSelecionada.set(null);
    this.vendaService
      .listarComFiltros(
        filtroCliente,
        filtroStatus,
        filtroDataInicio,
        filtroDataFim,
        this.paginaAtual(),
        this.itensPorPagina,
        this.campoOrdenacao(),
        this.direcaoOrdenacao()
      )
      .pipe(
        finalize(() => {
          this.carregandoLista.set(false);
        })
      )
      .subscribe({
        next: (response: PageResponse<VendaResponse>) => {
          this.vendas.set(response.content ?? []);
          this.totalPaginas.set(response.totalPages ?? 0);
          this.totalElementos.set(response.totalElements ?? 0);
          this.carregandoLista.set(false);
        },
        error: (err) => {
          console.error('Erro ao carregar histórico de vendas:', err);
          this.vendas.set([]);
          this.totalPaginas.set(0);
          this.totalElementos.set(0);
          this.errorLista.set(
            'Falha ao carregar o histórico de vendas.'
          );
          this.carregandoLista.set(false);
        }
      });
  }

  carregarDetalhes(vendaId: number): void {
    if (this.carregandoDetalhe()) {
      return;
    }
    this.carregandoDetalhe.set(true);
    this.erroDetalhe.set(null);
    this.vendaSelecionada.set(null);
    this.vendaService
      .buscarPorId(vendaId)
      .pipe(finalize(() => {
        this.carregandoDetalhe.set(false);
      })
      )
      .subscribe({
        next: (venda) => {
          this.vendaSelecionada.set(venda);
          this.carregandoDetalhe.set(false);
        },
        error: (err) => {
          console.error('Error ao carregar detalhes da venda:', err);
          this.carregandoDetalhe.set(false);
          this.erroDetalhe.set(
            'Nao foi possível carregar os detalhes da venda.'
          )
        }
      })
  }

  irParaPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas() || pagina === this.paginaAtual()) {
      return;
    }

    this.paginaAtual.set(pagina);
  }

  limparVendaSelecionada(): void {
    this.vendaSelecionada.set(null);
  }

  limparErroDetalhe(): void {
    this.erroDetalhe.set(null);
  }

  ordenarPor(campo: string): void {
    if (this.campoOrdenacao() === campo) {
      this.direcaoOrdenacao.update(
        direcao => direcao === 'asc' ? 'desc' : 'asc'
      )
    } else {
      this.campoOrdenacao.set(campo);
      this.direcaoOrdenacao.set('asc');
    }
    this.paginaAtual.set(0);
  }

  primeiraPagina(): void {
    this.paginaAtual.set(0);
  }

  ultimaPagina(): void {
    this.paginaAtual.set(this.totalPaginas() -1);
  }

  quitarConta(vendaId: number): void {
    this.quitando.set(true);
    this.erroQuitacao.set(null);
    this.vendaService.registrarPagamento(vendaId)
    .pipe(
      finalize(() => {
        this.quitando.set(false);
      })
    )
    .subscribe({
      next: () => {
        this.quitando.set(false);
        this.quitacaoConcluida.update(valor => valor + 1);
      },
      error: (err) => {
        console.error('Erro ao quitar conta:', err);
        this.erroQuitacao.set('Erro ao processar a quitação no servidor.')
        this.quitando.set(false);
      }
    })
  }

  selecionarVenda(venda: VendaResponse): void {
    this.vendaSelecionada.set(venda);
  }

}

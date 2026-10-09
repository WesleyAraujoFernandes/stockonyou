import { inject, Injectable, signal } from '@angular/core';
import { VendaService } from '../../../core/services/venda.service';
import { PagamentoRequest } from '../../../core/model/pagamento.model';
import { FiltroHistoricoVenda, VendaResponse } from '../../../core/model/venda.model';
import { HistoricoVendaResponse } from '../../../core/model/venda.model';
import { finalize, Observable, switchMap, tap } from 'rxjs';

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
  readonly vendaDetalheCarregandoId = signal<number | null>(null);

  readonly quitando = signal(false);
  readonly erroQuitacao = signal<string | null>(null);

  readonly erroDetalhe = signal<string | null>(null);

  readonly quitacaoConcluida = signal<VendaResponse | null>(null);
  readonly finalizacaoConcluida = signal<VendaResponse | null>(null);

  readonly cancelando = signal(false);
  readonly erroCancelamento = signal<string | null>(null);
  readonly cancelamentoConcluido = signal<VendaResponse | null>(null);

  readonly vendaSelecionada = signal<VendaResponse | null>(null);

  readonly campoOrdenacao = signal('id');
  readonly direcaoOrdenacao = signal<'asc' | 'desc'>('desc');

  readonly totalFaturado = signal(0);
  readonly totalPendente = signal(0);
  readonly totalAberto = signal(0);
  readonly totalCancelado = signal(0);

  carregar(
    filtros: FiltroHistoricoVenda
  ): void {
    this.carregandoLista.set(true);
    this.errorLista.set(null);
    this.vendaSelecionada.set(null);
    this.vendaService
      .listarComFiltros(
        filtros.cliente,
        filtros.status,
        filtros.dataInicio,
        filtros.dataFim,
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
        next: (response: HistoricoVendaResponse) => {
          this.vendas.set(response.pagina.content ?? []);
          this.totalPaginas.set(response.pagina.totalPages ?? 0);
          this.totalElementos.set(response.pagina.totalElements ?? 0);

          this.totalFaturado.set(response.totalFaturado ?? 0);
          this.totalPendente.set(response.totalPendente ?? 0);
          this.totalAberto.set(response.totalAberto ?? 0);
          this.totalCancelado.set(response.totalCancelado ?? 0);
        },
        error: (err) => {
          console.error('Erro ao carregar histórico de vendas:', err);
          this.vendas.set([]);
          this.totalPaginas.set(0);
          this.totalElementos.set(0);
          this.errorLista.set(
            'Falha ao carregar o histórico de vendas.'
          );
        }
      });
  }

  carregarDetalhes(vendaId: number): void {
    if (this.carregandoDetalhe()) {
      return;
    }
    this.carregandoDetalhe.set(true);
    this.vendaDetalheCarregandoId.set(vendaId);
    this.erroDetalhe.set(null);
    this.vendaSelecionada.set(null);
    this.vendaService
      .buscarPorId(vendaId)
      .pipe(finalize(() => {
        this.carregandoDetalhe.set(false);
        this.vendaDetalheCarregandoId.set(null);
      })
      )
      .subscribe({
        next: (venda) => {
          this.vendaSelecionada.set(venda);
        },
        error: (err) => {
          console.error('Error ao carregar detalhes da venda:', err);
          this.erroDetalhe.set(
            'Nao foi possível carregar os detalhes da venda.'
          )
        }
      })
  }

  atualizarVendaSelecionada(vendaId: number): Observable<VendaResponse> {
    return this.vendaService.buscarPorId(vendaId).pipe(
      tap({
        next: (venda) => {
          this.vendaSelecionada.set(venda);
        },
        error: (err) => {
          console.error('Erro ao atualizar venda:', err);
          this.erroDetalhe.set('Não foi possível atualizar os dados da venda.')
        }
      })
    )
  }

  irParaPagina(pagina: number): boolean {
    if (pagina < 0 || pagina >= this.totalPaginas() || pagina === this.paginaAtual()) {
      return false;
    }

    this.paginaAtual.set(pagina);
    return true;
  }

  limparVendaSelecionada(): void {
    this.vendaSelecionada.set(null);
  }

  limparFinalizacaoConcluida(): void {
    this.finalizacaoConcluida.set(null);
  }

  limparErroDetalhe(): void {
    this.erroDetalhe.set(null);
  }

  limparErroQuitacao(): void {
    this.erroQuitacao.set(null);
  }

  limparErroCancelamento(): void {
    this.erroCancelamento.set(null);
  }

  limparOrdenacao(): void {
    this.campoOrdenacao.set('id');
    this.direcaoOrdenacao.set('desc');
  }

  limparQuitacaoConcluida(): void {
    this.quitacaoConcluida.set(null);
  }

  limparCancelamentoConcluido(): void {
    this.cancelamentoConcluido.set(null);
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
        next: (venda) => {
          this.quitacaoConcluida.set(venda);
        },
        error: (err) => {
          console.error('Erro ao quitar conta:', err);
          this.erroQuitacao.set('Erro ao processar a quitação no servidor.')
        }
      })
  }

  registrarPagamentoDetalhado(
    vendaId: number,
    request: PagamentoRequest
  ): void {
    this.vendaService
      .registrarPagamentoDetalhado(vendaId, request)
      .pipe(
        switchMap(() => this.atualizarVendaSelecionada(vendaId))
      )
      .subscribe({
        next: (vendaAtualizada) => {
          this.quitacaoConcluida.set(vendaAtualizada);
        },
        error: (err) => {
          console.error('Erro ao registrar o pagamento:', err)
          this.erroQuitacao.set(
            'O pagamento foi solicitado, mas não foi possível concluir a atualização da venda.'
          )
        }
      })
  }

  finalizarVenda(vendaId: number): void {
    this.vendaService.finalizarVenda(vendaId)
      .subscribe({
        next: (venda) => {
          this.vendaSelecionada.set(venda);
          this.finalizacaoConcluida.set(venda);
        },
        error: (err) => {
          console.error('Erro ao finalizar venda:', err);
        }
      })
  }

  cancelarVenda(vendaId: number): void {
    this.cancelando.set(true);
    this.erroCancelamento.set(null);
    this.vendaService
      .cancelarComanda(vendaId)
      .pipe(
        finalize(() => {
          this.cancelando.set(false);
        })
      )
      .subscribe({
        next: (venda) => {
          this.vendaSelecionada.set(venda);
          this.cancelamentoConcluido.set(venda);
        },
        error: (err) => {
          console.error('Erro ao cancelar venda:', err);
          this.erroCancelamento.set(
            'Erro ao cancelar a venda no servidor.'
          )
        }
      })
  }

  selecionarVenda(venda: VendaResponse): void {
    this.vendaSelecionada.set(venda);
  }

}

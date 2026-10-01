import { Component, inject, OnInit, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../core/services/toast.service';
import { FiltroHistoricoVenda, StatusVenda, VendaResponse } from '../../core/model/venda.model';
import { HistoricoVendaStore } from './store/historico-venda.store';
import { ConfirmacaoModal } from './confirmacao-modal/confirmacao-modal';
import {
  LucideDynamicIcon,
  LucideCalendar,
  LucideCheckCircle,
  LucideUser,
  LucideChevronLeft,
  LucideChevronRight,
  LucideEye,
  LucideLoaderCircle,
  LucideAlertTriangle,

  LucideChevronsLeft,
  LucideChevronsRight
} from '@lucide/angular';

@Component({
  selector: 'app-historico-venda',
  imports: [CommonModule, FormsModule, LucideDynamicIcon, ConfirmacaoModal],
  templateUrl: './historico-venda.html',
  styleUrl: './historico-venda.css',
  providers: [HistoricoVendaStore]
})
export class HistoricoVenda implements OnInit {
  private readonly toast = inject(ToastService);

  readonly IconCalendar = LucideCalendar;
  readonly IconCheck = LucideCheckCircle;
  readonly IconUser = LucideUser;
  readonly IconLeft = LucideChevronLeft;
  readonly IconRight = LucideChevronRight;
  readonly IconEye = LucideEye;
  readonly IconLoader = LucideLoaderCircle;
  readonly IconAlert = LucideAlertTriangle;
  readonly IconFirst = LucideChevronsLeft;
  readonly IconLast = LucideChevronsRight;

  readonly exibirModalDetalhes = computed(
      () => this.historicoStore.vendaSelecionada() !== null
  )
  readonly historicoStore = inject(HistoricoVendaStore);

  exibirModalConfirmacaoCancelamento = signal(false);
  vendaParaCancelar = signal<number | null>(null);

  exibirModalConfirmacaoQuitacao = signal(false);
  vendaParaQuitar = signal<number | null>(null);

  filtros: FiltroHistoricoVenda = {
    cliente: '',
    status: '',
    dataInicio: '',
    dataFim: ''
  }

  constructor() {
    effect(() => {
      const quitacao = this.historicoStore.quitacaoConcluida();

      if (!quitacao) {
        return;
      }

      this.toast.sucesso(
        'Conta quitada com sucesso! Fluxo de caixa atualizado!'
      );

      this.exibirModalConfirmacaoQuitacao.set(false);
      this.vendaParaQuitar.set(null);

      this.historicoStore.limparVendaSelecionada();
      this.carregarHistorico();

      this.historicoStore.limparQuitacaoConcluida();
    });

    effect(() => {
      const erro = this.historicoStore.erroQuitacao();
      if (!erro) {
        return;
      }
      this.toast.erro(erro);
      this.historicoStore.limparErroQuitacao();
    })

    effect(() => {
      const cancelamento = this.historicoStore.cancelamentoConcluido();

      if (!cancelamento) {
        return;
      }

      this.toast.sucesso(
        'Venda cancelada com sucesso!'
      );

      this.historicoStore.limparVendaSelecionada();
      this.carregarHistorico();

      this.historicoStore.limparCancelamentoConcluido();
    });

    effect(() => {
      const erro = this.historicoStore.erroCancelamento();
      if (!erro) {
        return;
      }
      this.toast.erro(erro);
      this.historicoStore.limparErroCancelamento();
    })

    effect(() => {
      const erro = this.historicoStore.erroDetalhe();
      if (!erro) {
        return;
      }
      this.toast.erro(erro);
      this.historicoStore.limparErroDetalhe();
    })
  }

  ngOnInit(): void {
    this.carregarHistorico();
  }

  abrirDetalhes(venda: VendaResponse): void {
    if (this.historicoStore.carregandoDetalhe()) {
      return;
    }
    this.historicoStore.carregarDetalhes(venda.id);
  }

  aplicarFiltros(): void {
    this.historicoStore.primeiraPagina();
    this.carregarHistorico();
  }

  cancelarConfirmacao(): void {
    this.exibirModalConfirmacaoCancelamento.set(false);
    this.vendaParaCancelar.set(null);
  }

  cancelarConfirmacaoQuitacao(): void {
    this.exibirModalConfirmacaoQuitacao.set(false);
    this.vendaParaQuitar.set(null);
  }

  cancelarVenda(vendaId: number): void {
    this.vendaParaCancelar.set(vendaId);
    this.exibirModalConfirmacaoCancelamento.set(true);
  }

  carregarHistorico(): void {
    this.historicoStore.carregar(this.filtros);
  }

  confirmarCancelamento(): void {
    const vendaId = this.vendaParaCancelar();
    if (vendaId === null) {
      return;
    }
    this.exibirModalConfirmacaoCancelamento.set(false);
    this.vendaParaCancelar.set(null);
    this.historicoStore.cancelarVenda(vendaId);
  }

  confirmarQuitacao(): void {
    const vendaId = this.vendaParaQuitar();
    if (vendaId === null) {
      return;
    }
    this.historicoStore.quitarConta(vendaId);
  }

  fecharDetalhes(): void {
    this.historicoStore.limparVendaSelecionada();
    this.historicoStore.limparErroDetalhe();
  }

  irParaPaginaInformada(valor: string): void {
    const pagina = Number(valor);
    if (!Number.isInteger(pagina) ||
        pagina < 1 ||
        pagina > this.historicoStore.totalPaginas()

    ) {
      return;
    }
    this.navegarParaPagina(pagina - 1);

  }

  limparFiltros(): void {
    this.filtros = {
      cliente: '',
      status: '',
      dataInicio: '',
      dataFim: ''
    }
    this.historicoStore.primeiraPagina();
    this.historicoStore.limparOrdenacao();
    this.carregarHistorico();
  }

  mudarPagina(direcao: number): void {
    const novaPagina = this.historicoStore.paginaAtual() + direcao;
    if (
      novaPagina < 0 ||
      novaPagina >= this.historicoStore.totalPaginas()
    ) {
      return;
    }
    this.navegarParaPagina(novaPagina);
  }

  obterClassesStatus(status: StatusVenda): string {
    switch (status) {
      case 'ABERTA':
        return 'text-blue-400'
      case 'PENDENTE':
        return 'text-amber-400'
      case 'PAGO':
        return 'text-emerald-400'
      case 'CANCELADA':
        return 'text-red-400'
    }
  }

  obterTextoStatus(status: StatusVenda): string {
    switch (status) {
      case 'ABERTA':
        return 'Aberta'
      case 'PENDENTE':
        return 'Pendente'
      case 'PAGO':
        return 'Pago'
      case 'CANCELADA':
        return 'Cancelada'
    }
  }

  ordenarPor(campo: string): void {
    this.historicoStore.ordenarPor(campo);
    this.carregarHistorico();
  }

  podeCancelar(venda: VendaResponse | null): boolean {
    return venda?.status === 'ABERTA';
  }

  podeQuitar(venda: VendaResponse | null): boolean {
    return venda?.status === 'PENDENTE';
  }

  quitarContaPendurada(vendaId: number): void {
    this.vendaParaQuitar.set(vendaId);
    this.exibirModalConfirmacaoQuitacao.set(true);
  }

  private navegarParaPagina(pagina: number): void {
    const mudou = this.historicoStore.irParaPagina(pagina);

    if (mudou) {
      this.carregarHistorico();
    }
  }

}

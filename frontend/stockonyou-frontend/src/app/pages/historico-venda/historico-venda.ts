import { Component, inject, OnInit, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VendaService } from '../../core/services/venda.service';
import { ToastService } from '../../core/services/toast.service';
import { VendaResponse } from '../../core/model/venda.model';
import { HistoricoVendaStore } from './store/historico-venda.store';
import {
  LucideDynamicIcon,
  LucideSearch,
  LucideCalendar,
  LucideDollarSign,
  LucideCheckCircle,
  LucideClock,
  LucideUser,
  LucideChevronLeft,
  LucideChevronRight,
  LucideEye,
  LucideLoaderCircle,
  LucideAlertTriangle,
  LucideChevronFirst,
  LucideChevronLast,
  LucideChevronsLeft,
  LucideChevronsRight
} from '@lucide/angular';
import { KeycloakService } from '../../core/auth/keycloak.service';

@Component({
  selector: 'app-historico-venda',
  imports: [CommonModule, FormsModule, LucideDynamicIcon],
  templateUrl: './historico-venda.html',
  styleUrl: './historico-venda.css',
  providers: [HistoricoVendaStore]
})
export class HistoricoVenda implements OnInit {
  private readonly toast = inject(ToastService);
  private readonly keycloakService = inject(KeycloakService);
  //private readonly vendaService = inject(VendaService);

  readonly IconSearch = LucideSearch;
  readonly IconCalendar = LucideCalendar;
  readonly IconMoney = LucideDollarSign;
  readonly IconCheck = LucideCheckCircle;
  readonly IconClock = LucideClock;
  readonly IconUser = LucideUser;
  readonly IconLeft = LucideChevronLeft;
  readonly IconRight = LucideChevronRight;
  readonly IconEye = LucideEye;
  readonly IconLoader = LucideLoaderCircle;
  readonly IconAlert = LucideAlertTriangle;
  readonly IconFirst = LucideChevronsLeft;
  readonly IconLast = LucideChevronsRight;

  profile = this.keycloakService.getUserProfile();
  name = this.keycloakService.getUserDisplayName();

  readonly historicoStore = inject(HistoricoVendaStore);
  readonly vendas = this.historicoStore.vendas;
  readonly paginaAtual = this.historicoStore.paginaAtual;
  readonly totalPaginas = this.historicoStore.totalPaginas;
  readonly totalElementos = this.historicoStore.totalElementos;
  readonly carregandoLista = this.historicoStore.carregandoLista;
  readonly errorLista = this.historicoStore.errorLista;
  readonly quitando = this.historicoStore.quitando;
  readonly erroDetalhe = this.historicoStore.erroDetalhe;
  readonly erroQuitacao = this.historicoStore.erroQuitacao;
  readonly quitacaoConcluida =
    this.historicoStore.quitacaoConcluida;
  readonly cancelando = this.historicoStore.cancelando;
  readonly erroCancelamento = this.historicoStore.erroCancelamento;
  readonly cancelamentoConcluido = this.historicoStore.cancelamentoConcluido;
  readonly vendaSelecionada = this.historicoStore.vendaSelecionada;
  readonly carregandoDetalhe = this.historicoStore.carregandoDetalhe;

  private ultimaQuitacaoProcessada = 0;
  private ultimoCancelamentoProcessado = 0;


  //vendaDetalhada = signal<VendaResponse | null>(null);
  exibirModalDetalhes = signal<boolean>(false);

  filtroCliente = '';
  filtroStatus = '';
  filtroDataInicio = '';
  filtroDataFim = '';

  totalFaturado = computed(() => {
    return this.vendas()
      .filter(v => v.status === 'PAGO')
      .reduce((acc, v) => acc + v.valorTotal, 0);
  })

  totalPendente = computed(() => {
    return this.vendas()
      .filter(v => v.status === 'PENDENTE')
      .reduce((acc, v) => acc + v.valorTotal, 0);
  })

  constructor() {
    effect(() => {
      const quitacao = this.quitacaoConcluida();

      if (
        quitacao === 0 ||
        quitacao === this.ultimaQuitacaoProcessada
      ) {
        return;
      }

      this.ultimaQuitacaoProcessada = quitacao;

      this.toast.sucesso(
        'Conta quitada com sucesso! Fluxo de caixa atualizado!'
      );

      this.exibirModalDetalhes.set(false);
      this.carregarHistorico();
    });

    effect(() => {
      const erro = this.erroQuitacao();
      if (!erro) {
        return;
      }
      this.toast.erro(erro);
    })

    effect(() => {
      const cancelamento = this.cancelamentoConcluido();

      if (
        cancelamento === 0 ||
        cancelamento === this.ultimoCancelamentoProcessado
      ) {
        return;
      }

      this.ultimoCancelamentoProcessado = cancelamento;

      this.toast.sucesso(
        'Venda cancelada com sucesso!'
      );

      this.exibirModalDetalhes.set(false);
      this.carregarHistorico();
    });

    effect(() => {
      const erro = this.erroCancelamento();
      if (!erro) {
        return;
      }
      this.toast.erro(erro);
    })

    effect(() => {
      const carregando = this.carregandoDetalhe();
      const venda = this.vendaSelecionada();
      if (carregando || !venda) {
        return;
      }
      this.exibirModalDetalhes.set(true);
    })

    effect(() => {
      const erro = this.erroDetalhe();
      if (!erro) {
        return;
      }
      this.toast.erro(erro);
    })
  }


  ngOnInit(): void {
    this.carregarHistorico();
  }

  carregarHistorico(): void {
    this.historicoStore.carregar(
      this.filtroCliente,
      this.filtroStatus,
      this.filtroDataInicio,
      this.filtroDataFim
    )
  }

  aplicarFiltros(): void {
    this.historicoStore.primeiraPagina();
    this.carregarHistorico();
  }

  limparFiltros(): void {
    this.filtroCliente = '';
    this.filtroStatus = '';
    this.filtroDataInicio = '';
    this.filtroDataFim = '';
    this.historicoStore.primeiraPagina();
    this.historicoStore.limparOrdenacao();
    this.carregarHistorico();
  }

  mudarPagina(direcao: number): void {
    const novaPagina = this.paginaAtual() + direcao;
    if (novaPagina >= 0 && novaPagina < this.totalPaginas()) {
      const mudou = this.historicoStore.irParaPagina(novaPagina);
      if (mudou) {
        this.carregarHistorico();
      }
    }
  }

  irParaPaginaInformada(valor: string): void {
    const pagina = Number(valor);
    if (!Number.isInteger(pagina) || pagina < 1 || pagina > this.totalPaginas()) {
      return;
    }
    const mudou = this.historicoStore.irParaPagina(pagina - 1);
    if (mudou) {
      this.carregarHistorico();
    }
  }

  ordenarPor(campo: string): void {
    this.historicoStore.ordenarPor(campo);
    this.carregarHistorico();
  }

  abrirDetalhes(venda: VendaResponse): void {
    if (this.carregandoDetalhe()) {
      return;
    }
    this.historicoStore.carregarDetalhes(venda.id);
  }

  fecharDetalhes(): void {
    this.exibirModalDetalhes.set(false);
    this.historicoStore.limparVendaSelecionada();
    this.historicoStore.limparErroDetalhe();
  }

  quitarContaPendurada(vendaId: number): void {
    const desejaQuitar = confirm(
      'Confirma o recebimento total e quitação desta conta pendurada?'
    );

    if (!desejaQuitar) {
      return;
    }

    this.historicoStore.quitarConta(vendaId);
  }

  cancelarVenda(vendaId: number): void {
    const desejaCancelar = confirm(
      'Confirma o cancelamento desta venda?'
    )
    if (!desejaCancelar) {
      return;
    }
    this.historicoStore.cancelarVenda(vendaId);
  }

}

import { Component, inject, OnInit, signal, computed } from '@angular/core';
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
  LucideAlertTriangle
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
  private readonly vendaService = inject(VendaService);
  private readonly toast = inject(ToastService);
  private readonly keycloakService = inject(KeycloakService);
  private readonly historicoStore = inject(HistoricoVendaStore);

  readonly IconSearch = LucideSearch;
  readonly IconCalendar = LucideCalendar;
  readonly IconMoney = LucideDollarSign;
  readonly IconCheck = LucideCheckCircle;
  readonly IconClock = LucideClock;
  readonly IconUser = LucideUser;
  readonly IconLeft = LucideChevronLeft;
  readonly IconRight = LucideChevronRight;
  readonly IconEye = LucideEye;
  readonly IconAlert = LucideAlertTriangle;

  profile = this.keycloakService.getUserProfile();
  name = this.keycloakService.getUserDisplayName();

  readonly vendas = this.historicoStore.vendas;
  readonly paginaAtual = this.historicoStore.paginaAtual;
  readonly totalPaginas = this.historicoStore.totalPaginas;
  readonly totalElementos = this.historicoStore.totalElementos;

  vendaDetalhada = signal<VendaResponse | null>(null);
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
    this.filtroDataInicio= '';
    this.filtroDataFim = '';
    this.historicoStore.primeiraPagina();
    this.carregarHistorico();
  }

  mudarPagina(direcao: number): void {
    const novaPagina = this.paginaAtual() + direcao;
    if (novaPagina >= 0 && novaPagina < this.totalPaginas()) {
      this.historicoStore.irParaPagina(novaPagina);
      this.carregarHistorico();
    }
  }

  abrirDetalhes(venda: VendaResponse): void {
    this.vendaDetalhada.set(venda);
    this.exibirModalDetalhes.set(true);
  }

  quitarContaPendurada(vendaId: number): void {
    const desejaQuitar = confirm('Confirma o recebimento total e quitação desta conta pendurada?')
    if (!desejaQuitar) return;

    this.vendaService.registrarPagamento(vendaId).subscribe({
      next: () => {
        this.toast.sucesso('Conta quitada com sucesso! Fluxo de caixa atualizado!')
        this.exibirModalDetalhes.set(false);
        this.carregarHistorico()
      },
      error: (err) => {
        console.error('Erro ao quitar conta:',err)
        this.toast.erro('Erro ao processar a quitação no servidor.');
      }
    })
  }
}

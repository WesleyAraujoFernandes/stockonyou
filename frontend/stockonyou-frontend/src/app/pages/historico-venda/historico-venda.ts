import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VendaService } from '../../core/services/venda.service';
import { ToastService } from '../../core/services/toast.service';
import { VendaResponse } from '../../core/model/venda.model';
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
  LucideEye
} from '@lucide/angular';

@Component({
  selector: 'app-historico-venda',
  imports: [CommonModule, FormsModule, LucideDynamicIcon],
  templateUrl: './historico-venda.html',
  styleUrl: './historico-venda.css',
})
export class HistoricoVenda implements OnInit {
  private readonly vendaService = inject(VendaService);
  private readonly toast = inject(ToastService);

  readonly IconSearch = LucideSearch;
  readonly IconCalendar = LucideCalendar;
  readonly IconMoney = LucideDollarSign;
  readonly IconCheck = LucideCheckCircle;
  readonly IconClock = LucideClock;
  readonly IconUser = LucideUser;
  readonly IconLeft = LucideChevronLeft;
  readonly IconRight = LucideChevronRight;
  readonly IconEye = LucideEye;

  vendas = signal<VendaResponse[]>([]);
  vendaDetalhada = signal<VendaResponse | null>(null);
  exibirModalDetalhes = signal<boolean>(false);

  filtroCliente = '';
  filtroStatus = '';

  paginaAtual = signal<number>(0);
  totalPaginas = signal<number>(0);
  totalElementos = signal<number>(0);
  itensPorPagina = 10;

  ngOnInit(): void {
    this.carregarHistorico();
  }

  carregarHistorico(): void {
    this.vendaService.listarComFiltros(
      this.filtroCliente,
      this.filtroStatus,
      undefined,
      undefined,
      this.paginaAtual(),
      this.itensPorPagina
    ).subscribe({
      next: (response: any) => {
        this.vendas.set(response.content || []);
        this.totalPaginas.set(response.totalPages || 0);
        this.totalElementos.set(response.totalElements || 0);
      },
      error: (err) => {
        console.error('Erro ao carregar histórico:', err);
        this.toast.erro('Falha ao carregar o histórico de vendas.');
      }
    });
  }

  aplicarFiltros(): void {
    this.paginaAtual.set(0);
    this.carregarHistorico();
  }

  limparFiltros(): void {
    this.filtroCliente = '';
    this.filtroStatus = '';
    this.paginaAtual.set(0);
    this.carregarHistorico();
  }

  mudarPagina(direcao: number): void {
    const novaPagina = this.paginaAtual() + direcao;
    if (novaPagina >= 0 && novaPagina < this.totalPaginas()) {
      this.paginaAtual.set(novaPagina);
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

    this.vendaService.finalizarComanda(vendaId, 'PAGO').subscribe({
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

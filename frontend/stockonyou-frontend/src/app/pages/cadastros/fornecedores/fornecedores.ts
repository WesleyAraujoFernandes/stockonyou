import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FornecedorService } from '../../../core/services/fornecedor.service';
import { ToastService } from '../../../core/services/toast.service';
import { Fornecedor } from '../../../core/model/fornecedor.model';
import { PageResponse } from '../../../core/model/page-response.model';

@Component({
  selector: 'app-fornecedores',
  imports: [CommonModule, FormsModule],
  templateUrl: './fornecedores.html',
  styleUrl: './fornecedores.css',
})
export class Fornecedores implements OnInit {
  private readonly fornecedorService = inject(FornecedorService)
  private readonly toast = inject(ToastService)

  readonly fornecedores = signal<Fornecedor[]>([])
  readonly termoBusca = signal('');
  readonly paginaAtual = signal(0);
  readonly tamanhoPagina = signal(10);
  readonly totalElementos = signal(0);
  readonly totalPaginas = signal(0);
  readonly carregando = signal(false);

  private temporizadorBusca?: ReturnType<typeof setTimeout>;

  ngOnInit(): void {
    this.carregarFornecedores();
  }

  carregarFornecedores(): void {
    this.carregando.set(true);

    this.fornecedorService.listarComFiltros(
      this.termoBusca(),
      this.paginaAtual(),
      this.tamanhoPagina()
    ).subscribe({
      next: (resposta: PageResponse<Fornecedor>) => {
        this.fornecedores.set(resposta.content ?? []);
        this.totalElementos.set(resposta.totalElements);
        this.totalPaginas.set(resposta.totalPages);
        this.carregando.set(false);
      },
      error: (erro) => {
        console.error('Erro ao carregar fornecedores:', erro);
        this.fornecedores.set([]);
        this.totalElementos.set(0);
        this.totalPaginas.set(0);
        this.carregando.set(false);
        this.toast.erro('Erro ao carregar a lista de fornecedores')
      }
    })
  }

  pesquisar(termo: string): void {
    this.termoBusca.set(termo);
    this.paginaAtual.set(0);
    if (this.temporizadorBusca) {
      clearTimeout(this.temporizadorBusca);
    }
    this.temporizadorBusca = setTimeout(() => {
      this.carregarFornecedores();
    }, 300)
  }

  irParaPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas()) {
      return;
    }
    this.paginaAtual.set(pagina);
    this.carregarFornecedores();
  }

  paginaAnterior(): void {
    this.irParaPagina(this.paginaAtual() - 1)
  }

  proximaPagina(): void {
    this.irParaPagina(this.paginaAtual() + 1)
  }
}

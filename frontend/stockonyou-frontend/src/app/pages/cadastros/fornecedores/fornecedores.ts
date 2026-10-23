import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FornecedorService } from '../../../core/services/fornecedor.service';
import { ToastService } from '../../../core/services/toast.service';
import { Fornecedor } from '../../../core/model/fornecedor.model';
import { PageResponse } from '../../../core/model/page-response.model';
import { Cidade } from '../../../core/model/cidade.model';
import { FornecedorRequest } from '../../../core/model/fornecedor.model';
import { CidadeService } from '../../../core/services/cidade.service';
import { forkJoin } from 'rxjs';
import { LucideDynamicIcon, LucidePlus, LucideX } from '@lucide/angular';

@Component({
  selector: 'app-fornecedores',
  imports: [CommonModule, FormsModule, LucideDynamicIcon],
  templateUrl: './fornecedores.html',
  styleUrl: './fornecedores.css',
})
export class Fornecedores implements OnInit {
  private readonly fornecedorService = inject(FornecedorService)
  private readonly toast = inject(ToastService)
  private readonly cidadeService = inject(CidadeService);
  readonly IconPlus = LucidePlus;
  readonly IconX = LucideX;

  readonly cidades = signal<Cidade[]>([]);
  readonly carregandoCidades = signal(false);
  readonly salvando = signal(false);
  readonly exibirModalCadastro = signal(false);
  readonly fornecedores = signal<Fornecedor[]>([])
  readonly termoBusca = signal('');
  readonly paginaAtual = signal(0);
  readonly tamanhoPagina = signal(10);
  readonly totalElementos = signal(0);
  readonly totalPaginas = signal(0);
  readonly carregando = signal(false);

  novoFornecedor: {
    nome: string;
    email: string;
    telefone: string;
    cnpj: string;
    endereco: string;
    cep: string;
    observacao: string;
    cidadeId: number | null;
  } = this.novoFormularioFornecedor();

  private novoFormularioFornecedor() {
    return {
      nome: '',
      email: '',
      telefone: '',
      cnpj: '',
      endereco: '',
      cep: '',
      observacao: '',
      cidadeId: null,
    }
  }

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

  carregarCidades(): void {
    this.carregandoCidades.set(true);
    this.cidadeService.listarTodas(0, 100).subscribe({
      next: (primeiraPagina) => {
        const paginasAdicionais = Array.from(
          {
            length: Math.max(0, primeiraPagina.totalPages -1)
          },
          (_, indice) => indice + 1
        )
        if (paginasAdicionais.length === 0) {
          this.cidades.set(primeiraPagina.content ?? [])
          this.carregandoCidades.set(false);
          return;
        }
        forkJoin(
          paginasAdicionais.map((paginas) =>
            this.cidadeService.listarTodas(paginas, 100)
          )
        ).subscribe({
          next: (demaisPaginas) => {
            this.cidades.set([
              ...(primeiraPagina.content ?? []),
              ...demaisPaginas.flatMap((pagina) => pagina.content ?? []),
            ]);
            this.carregandoCidades.set(false);
          }
        })
      },
      error: (erro) => {
        console.error('Erro ao carregar cidades: ',erro);
        this.cidades.set([]);
        this.carregandoCidades.set(false);
        this.toast.erro('Falha ao carregar as cidades');
      }
    })
  }

  abrirCadastro(): void {
    this.novoFornecedor = this.novoFormularioFornecedor();
    this.exibirModalCadastro.set(true);

    if (this.cidades().length === 0) {
      this.carregarCidades();
    }
  }

  fecharCadastro(): void {
    if (this.salvando()) {
      return;
    }
    this.exibirModalCadastro.set(false);
    this.novoFornecedor = this.novoFormularioFornecedor();
  }

  salvarFornecedor(): void {
    const cidadeId = this.novoFornecedor.cidadeId;
    if(!cidadeId || this.salvando()) {
      return;
    }
    const payload: FornecedorRequest = {
      nome: this.novoFornecedor.nome.trim(),
      email: this.novoFornecedor.email.trim(),
      telefone: this.novoFornecedor.telefone.trim(),
      cnpj: this.novoFornecedor.cnpj.trim(),
      endereco: this.novoFornecedor.endereco.trim(),
      cep: this.novoFornecedor.cep.trim(),
      observacao: this.novoFornecedor.observacao.trim(),
      cidadeId
    };
    this.salvando.set(true);
    this.fornecedorService.criar(payload).subscribe ({
      next: () => {
        this.salvando.set(false);
        this.exibirModalCadastro.set(false);
        this.novoFornecedor = this.novoFormularioFornecedor();
        this.paginaAtual.set(0);
        this.carregarFornecedores();
        this.toast.sucesso('Fornecedor cadastrado com sucesso!');
      },
      error: (erro) => {
        console.error('Erro ao cadastrar fornecedor: ', erro)
        this.salvando.set(false);
        this.toast.erro('Falha ao cadastrar o fornecedor.')
      }
    })
  }
  abrirModal(fornecedor?: Fornecedor): void {
  }
}

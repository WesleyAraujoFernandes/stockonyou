import { Component, inject, OnInit, signal } from '@angular/core';
import { ProdutoService } from '../../core/services/produto.service';
import { ToastService } from '../../core/services/toast.service';
import { CategoriaService } from '../../core/services/categoria.service'; // Adicionado para carregar o select
import { Produto, Categoria } from '../../core/model/produto.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideAlertTriangle,
  LucideBox,
  LucideDollarSign,
  LucideDynamicIcon,
  LucideEdit,
  LucideEraser,
  LucidePlus,
  LucideSearch,
  LucideTrash2,
  LucideX
} from '@lucide/angular';

@Component({
  selector: 'app-produtos',
  imports: [
    CommonModule,
    FormsModule,
    LucideDynamicIcon,
    //LucideSearch,
    //LucideEraser,
    //LucidePlus,
    LucideEdit,
    LucideTrash2,
    LucideX,
    LucideAlertTriangle,
    //LucideBox,
    //LucideDollarSign
  ],
  templateUrl: './produtos.html',
  styleUrl: './produtos.css',
})
export class Produtos implements OnInit {
  private readonly produtoService = inject(ProdutoService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly toast = inject(ToastService); // 2. INJEÇÃO DO TOAST GLOBAL

  // Referências dos Ícones p/ Templates Estáticos ou Dinâmicos
  readonly IconPlus = LucidePlus;
  readonly IconEreaser = LucideEraser;
  readonly IconSearch = LucideSearch;
  readonly IconMoney = LucideDollarSign;

  // Signals de Dados
  produtos = signal<Produto[]>([]);
  categorias = signal<Categoria[]>([]); // Lista utilizada no combo do formulário

  // Estado dos Filtros
  filtroNome = '';
  filtroPrecoFaixa = signal<string>('');
  categoriasSelecionadas = signal<number[]>([]);
  //filtroPrecoMin?: number;
  //filtroPrecoMax?: number;

  // Signals de Controle dos Modais
  exibirModal = signal<boolean>(false);
  exibirModalExclusao = signal<boolean>(false);
  produtoEmEdicao = signal<Produto | null>(null);
  produtoParaExcluir = signal<Produto | null>(null);

  // Estrutura auxiliar para manipulação do formulário (POST/PUT)
  // Mapeamos categoriaId de forma simples para ligar ao <select>
  novoProduto = {
    nome: '',
    codigoBarras: '',
    quantidade: 0,
    quantidadeMinima: 0,
    preco: 0,
    categoriaId: ''
  };

  ngOnInit(): void {
    this.carregarProdutos();
    this.carregarCategorias();
  }

  carregarProdutos(): void {
    let precoMin: number | undefined = undefined;
    let precoMax: number | undefined = undefined;

    const faixa = this.filtroPrecoFaixa();

    if (faixa === 'ate10') {
      precoMax = 10;
    } else if (faixa === 'ate20') {
      precoMax = 20;
    } else if (faixa === 'ate50') {
      precoMax = 50;
    } else if (faixa === 'ate100') {
      precoMax = 100;
    } else if (faixa === 'mais100') {
      precoMin = 100.01;
    }

    const categoriaIds = this.categoriasSelecionadas().length > 0
      ? this.categoriasSelecionadas()
      : undefined;

    this.produtoService
      .listarComFiltros(this.filtroNome, precoMin, precoMax, categoriaIds)
      .subscribe({
        next: (response) => this.produtos.set(response.content || []),
        error: (err) => {
          console.error('Erro ao carregar produto:', err);
          this.toast.erro('Falha ao carregar a lista de produtos.');
        },
      });
  }

  alternarCategoria(id: number, checked: boolean): void {
    if (checked) {
      // Adiciona o ID de forma imutável abrindo a lista antiga
      this.categoriasSelecionadas.update(lista => [...lista, id]);
    } else {
      // Remove o ID filtrando a lista antiga
      this.categoriasSelecionadas.update(lista => lista.filter(catId => catId !== id));
    }

    // Dispara a requisição com o novo array consolidado
    this.carregarProdutos();
  }


  carregarCategorias(): void {
    this.categoriaService.listarTodas().subscribe({
      next: (data) => this.categorias.set(Array.isArray(data) ? data : []),
      error: (err) => console.error('Erro ao carregar categorias para formulário:', err)
    });
  }

  limparFiltros(): void {
    this.filtroNome = '';
    this.filtroPrecoFaixa.set('');
    this.categoriasSelecionadas.set([]);
    this.carregarProdutos();
  }

  abrirModal(produto?: Produto): void {
    if (produto) {
      this.produtoEmEdicao.set(produto);
      this.novoProduto = {
        nome: produto.nome,
        codigoBarras: produto.codigoBarras,
        quantidade: produto.quantidade,
        quantidadeMinima: produto.quantidadeMinima,
        preco: produto.preco,
        categoriaId: produto.categoria?.id?.toString() || ''
      };
    } else {
      this.produtoEmEdicao.set(null);
      this.novoProduto = {
        nome: '',
        codigoBarras: '',
        quantidade: 0,
        quantidadeMinima: 0,
        preco: 0,
        categoriaId: ''
      };
    }
    this.exibirModal.set(true);
  }

  fecharModal(): void {
    this.exibirModal.set(false);
    this.produtoEmEdicao.set(null);
  }

  salvar(): void {
    // Monta o payload conforme a interface Produto esperada pelo Backend
    const payload = {
      nome: this.novoProduto.nome,
      codigoBarras: this.novoProduto.codigoBarras,
      quantidade: this.novoProduto.quantidade,
      quantidadeMinima: this.novoProduto.quantidadeMinima,
      preco: this.novoProduto.preco,
      categoriaId: Number(this.novoProduto.categoriaId)
    };

    const prodEditando = this.produtoEmEdicao();

    if (prodEditando) {
      // Fluxo de Atualização (PUT)
      this.produtoService.atualizar(prodEditando.id, payload as any).subscribe({
        next: () => {
          this.carregarProdutos();
          this.fecharModal();
          this.toast.sucesso('Produto atualizado com sucesso!');
        },
        error: (err) => {
          console.error('Erro ao atualizar produto:', err);
          this.toast.erro('Falha ao atualizar o produto.')
        }
      });
    } else {
      // Fluxo de Criação (POST)
      this.produtoService.criar(payload as any).subscribe({
        next: () => {
          this.carregarProdutos();
          this.fecharModal();
          this.toast.sucesso('Produto cadastrado com sucesso!');
        },
        error: (err) => {
          console.error('Erro ao cadastrar produto:', err);
          this.toast.erro('Falha ao cadastrar o produto');
        }

      });
    }
  }

  confirmarExclusao(produto: Produto): void {
    this.produtoParaExcluir.set(produto);
    this.exibirModalExclusao.set(true);
  }

  fecharModalExclusao(): void {
    this.exibirModalExclusao.set(false);
    this.produtoParaExcluir.set(null);
  }

  executarExclusao(): void {
    const produto = this.produtoParaExcluir();
    if (!produto) return;

    this.produtoService.excluir(produto.id).subscribe({
      next: () => {
        this.carregarProdutos();
        this.fecharModalExclusao();
      },
      error: (err) => console.error('Erro ao deletar produto:', err)
    });
  }
}

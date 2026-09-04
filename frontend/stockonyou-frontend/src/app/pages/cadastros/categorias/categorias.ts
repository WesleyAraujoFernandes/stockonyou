import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CategoriaService } from '../../../core/services/categoria.service';
import { ToastService } from '../../../core/services/toast.service'; // 1. INJETAR O NOVO SERVIÇO GLOBAL
import { Categoria } from '../../../core/model/produto.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideDynamicIcon,
  LucideSearch,
  LucidePlus,
  LucideEdit,
  LucideTrash2,
  LucideX,
  LucideAlertTriangle
} from '@lucide/angular';

@Component({
  selector: 'app-categorias',
  imports: [
    CommonModule,
    FormsModule,
    LucideDynamicIcon,
    LucideSearch,
    //LucidePlus,
    LucideEdit,
    LucideTrash2,
    LucideX,
    LucideAlertTriangle
  ],
  templateUrl: './categorias.html',
  styleUrl: './categorias.css',
})
export class Categorias implements OnInit {
  private readonly categoriaService = inject(CategoriaService);
  private readonly toast = inject(ToastService); // 2. INJEÇÃO DO TOAST GLOBAL
  private readonly listaCategoriaRaw = signal<Categoria[]>([]);

  readonly IconPlus = LucidePlus;

  termoBusca = signal<string>('');
  novaCategoria: Partial<Categoria> = { nome: '', descricao: '' };
  categoriaParaExcluir = signal<Categoria | null>(null);
  categoriaEmEdicao = signal<Categoria | null>(null);

  exibirModal = signal<boolean>(false);
  exibirModalExclusao = signal<boolean>(false);

  categoriasFiltradas = computed(() => {
    const termo = this.termoBusca().toLowerCase().trim();
    const lista = this.listaCategoriaRaw();

    if (!termo) return lista;

    return lista.filter(cat =>
      cat.nome.toLowerCase().includes(termo) ||
      (cat.descricao && cat.descricao.toLowerCase().includes(termo))
    );
  });

  ngOnInit(): void {
    this.carregarCategorias();
  }

  carregarCategorias(): void {
    this.categoriaService.listarTodas().subscribe({
      next: (data) => {
        this.listaCategoriaRaw.set(Array.isArray(data) ? data : []);
      },
      error: (err) => {
        console.error('Erro ao carregar categorias:', err);
        this.listaCategoriaRaw.set([]);
        this.toast.erro('Erro ao carregar a lista de categorias.'); // 3. TOAST DE ERRO GLOBAL
      }
    });
  }

  abrirModal(categoria?: Categoria): void {
    if (categoria) {
      this.categoriaEmEdicao.set(categoria);
      this.novaCategoria = { nome: categoria.nome, descricao: categoria.descricao };
    } else {
      this.categoriaEmEdicao.set(null);
      this.novaCategoria = { nome: '', descricao: '' };
    }
    this.exibirModal.set(true);
  }

  fecharModal(): void {
    this.exibirModal.set(false);
  }

  salvar(): void {
    if (!this.novaCategoria.nome) return;

    const catEditando = this.categoriaEmEdicao();

    if (catEditando) {
      this.categoriaService.atualizar(catEditando.id, this.novaCategoria as Categoria).subscribe({
        next: () => {
          this.carregarCategorias();
          this.fecharModal();
          this.toast.sucesso('Categoria atualizada com sucesso!'); // 4. TOAST DE SUCESSO GLOBAL
        },
        error: (err) => {
          console.error('Erro ao atualizar categoria:', err);
          this.toast.erro('Falha ao atualizar categoria.');
        }
      });
    } else {
      this.categoriaService.criar(this.novaCategoria).subscribe({
        next: () => {
          this.carregarCategorias();
          this.fecharModal();
          this.toast.sucesso('Categoria cadastrada com sucesso!'); // 5. TOAST DE SUCESSO GLOBAL
        },
        error: (err) => {
          console.error('Erro ao criar categoria:', err);
          this.toast.erro('Falha ao cadastrar categoria.');
        }
      });
    }
  }

  confirmarExclusao(categoria: Categoria): void {
    this.categoriaParaExcluir.set(categoria);
    this.exibirModalExclusao.set(true);
  }

  executarExclusao(): void {
    const categoria = this.categoriaParaExcluir();
    if (!categoria) return;

    this.categoriaService.excluir(categoria.id).subscribe({
      next: () => {
        this.carregarCategorias();
        this.fecharModalExclusao();
        this.toast.sucesso('Categoria excluída com sucesso!'); // 6. TOAST DE SUCESSO GLOBAL
      },
      error: (err) => {
        console.error('Erro ao excluir categoria:', err);
        this.toast.erro('Falha ao excluir categoria. Existem produtos associados a esta categoria.');
      }
    });
  }

  fecharModalExclusao(): void {
    this.exibirModalExclusao.set(false);
    this.categoriaParaExcluir.set(null);
  }
}

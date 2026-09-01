import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CategoriaService } from '../../../core/services/categoria.service';
import { Categoria } from '../../../core/model/produto.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-categorias',
  imports: [CommonModule, FormsModule],
  templateUrl: './categorias.html',
  styleUrl: './categorias.css',
})
export class Categorias implements OnInit {
  private readonly categoriaService = inject(CategoriaService);
  private readonly listaCategoriaRaw = signal<Categoria[]>([]);

  termoBusca = signal<string>('');
  categoriasFiltradas = computed(() => {
    const termo = this.termoBusca().toLowerCase().trim();
    const lista = this.listaCategoriaRaw();

    if (!termo) return lista;

    return lista.filter(cat =>
      cat.nome.toLowerCase().includes(termo) ||
      (cat.descricao && cat.descricao.toLowerCase().includes(termo))
    )
  })
  categorias = signal<Categoria[]>([]);
  exibirModal = signal<boolean>(false);
  exibirModalExclusao = signal<boolean>(false);
  categoriaParaExcluir = signal<Categoria | null>(null);
  categoriaEmEdicao = signal<Categoria | null>(null);

  // Signals do Toast utilizados no seu HTML
  toastMessage = signal<string>('');
  toastType = signal<'success' | 'error'>('success');

  ngOnInit(): void {
    this.carregarCategorias();
  }

  carregarCategorias(): void {
    this.categoriaService.listarTodas().subscribe({
      next: (data) => {
        // Altere aqui para salvar no sinal RAW (bruto)
        this.listaCategoriaRaw.set(Array.isArray(data) ? data : []);
      },
      error: (err) => {
        console.error('Erro ao carregar categorias:', err);
        this.listaCategoriaRaw.set([]);
        this.mostrarToast('Erro ao carregar a lista de categorias.', 'error');
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

  novaCategoria: Partial<Categoria> = { nome: '', descricao: '' };

  salvar(): void {
    if (!this.novaCategoria.nome) return;

    const catEditando = this.categoriaEmEdicao();

    if (catEditando) {
      // Fluxo de Atualização (PUT)
      this.categoriaService.atualizar(catEditando.id, this.novaCategoria as Categoria).subscribe({
        next: () => {
          this.carregarCategorias();
          this.fecharModal();
          this.mostrarToast('Categoria atualizada com sucesso!', 'success');
        },
        error: (err) => {
          console.error('Erro ao atualizar categoria:', err);
          this.mostrarToast('Falha ao atualizar categoria.', 'error');
        }
      });
    } else {
      // Fluxo de Criação (POST)
      this.categoriaService.criar(this.novaCategoria).subscribe({
        next: () => {
          this.carregarCategorias();
          this.fecharModal();
          this.mostrarToast('Categoria cadastrada com sucesso!', 'success');
        },
        error: (err) => {
          console.error('Erro ao criar categoria:', err);
          this.mostrarToast('Falha ao cadastrar categoria.', 'error');
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
        this.mostrarToast('Categoria excluída com sucesso!', 'success');
      },
      error: (err) => {
        console.error('Erro ao excluir categoria:', err);
        this.mostrarToast('Falha ao excluir categoria.', 'error');
      }
    });
  }

  fecharModalExclusao(): void {
    this.exibirModalExclusao.set(false);
    this.categoriaParaExcluir.set(null);
  }

  // Método auxiliar para gerenciar a exibição do Toast temporizado
  private mostrarToast(mensagem: string, tipo: 'success' | 'error'): void {
    this.toastMessage.set(mensagem);
    this.toastType.set(tipo);

    // Esconde o Toast automaticamente após 3 segundos (3000ms)
    setTimeout(() => {
      this.toastMessage.set('');
    }, 3000);
  }
}

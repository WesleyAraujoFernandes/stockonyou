import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { VendaService } from '../../core/services/venda.service';
import { ProdutoService } from '../../core/services/produto.service';
import { ToastService } from '../../core/services/toast.service';
import { LucideBarcode, LucideDollarSign, LucidePlus, LucideSearch, LucideShoppingCart, LucideTrash2, LucideUser } from '@lucide/angular';
import { Produto } from '../../core/model/produto.model';
import { ItemVendaRequest } from '../../core/model/venda.model';

interface ItemCarrinho {
  produto: Produto;
  quantidade: number;
  precoUnitario: number;
  subTotal: number;
}

@Component({
  selector: 'app-nova-venda',
  imports: [CommonModule, FormsModule],
  templateUrl: './nova-venda.html',
  styleUrl: './nova-venda.css',
})
export class NovaVenda implements OnInit {
  private readonly vendaService = inject(VendaService);
  private readonly produtoService = inject(ProdutoService);
  private readonly toast = inject(ToastService);

  readonly IconCart = LucideShoppingCart;
  readonly IconPlus = LucidePlus;
  readonly IconTrash = LucideTrash2;
  readonly IconUser = LucideUser;
  readonly IconSearch = LucideSearch;
  readonly IconBarcode = LucideBarcode;
  readonly IconMoney = LucideDollarSign;

  clienteNome = signal<string>('');
  carrinho = signal<ItemCarrinho[]>([]);

  termoBuscaProduto = '';
  produtosEncontrados = signal<Produto[]>([]);
  produtoSelecionado: Produto | null = null;
  quantidadeInserir = 1;

  valorTotalCarrinho = computed(() => {
    return this.carrinho().reduce((acc, item) => acc + item.subTotal, 0);
  })

  ngOnInit(): void {

  }

  buscarProdutosPorTermo(): void {
    if (!this.termoBuscaProduto.trim()) {
      this.produtosEncontrados.set([]);
      return;
    }
    this.produtoService.listarComFiltros(this.termoBuscaProduto, undefined, undefined, undefined, 0, 5)
      .subscribe({
        next: (response) => this.produtosEncontrados.set(response.content || []),
        error: (err) => console.error('Erro ao buscar produtos para o PDV:', err)
      })
  }

  selecionarProduto(produto: Produto): void {
    this.produtoSelecionado = produto;
    this.termoBuscaProduto = produto.nome;
    this.produtosEncontrados.set([]);
  }

  adicionarNoCarrinho(): void {
    if (!this.produtoSelecionado) {
      this.toast.erro('Selecione um produto antes de adicionar.');
      return;
    }
    if (this.quantidadeInserir <= 0) {
      this.toast.erro('A quantidade deve ser maior que zero.');
      return;
    }
    if (this.quantidadeInserir > this.produtoSelecionado.quantidade) {
      this.toast.erro(`Estoque insuficiente. Quantidade disponível: ${this.produtoSelecionado.quantidade}`);
      return;
    }
    const itensAtuais = [...this.carrinho()];
    const itemExistente = itensAtuais.find(item => item.produto.id === this.produtoSelecionado!.id);
    if (itemExistente) {
      const novaQtd = itemExistente.quantidade + this.quantidadeInserir;
      if (novaQtd > this.produtoSelecionado.quantidade) {
        this.toast.erro(`Estoque insuficiente somando o carrinho. Limite: ${this.produtoSelecionado.quantidade}`);
        return;
      }
      itemExistente.quantidade = novaQtd;
      itemExistente.subTotal = itemExistente.quantidade * itemExistente.precoUnitario;
      this.carrinho.set(itensAtuais);
    } else {
      this.carrinho.update(lista => [...lista, {
        produto: this.produtoSelecionado!,
        quantidade: this.quantidadeInserir,
        precoUnitario: this.produtoSelecionado!.preco,
        subTotal: this.quantidadeInserir * this.produtoSelecionado!.preco
      }])
    }
    this.toast.sucesso(`${this.produtoSelecionado.nome} adicionado.`);
    this.produtoSelecionado = null;
    this.termoBuscaProduto = '';
    this.quantidadeInserir = 1;
  }
  removerDoCarrinho(index: number): void {
    this.carrinho.update(lista => lista.filter((_, i) => i !== index));
    this.toast.sucesso('Item removido do carrinho.');
  }

  finalizarVenda(): void {
    if (this.carrinho().length === 0) {
      this.toast.erro('O carrinho está vazio.');
      return;
    }
    const itensRequest: ItemVendaRequest[] = this.carrinho().map(item => ({
      produtoId: item.produto.id,
      quantidade: item.quantidade,
      precoUnitario: item.precoUnitario
    }))
    const payload = {
      clienteNome: this.clienteNome().trim() || undefined,
      itens: itensRequest
    }
    this.vendaService.realizarVenda(payload).subscribe({
      next: () => {
        this.toast.sucesso('Venda finalizada com sucesso! Estoque atualizado.');
        this.carrinho.set([]);
        this.clienteNome.set('');
      },
      error: (err) => {
        console.error('Erro ao finalizar venda:', err);
        this.toast.erro('Falha ao concluir a venda. Verifique as regras de negócio.')
      }
    })
  }
}

import { Component, inject, OnInit, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VendaService } from '../../core/services/venda.service';
import { ProdutoService } from '../../core/services/produto.service';
import { ToastService } from '../../core/services/toast.service';
import { Produto } from '../../core/model/produto.model';
import { ItemVendaRequest } from '../../core/model/venda.model';
import { Cliente, ClienteService } from '../../core/services/cliente.service';

import {
  LucideDynamicIcon,
  LucideShoppingCart,
  LucidePlus,
  LucideTrash2,
  LucideUser,
  LucideSearch,
  LucideBarcode,
  LucideDollarSign,
  LucideUserPlus
} from '@lucide/angular';

interface ItemCarrinho {
  produto: Produto;
  quantidade: number;
  precoUnitario: number;
  subTotal: number;
}

interface ComandaAtiva {
  cliente: Cliente;
  carrinho: ItemCarrinho[];
}

@Component({
  selector: 'app-nova-venda',
  imports: [CommonModule, FormsModule, LucideDynamicIcon],
  templateUrl: './nova-venda.html',
  styleUrl: './nova-venda.css',
})
export class NovaVenda implements OnInit {
  private readonly clienteService = inject(ClienteService);
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
  readonly IconUserPlus = LucideUserPlus;

  // Lista de comandas abertas na memória do sistema
  comandasAtivas = signal<ComandaAtiva[]>([]);
  clienteSelecionado = signal<Cliente>({ id: 1, nome: 'Cliente Padrão' });
  carrinho = signal<ItemCarrinho[]>([]);

  termoBuscaCliente = '';
  clientesEncontrados = signal<Cliente[]>([]);

  termoBuscaProduto = '';
  produtosEncontrados = signal<Produto[]>([]);
  produtoSelecionado: Produto | null = null;
  quantidadeInserir = 1;

  valorTotalCarrinho = computed(() => {
    return this.carrinho().reduce((acc, item) => acc + item.subTotal, 0);
  });

  constructor() {
    effect(() => {
      const estadoAtual = {
        comandas: this.comandasAtivas(),
        clienteAtivoId: this.clienteSelecionado().id
      };
      localStorage.setItem('pdv_comandas_v1', JSON.stringify(estadoAtual));
    })
  }

  ngOnInit(): void {
    const dadosSalvos = localStorage.getItem('pdv_comandas_v1')
    if (dadosSalvos) {
      try {
        const estadoBackup = JSON.parse(dadosSalvos);
        if (estadoBackup.comandas && estadoBackup.comandas.length > 0) {
          this.comandasAtivas.set(estadoBackup.comandas);
          const clienteSalvo = estadoBackup.comandas.find((c: ComandaAtiva) => c.cliente.id === estadoBackup.clienteAtivoId);
          if (clienteSalvo) {
            this.clienteSelecionado.set(clienteSalvo.cliente);
            this.carrinho.set(clienteSalvo.carrinho);
          }
          return;
        }
      } catch (e) {
        console.error('Erro ao ler rascunhos de comandas:',e)
      }
    }
    this.comandasAtivas.set([{
      cliente: { id: 1, nome: 'Cliente Padrão' },
      carrinho: []
    }])
  }

  abrirNovaComanda(cliente: Cliente): void {
    const jaExiste = this.comandasAtivas().some(c => c.cliente.id === cliente.id);
    if (jaExiste && cliente.id !== 1) {
      this.toast.erro(`A comanda para ${cliente.nome} já está aberta.`);
      this.alternarParaComanda(cliente);
      return;
    }

    const nova: ComandaAtiva = { cliente, carrinho: [] };
    this.comandasAtivas.update(lista => [...lista, nova]);
    this.alternarParaComanda(cliente);
    this.toast.sucesso(`Comanda de ${cliente.nome} aberta.`);
    this.termoBuscaCliente = '';
    this.clientesEncontrados.set([]);
  }

  alternarParaComanda(cliente: Cliente): void {
    // 1. Salva o estado atual do carrinho na comanda do cliente que estava ativo
    this.comandasAtivas.update(lista => lista.map(c => {
      if (c.cliente.id === this.clienteSelecionado().id) {
        return { ...c, carrinho: this.carrinho() };
      }
      return c;
    }));

    // 2. Carrega a nova comanda selecionada
    const comandaAlvo = this.comandasAtivas().find(c => c.cliente.id === cliente.id);
    if (comandaAlvo) {
      this.clienteSelecionado.set(comandaAlvo.cliente);
      this.carrinho.set(comandaAlvo.carrinho);
    }
  }

  buscarClientesPorTermo(): void {
    if (!this.termoBuscaCliente.trim()) {
      this.clientesEncontrados.set([]);
      return;
    }
    this.clienteService.buscarPorTermo(this.termoBuscaCliente).subscribe({
      next: (dados) => this.clientesEncontrados.set(dados || []),
      error: (err) => console.error('Erro ao buscar clientes:', err)
    });
  }

  verificarOuCadastrarCliente(): void {
    const termo = this.termoBuscaCliente.trim();
    if (!termo || termo === this.clienteSelecionado().nome) {
      return;
    }

    const desejaCadastrar = confirm(`O cliente "${termo}" não foi encontrado. Deseja abrir uma nova comanda para ele?`);
    if (desejaCadastrar) {
      this.clienteService.cadastrarRapido(termo).subscribe({
        next: (novoCliente) => {
          this.abrirNovaComanda(novoCliente);
        },
        error: (err) => this.toast.erro('Falha ao cadastrar o cliente.')
      });
    }
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
      });
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
      this.toast.erro(`Estoque insuficiente. Disponível: ${this.produtoSelecionado.quantidade}`);
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
      }]);
    }

    this.comandasAtivas.update(lista => lista.map(c => {
      if (c.cliente.id === this.clienteSelecionado().id) {
        return {...c, carrinho: this.carrinho() }
      }
      return c;
    }))

    this.toast.sucesso(`${this.produtoSelecionado.nome} adicionado.`);
    this.produtoSelecionado = null;
    this.termoBuscaProduto = '';
    this.quantidadeInserir = 1;
  }

  removerDoCarrinho(index: number): void {
    this.carrinho.update(lista => lista.filter((_, i) => i !== index));
    this.comandasAtivas.update(lista => lista.map(c => {
      if (c.cliente.id === this.clienteSelecionado().id) {
        return { ...c, carrinho: this.carrinho() }
      }
      return c;
    }))
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
    }));

    const idClienteFinal = (this.clienteSelecionado() && this.clienteSelecionado().id)
      ? Number(this.clienteSelecionado().id)
      : 1;

    const payload = {
      clienteId: idClienteFinal,
      itens: itensRequest
    };

    this.vendaService.realizarVenda(payload).subscribe({
      next: () => {
        this.toast.sucesso('Venda finalizada com sucesso! Estoque atualizado.');

        const idFechado = this.clienteSelecionado().id;
        this.carrinho.set([]);

        // Remove a comanda finalizada do array reativo
        this.comandasAtivas.update(lista => lista.filter(c => c.cliente.id !== idFechado));

        // Se fechou todas as comandas, garante a abertura da comanda Balcão novamente
        if (this.comandasAtivas().length === 0) {
          this.comandasAtivas.set([{ cliente: { id: 1, nome: 'Cliente Padrão' }, carrinho: [] }]);
        }

        // Seleciona a primeira comanda restante da lista
        this.clienteSelecionado.set(this.comandasAtivas()[0].cliente);
        this.carrinho.set(this.comandasAtivas()[0].carrinho);
        this.termoBuscaCliente = '';
        this.clientesEncontrados.set([]);
      },
      error: (err) => {
        console.error('Erro ao finalizar venda:', err);
        this.toast.erro('Falha ao concluir a venda. Verifique as regras de negócio.');
      }
    });
  }

  ajustarQuantidadeItem(index: number): void {
    const itensAtuais = [...this.carrinho()];
    const item = itensAtuais[index];
    if (!item) return;

    const novaQuantidade = item.quantidade + -1;

    if (novaQuantidade <= 0) {
      this.removerDoCarrinho(index);
      return;
    }

    item.quantidade = novaQuantidade;
    item.subTotal = item.quantidade * item.precoUnitario;
    this.carrinho.set(itensAtuais);

    this.comandasAtivas.update(lista => lista.map(c => {
      if (c.cliente.id === this.clienteSelecionado().id) {
        return { ...c, carrinho: this.carrinho() }
      }
      return c;
    }))
  }
}

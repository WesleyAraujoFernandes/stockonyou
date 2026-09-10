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
import { KeycloakService } from '../../core/auth/keycloak.service';

interface ItemCarrinho {
  produto?: Produto;
  quantidade: number;
  precoUnitario: number;
  subTotal: number;
}

interface ComandaAtiva {
  vendaId?: number;
  cliente: Cliente;
  usuario: string;
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
  private readonly keycloakService = inject(KeycloakService);
  private readonly usuarioLogado = this.keycloakService.getUserDisplayName()


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
  vendaIdAtual: number | undefined = undefined;

  termoBuscaCliente = '';
  clientesEncontrados = signal<Cliente[]>([]);

  termoBuscaProduto = '';
  produtosEncontrados = signal<Produto[]>([]);
  produtoSelecionado: Produto | null = null;
  quantidadeInserir = 1;

  exibirModalFechamento = signal<boolean>(false);

  idComandaAberta = signal<number | null>(null);

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
    this.recuperarTodasComandasDoBanco();
  }

  recuperarTodasComandasDoBanco(): void {
    this.vendaService.listarComandasAbertas().subscribe({
      next: (comandasBanco : any[]) => {
        if (comandasBanco && comandasBanco.length > 0) {
          const mapeadas: ComandaAtiva[] = comandasBanco.map(venda => {
            const idDoClienteReal = venda.cliente?.id || venda.clienteId || 999;
            const clienteValido: Cliente = {
              id: Number(idDoClienteReal),
              nome: venda.clienteNome || (venda as any).cliente?.nome || 'Cliente sem Nome'
            };

            return {
              vendaId: venda.id,
              cliente: clienteValido, // Sempre garante um objeto Cliente preenchido
              usuario: this.usuarioLogado,
              carrinho: (venda.itens || []).map((item: any) => ({
                produto: item.produto || { id: item.produtoId, nome: item.produtoNome, preco: item.precoUnitario },
                quantidade: item.quantidade,
                precoUnitario: item.precoUnitario,
                subTotal: item.subtotal
              }))
            };
          });

          this.comandasAtivas.set(mapeadas);

          const primeira = mapeadas[0];
          this.clienteSelecionado.set(primeira.cliente);
          this.carrinho.set(primeira.carrinho)
          this.vendaIdAtual = primeira.vendaId;

        } else {
          this.comandasAtivas.set([{
            cliente: { id: 1, nome: 'Cliente Padrão' },
            usuario: this.usuarioLogado,
            carrinho: [] }]);
          this.clienteSelecionado.set({ id: 1, nome: 'Cliente Padrão' });
          this.carrinho.set([]);
          this.vendaIdAtual = undefined;
        }
      },
      error: (err) => {
        console.error('Erro ao listar comandas do banco:', err);
        this.toast.erro('Falha ao carregar as comandas do servidor.');
      }
    });
  }

  carregarComandaDoCliente(clienteId: number): void {
    if (clienteId === 1) {
      this.carrinho.set([]);
      this.idComandaAberta.set(null);
      return;
    }
    this.vendaService.buscarComandaAbertaPorCliente(clienteId).subscribe({
      next: (comandaAtiva: any) => {
        if (comandaAtiva && comandaAtiva.itens && comandaAtiva.itens.length > 0) {
          this.idComandaAberta.set(comandaAtiva.id);
          const itensMapeados = comandaAtiva.itens.map((item: any) => ({
            produto: {
              id: item.produtoId,
              nome: item.produtoNome,
              preco: item.precoUnitario,
              codigoBarras: '',
              quantidade: 0,
              categoria: { id: 0, nome: '' }
            } as Produto,
            quantidade: item.quantidade,
            precoUnitario: item.precoUnitario,
            subTotal: item.subtotal
          }));
          this.carrinho.set(itensMapeados);
          this.toast.sucesso(`Comanda aberta recuperada para ${this.clienteSelecionado().nome}`)
        } else {
          this.carrinho.set([]);
          this.idComandaAberta.set(null);
        }
      },
      error: (err) => {
        console.error('Erro ao buscar comanda do cliente:', err);
        this.carrinho.set([]);
        this.idComandaAberta.set(null);
      }
    })
  }

  fecharComandaAtiva(): void {
    const idComanda = this.idComandaAberta();
    if (!idComanda) {
      this.toast.erro('Não há nenhuma comanda ativa aberta no servidor para ser finalizada.')
      return
    }
    const confirmar = confirm(`Deseja realmente encerrar e fechar a conta de "${this.clienteSelecionado().nome}" no valor de ${this.valorTotalCarrinho().toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}`)
    if (confirmar) {
      this.vendaService.finalizarComanda(idComanda, 'PAGO').subscribe({
        next: () => {
          this.toast.sucesso('Conta fechada com sucesso! Comanda Finalizada.');
          this.carrinho.set([]);
          this.idComandaAberta.set(null);
          this.clienteSelecionado.set({ id: 1, nome: 'Cliente Padrão' });
          this.termoBuscaCliente = ''
          this.clientesEncontrados.set([]);
        },
        error: (err) => {
          console.error('Erro ao finalizar comanda:', err);
          this.toast.erro('Falha ao encerrar a comanda no servidor.');
        }
      })
    }
  }

  abrirNovaComanda(cliente: Cliente): void {
    const jaExiste = this.comandasAtivas().some(c => c.cliente.id === cliente.id);
    if (jaExiste && cliente.id !== 1) {
      const desejaCarregar = confirm(`A comanda para ${cliente.nome} já está aberta. Deseja carregar o atendimento existente dela?`);
      if (desejaCarregar) {
        this.alternarParaComanda(cliente);
        this.termoBuscaCliente = '';
      } else {
        alert(`Para abrir um novo atendimento separado, adicione um sobrenome ou identificador ao nome do cliente (Ex: ${cliente.nome} Silva, ou ${cliente.nome} Mesa 2).`)
        this.termoBuscaCliente = `${cliente.nome} `;
      }
      this.clientesEncontrados.set([]);
      return;
    }

    const nova: ComandaAtiva = { cliente, usuario: this.usuarioLogado , carrinho: [] };
    this.comandasAtivas.update(lista => [...lista, nova]);
    this.clienteSelecionado.set(cliente);
    this.carrinho.set([]);
    this.vendaIdAtual = undefined;

    this.toast.sucesso(`Comanda de ${cliente.nome} aberta.`)
    this.termoBuscaCliente = '';
    this.clientesEncontrados.set([]);
  }

  alternarParaComanda(cliente: Cliente): void {
    // 1. Salva o estado atual do carrinho na comanda do cliente que estava ativo
    this.comandasAtivas.update(lista => lista.map(c => {
      if (c.cliente.id === this.clienteSelecionado().id) {
        return { ...c, carrinho: this.carrinho(), vendaId: this.vendaIdAtual };
      }
      return c;
    }));

    // 2. Carrega a comanda destino
    const comandaAlvo = this.comandasAtivas().find(c => c.cliente.id === cliente.id);
    if (comandaAlvo) {
      this.clienteSelecionado.set(comandaAlvo.cliente);
      this.carrinho.set(comandaAlvo.carrinho);
      this.vendaIdAtual = comandaAlvo.vendaId;
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
    if (produto.quantidade <= 0) {
      this.toast.erro(`O produto "${produto.nome}" está esgotado no estoque!`);
      this.termoBuscaProduto = ''
      this.produtosEncontrados.set([]);
      return;
    }
    this.produtoSelecionado = produto;
    this.termoBuscaProduto = produto.nome;
    this.produtosEncontrados.set([]);
  }

  adicionarNoCarrinho(): void {
    if (!this.produtoSelecionado) return;
    console.log('adicionarNoCarrinho -> usuarioLogado:',this.usuarioLogado);
    // CASO 1: SE FOR CLIENTE PADRÃO (ID 1) -> Gerencia apenas em memória local
    if (this.clienteSelecionado().id === 1) {
      const itensAtuais = [...this.carrinho()];
      const itemExistente = itensAtuais
      .find(item => item.produto!.id === this.produtoSelecionado!.id);
      if (itemExistente) {
        itemExistente.quantidade += this.quantidadeInserir;
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

      this.sincronizarListaLateral();
      this.toast.sucesso(`${this.produtoSelecionado.nome} adicionado ao balcão.`);
      this.produtoSelecionado = null;
      this.termoBuscaProduto = '';
      this.quantidadeInserir = 1;
      return; // Finaliza o método aqui, sem chamar o HTTP PUT
    }

    // CASO 2: CLIENTES REAIS (Etevaldo, Eliana...) -> Envia para a comanda aberta no banco
    const itemRequest: ItemVendaRequest = {
      produtoId: this.produtoSelecionado.id,
      quantidade: this.quantidadeInserir,
      precoUnitario: this.produtoSelecionado.preco
    };

    this.vendaService.atualizarComanda(this.clienteSelecionado().id, itemRequest).subscribe({
      next: (vendaAtualizada: any) => {
        this.vendaIdAtual = vendaAtualizada.id;

        const novoCarrinho = vendaAtualizada.itens.map((item: any) => ({
          produto: {
            id: item.produtoId,
            nome: item.produtoNome || 'Produto',
            preco: item.precoUnitario,
            codigoBarras: '',
            quantidade: 9999,
            categoria: { id: 0, nome: '' }
          } as Produto,
          quantidade: item.quantidade,
          precoUnitario: item.precoUnitario,
          subTotal: item.subtotal
        }));

        this.carrinho.set(novoCarrinho);
        this.sincronizarListaLateral();
        this.toast.sucesso('Item inserido e salvo na comanda do banco.');

        this.produtoSelecionado = null;
        this.termoBuscaProduto = '';
        this.quantidadeInserir = 1;
      },
      error: () => this.toast.erro('Erro ao salvar item no banco.')
    });
  }
  removerDoCarrinho(index: number): void {
    const item = this.carrinho()[index];
    // CORREÇÃO: Garante que o item e a propriedade produto existam antes de prosseguir
    if (!item || !item.produto) return;

    const itemRequest: ItemVendaRequest = {
      produtoId: item.produto.id,
      quantidade: 0,
      precoUnitario: item.precoUnitario
    };

    this.vendaService.atualizarComanda(this.clienteSelecionado().id, itemRequest).subscribe({
      next: () => {
        this.carrinho.update(lista => lista.filter((_, i) => i !== index));
        this.sincronizarListaLateral();
        this.toast.sucesso('Item removido do banco.');
      },
      error: (err) => {
        console.error('Erro ao remover item:', err);
        this.toast.erro('Falha ao remover o item do banco.');
      }
    });
  }

  ajustarQuantidadeItem(index: number): void {
    const item = this.carrinho()[index];
    if (!item || !item.produto) return;

    // CORREÇÃO: Declarada aqui em cima para funcionar em todo o escopo do método
    const novaQtd = item.quantidade - 1;

    // CASO 1: SE FOR CLIENTE PADRÃO (ID 1) -> Gerencia apenas em memória local
    if (this.clienteSelecionado().id === 1) {
      if (novaQtd <= 0) {
        this.carrinho.update(lista => lista.filter((_, i) => i !== index));
      } else {
        const itensAtuais = [...this.carrinho()];
        itensAtuais[index].quantidade = novaQtd;
        itensAtuais[index].subTotal = novaQtd * item.precoUnitario;
        this.carrinho.set(itensAtuais);
      }
      this.sincronizarListaLateral();
      this.toast.sucesso('Quantidade ajustada no balcão.');
      return; // Interrompe aqui para não fazer o PUT no banco
    }

    // CASO 2: CLIENTES REAIS -> Envia para a comanda aberta no banco
    const itemRequest: ItemVendaRequest = {
      produtoId: item.produto.id,
      quantidade: novaQtd,
      precoUnitario: item.precoUnitario
    };

    this.vendaService.atualizarComanda(this.clienteSelecionado().id, itemRequest).subscribe({
      next: (vendaAtualizada: any) => {
        if (novaQtd <= 0) {
          this.carrinho.update(lista => lista.filter((_, i) => i !== index));
        } else {
          // Reconstrói o objeto Produto a partir do DTO plano do Java
          const novoCarrinho = vendaAtualizada.itens.map((it: any) => ({
            produto: {
              id: it.produtoId,
              nome: it.produtoNome || 'Produto',
              preco: it.precoUnitario,
              codigoBarras: '',
              quantidade: 9999,
              categoria: { id: 0, nome: '' }
            } as Produto,
            quantidade: it.quantidade,
            precoUnitario: it.precoUnitario,
            subTotal: it.subtotal
          }));
          this.carrinho.set(novoCarrinho);
        }
        this.sincronizarListaLateral();
        this.toast.sucesso('Quantidade ajustada no banco.');
      },
      error: (err) => {
        console.error('Erro ao ajustar quantidade:', err);
        this.toast.erro('Falha ao ajustar a quantidade no banco.');
      }
    });
  }


  private sincronizarListaLateral(): void {
    this.comandasAtivas.update(lista => lista.map(c => {
      if (c.cliente.id === this.clienteSelecionado().id) {
        return { ...c, carrinho: this.carrinho(), vendaId: this.vendaIdAtual }
      }
      return c;
    }))
  }

  abrirModalFinalizacao(): void {
    if (this.carrinho().length === 0) {
      this.toast.erro('O carrinho está vazio.');
      return;
    }
    this.exibirModalFechamento.set(true);
  }

  confirmarFechamento(tipo: 'PAGO' | 'PENDENTE'): void {
    const idCliente = Number(this.clienteSelecionado().id);

    // CASO 1: SE FOR CLIENTE PADRÃO (ID 1) -> Dispara a venda direta de balcão (POST /api/vendas)
    if (idCliente === 1 || this.clienteSelecionado().nome.toLowerCase() === 'cliente padrão') {
      const itensRequest = this.carrinho()
        .filter(item => item.produto !== undefined && item.produto !== null)
        .map(item => ({
          produtoId: item.produto!.id,
          quantidade: item.quantidade,
          precoUnitario: item.precoUnitario
        }));

      const payload = {
        clienteId: 1,
        itens: itensRequest
      };

      this.vendaService.realizarVenda(payload).subscribe({
        next: () => {
          this.toast.sucesso('Venda de balcão finalizada com sucesso!');
          this.limparEstadoPdvAposFechamento(1);
        },
        error: (err) => {
          console.error('Erro ao processar venda de balcão:', err);
          this.toast.erro('Erro ao processar venda de balcão.');
        }
      });
      return; // Interrompe para não seguir para o fluxo de comanda
    }

    // CASO 2: CLIENTES REAIS (Wesley, Eliana...) -> Finaliza comanda no banco (PUT /api/vendas/{id}/finalizar)
    // Se a vendaIdAtual sumiu da memória, tentamos buscar a referência guardada na lista lateral
    if (!this.vendaIdAtual) {
      const comandaMemoria = this.comandasAtivas().find(c => c.cliente.id === idCliente);
      this.vendaIdAtual = comandaMemoria?.vendaId;
    }

    if (!this.vendaIdAtual) {
      this.toast.erro('Não foi possível localizar o ID da comanda para este cliente no servidor.');
      return;
    }

    this.vendaService.finalizarComanda(this.vendaIdAtual, tipo).subscribe({
      next: () => {
        const mensagem = tipo === 'PAGO' ? 'Venda quitada com sucesso!' : 'Conta pendurada (Fiado) registrada!';
        this.toast.sucesso(mensagem);
        this.limparEstadoPdvAposFechamento(idCliente);
      },
      error: (err) => {
        console.error('Erro ao finalizar comanda do cliente:', err);
        this.toast.erro('Falha ao encerrar comanda no servidor.');
      }
    });
  }

  // Método auxiliar para isolar a limpeza das listas após salvar
  limparEstadoPdvAposFechamento(idCliente: number): void {
    this.carrinho.set([]);
    this.exibirModalFechamento.set(true); // Oculta o modal
    this.exibirModalFechamento.set(false);
    const usuario = this.usuarioLogado;

    this.comandasAtivas.update(lista => lista.filter(c => c.cliente.id !== idCliente));

    if (this.comandasAtivas().length === 0) {
      this.comandasAtivas.set([{ cliente: { id: 1, nome: 'Cliente Padrão' }, usuario, carrinho: [] }]);
    }

    this.clienteSelecionado.set(this.comandasAtivas()[0].cliente);
    this.carrinho.set(this.comandasAtivas()[0].carrinho);
    this.vendaIdAtual = (this.comandasAtivas()[0] as any).vendaId;
    this.termoBuscaCliente = '';
    this.clientesEncontrados.set([]);
  }
}

import { Component, inject, OnInit, signal, computed, effect } from '@angular/core';
import { switchMap } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VendaService } from '../../core/services/venda.service';
import { ProdutoService } from '../../core/services/produto.service';
import { ToastService } from '../../core/services/toast.service';
import { Produto } from '../../core/model/produto.model';
import { ItemVendaRequest, VendaRequest } from '../../core/model/venda.model';
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
      next: (comandasBanco: any[]) => {
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
            carrinho: []
          }]);
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
      this.vendaService.registrarPagamento(idComanda).subscribe({
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

    const nova: ComandaAtiva = { cliente, usuario: this.usuarioLogado, carrinho: [] };
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
    if (!termo) {
      return;
    }

    const clienteSelecionado = this.clienteSelecionado()

    if (
      clienteSelecionado &&
      clienteSelecionado.nome.trim().toLowerCase() === termo.toLowerCase()
    ) {
      return;
    }

    const clienteExistente = this.clientesEncontrados().find(
      cliente =>
        cliente.nome.trim().toLowerCase() === termo.toLowerCase()
    );

    if (clienteExistente) {
      this.abrirNovaComanda(clienteExistente);
      return;
    }

    const desejaCadastrar = confirm(`O cliente "${termo}" não foi encontrado. Deseja abrir uma nova comanda para ele?`);
    if (!desejaCadastrar) {
      return;
    }
    this.clienteService.cadastrarRapido(termo).subscribe({
      next: (novoCliente) => {
        this.toast.sucesso('Cliente cadastrado com sucesso!')
        this.abrirNovaComanda(novoCliente);
      },
      error: (err) => {
        console.error('Erro a cadastrar cliente:', err)
        this.toast.erro('Falha ao cadastrar o cliente.')
      }
    });
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
      quantidade: this.quantidadeInserir
    };

    if (this.vendaIdAtual === undefined) {
      const payload: VendaRequest = {
        clienteId: this.clienteSelecionado().id,
        itens: [itemRequest]
      }
      this.vendaService.criarComanda(payload).subscribe({
        next: (novaComanda) => {
          this.vendaIdAtual = novaComanda.id;
          const novoCarrinho: ItemCarrinho[] = novaComanda.itens.map(item => ({
            produto: {
              id: item.produtoId,
              nome: item.produtoNome,
              preco: item.precoUnitario,
              codigoBarras: '',
              quantidade: 9999,
              categoria: { id: 0, nome: '' }
            } as Produto,
            quantidade: item.quantidade,
            precoUnitario: item.precoUnitario,
            subTotal: item.subtotal
          }))
          this.carrinho.set(novoCarrinho);
          this.comandasAtivas.update(lista =>
            lista.map(comanda =>
              comanda.cliente.id === this.clienteSelecionado().id
                ? {
                  ...comanda,
                  vendaId: novaComanda.id,
                  carrinho: novoCarrinho
                }
                : comanda
            )
          )
          this.sincronizarListaLateral();
          this.toast.sucesso(
            'Comanda criada e produto adicionado com sucesso.'
          )
          this.produtoSelecionado = null;
          this.termoBuscaProduto = '';
          this.quantidadeInserir = 1;
        },
        error: (err) => {
          console.error('Erro ao criar comanda: ', err);
          this.toast.erro('Erro ao criar a comanda no servidor.')
        }
      })
      return;
    }

    this.vendaService.adicionarItemComanda(this.vendaIdAtual, itemRequest).subscribe({
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

    if (!item || !item.produto) return;

    if (this.clienteSelecionado().id === 1) {
      this.carrinho.update(lista => lista.filter((_, i) => i !== index))
      this.sincronizarListaLateral();
      this.toast.sucesso('Item removedo do balcão.');
      return;
    }
    if (this.vendaIdAtual === undefined) {
      this.vendaIdAtual = this.idComandaAberta() ?? undefined;
    }
    if (this.vendaIdAtual === undefined) {
      this.toast.erro(
        'Nenuma comanda aberta foi selecionada.'
      )
      return;
    }
    const produtoId = item.produto!.id;
    this.vendaService
      .removerItemComanda(
        this.vendaIdAtual,
        produtoId
      )
      .subscribe({
        next: (vendaAtualizada) => {
          const novoCarrinho: ItemCarrinho[] =
            vendaAtualizada.itens.map(itemAtualizado => ({
              produto: {
                id: itemAtualizado.produtoId,
                nome: itemAtualizado.produtoNome,
                preco: itemAtualizado.precoUnitario,
                codigoBarras: '',
                quantidade: 9999,
                categoria: { id: 0, nome: '' }
              } as Produto,
              quantidade: itemAtualizado.quantidade,
              precoUnitario: itemAtualizado.precoUnitario,
              subTotal: itemAtualizado.subtotal
            }));

          this.carrinho.set(novoCarrinho);

          this.comandasAtivas.update(lista =>
            lista.map(comanda =>
              comanda.cliente.id === this.clienteSelecionado().id
                ? {
                  ...comanda,
                  carrinho: novoCarrinho
                }
                : comanda
            )
          );

          this.sincronizarListaLateral();

          this.toast.sucesso(
            'Item removido da comanda.'
          );
        },
        error: (err) => {
          console.error(
            'Erro ao remover item da comanda:',
            err
          );

          this.toast.erro(
            'Erro ao remover o item da comanda.'
          );
        }
      });
  }

  ajustarQuantidadeItem(index: number): void {
    const item = this.carrinho()[index];
    if (!item || !item.produto) return;

    // CASO 1: SE FOR CLIENTE PADRÃO (ID 1) -> Gerencia apenas em memória local
    if (this.clienteSelecionado().id === 1) {
      const novaQtd = item.quantidade - 1;
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
      return;
    }

    // Comanda real: ainda não existe endpoint para reduzir a quantidade de um item.

    const novaQtd = item.quantidade - 1;

    if (novaQtd <= 0) {
      this.toast.info('Para remover o item da comanda, utilize a opção de remoção.');
      return;
    }

    this.vendaService.atualizarQuantidadeItemComanda(
      this.vendaIdAtual!,
      item.produto.id,
      novaQtd
    )
      .subscribe({
        next: (vendaAtualizada) => {
          const novoCarrinho = vendaAtualizada.itens.map(itemAtualizado => ({
            produto: {
              id: itemAtualizado.produtoId,
              nome: itemAtualizado.produtoNome,
              preco: itemAtualizado.precoUnitario,
              codigoBarras: '',
              quantidade: 9999,
              categoria: { id: 0, nome: '' }
            } as Produto,
            quantidade: itemAtualizado.quantidade,
            precoUnitario: itemAtualizado.precoUnitario,
            subTotal: itemAtualizado.subtotal
          }));
          this.carrinho.set(novoCarrinho);
          this.sincronizarListaLateral();
          this.toast.sucesso(
            'Quantidade do item atualizada na comanda.'
          )
        },
        error: (err) => {
          console.error('Erro ao atualizar a quantidade do item:', err);
          this.toast.erro('Erro ao atualizar a quantidade do item na comanda.')
        }
      })
  }

  aumentarQuantidadeItem(index: number): void {
    const item = this.carrinho()[index];
    if (!item || !item.produto) return;
    const novaQtd = item.quantidade + 1;
    if (this.clienteSelecionado().id === 1) {
      const itensAtuais = [...this.carrinho()];
      itensAtuais[index].quantidade = novaQtd;
      itensAtuais[index].subTotal = novaQtd * item.precoUnitario;
      this.carrinho.set(itensAtuais);
      this.sincronizarListaLateral();
      this.toast.sucesso(
        'Quantidade aumentada no balção.'
      );
      return;
    }

    if (this.vendaIdAtual === undefined) {
      this.toast.erro(
        'Nenhuma comanda aberta foi selecionada.'
      );
      return;
    }

    const produtoId = item.produto!.id;

    this.vendaService.atualizarQuantidadeItemComanda(this.vendaIdAtual, produtoId, novaQtd)
      .subscribe({
        next: (vendaAtualizada) => {
          const novoCarrinho = vendaAtualizada.itens.map(
            itemAtualizado => ({
              produto: {
                id: itemAtualizado.produtoId,
                nome: itemAtualizado.produtoNome,
                preco: itemAtualizado.precoUnitario,
                codigoBarras: '',
                quantidade: 9999,
                categoria: {
                  id: 0,
                  nome: ''
                }
              } as Produto,
              quantidade: itemAtualizado.quantidade,
              precoUnitario: itemAtualizado.precoUnitario,
              subTotal: itemAtualizado.subtotal
            })
          )
          this.carrinho.set(novoCarrinho);
          this.sincronizarListaLateral();
          this.toast.sucesso(
            'Quantidade do item atualizada na comanda.'
          )
        },
        error: (err) => {
          console.error('Erro ao aumentar quantidade do item:', err);
          this.toast.erro('Erro ao atualizar a quantidade do item na comanda.')
        }
      })
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

    // Cliente padrão -> venda direta de balcão
    if (
      idCliente === 1 ||
      this.clienteSelecionado().nome.toLowerCase() === 'cliente padrão'
    ) {
      const itensRequest: ItemVendaRequest[] = this.carrinho()
        .filter(item => item.produto !== undefined && item.produto !== null)
        .map(item => ({
          produtoId: item.produto!.id,
          quantidade: item.quantidade
        }));

      const payload: VendaRequest = {
        clienteNome: 'Cliente Padrão',
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

      return;
    }

    // Cliente real -> comanda
    if (!this.vendaIdAtual) {
      const comandaMemoria = this.comandasAtivas()
        .find(c => c.cliente.id === idCliente);

      this.vendaIdAtual = comandaMemoria?.vendaId;
    }

    if (!this.vendaIdAtual) {
      this.toast.erro(
        'Não foi possível localizar o ID da comanda para este cliente no servidor.'
      );
      return;
    }

    const vendaId = this.vendaIdAtual;

    // Pendente -> pagamento direto
    if (tipo === 'PAGO') {
      this.vendaService.buscarPorId(vendaId).subscribe({
        next: (venda) => {

          if (venda.status === 'PENDENTE') {
            this.vendaService.registrarPagamento(vendaId).subscribe({
              next: () => {
                this.toast.sucesso('Venda quitada com sucesso!');
                this.limparEstadoPdvAposFechamento(idCliente);
              },
              error: (err) => {
                console.error('Erro ao registrar pagamento:', err);
                this.toast.erro(
                  'Falha ao registrar o pagamento no servidor.'
                );
              }
            });

            return;
          }

          // Aberta -> concluir e depois pagar
          if (venda.status === 'ABERTA') {
            this.vendaService.concluirComanda(vendaId)
              .pipe(
                switchMap(() =>
                  this.vendaService.registrarPagamento(vendaId)
                )
              )
              .subscribe({
                next: () => {
                  this.toast.sucesso('Venda quitada com sucesso!');
                  this.limparEstadoPdvAposFechamento(idCliente);
                },
                error: (err) => {
                  console.error(
                    'Erro ao finalizar comanda:',
                    err
                  );

                  this.toast.erro(
                    'Falha ao encerrar a comanda no servidor.'
                  );
                }
              });

            return;
          }

          this.toast.erro(
            'Esta venda não pode ser paga neste estado.'
          );
        },
        error: (err) => {
          console.error(
            'Erro ao consultar o estado da comanda:',
            err
          );

          this.toast.erro(
            'Não foi possível verificar o estado da comanda.'
          );
        }
      });

      return;
    }

    // Cliente real -> deixar conta pendente
    this.vendaService.concluirComanda(vendaId)
      .subscribe({
        next: () => {
          this.toast.sucesso(
            'Conta pendurada (Fiado) registrada!'
          );

          this.limparEstadoPdvAposFechamento(idCliente);
        },
        error: (err) => {
          console.error(
            'Erro ao concluir comanda:',
            err
          );

          this.toast.erro(
            'Falha ao encerrar comanda no servidor.'
          );
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

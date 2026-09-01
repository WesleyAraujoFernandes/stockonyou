import { Component, inject, OnInit, signal } from '@angular/core';
import { ProdutoService } from '../../core/services/produto.service';
import { Produto } from '../../core/model/produto.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-produtos',
  imports: [CommonModule, FormsModule],
  templateUrl: './produtos.html',
  styleUrl: './produtos.css',
})
export class Produtos implements OnInit {
  private readonly produtoService = inject(ProdutoService);
  produtos = signal<Produto[]>([]);
  filtroNome = '';
  filtroPrecoMin?: number;
  filtroPrecoMax?: number;

  ngOnInit(): void {
    this.carregarProdutos();
  }

  carregarProdutos(): void {
    this.produtoService
      .listarComFiltros(this.filtroNome, this.filtroPrecoMin, this.filtroPrecoMax)
      .subscribe({
        next: (response) => this.produtos.set(response.content),
        error: (err) => console.error('Erro ao carregar produto:', err),
      });
  }

  limparFiltros(): void {
    this.filtroNome = '';
    this.filtroPrecoMin = undefined;
    this.filtroPrecoMax = undefined;
    this.carregarProdutos();
  }
}

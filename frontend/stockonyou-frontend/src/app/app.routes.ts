import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  // 1. Redirecionamento inicial para a raiz protegida
  {
    path: '',
    redirectTo: 'cadastros/produtos',
    pathMatch: 'full',
  },

  // 2. Rota pública de Login (Sem o layout de Dashboard/Sidebar)
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
  },

  // 3. Grupo de rotas protegidas que usam o Layout com Header + Sidebar Vertical
  {
    path: '',
    loadComponent: () =>
      import('./layouts/dashboard-layout/dashboard-layout').then((m) => m.DashboardLayout),
    canActivate: [authGuard], // Protege o layout e todas as rotas filhas
    children: [
      // Subgrupo: Cadastros
      {
        path: 'cadastros/categorias',
        loadComponent: () =>
          import('./pages/cadastros/categorias/categorias').then((m) => m.Categorias),
      },
      {
        path: 'cadastros/produtos',
        loadComponent: () => import('./pages/produtos/produtos').then((m) => m.Produtos),
      },

      // Subgrupo: Vendas
      {
        path: 'vendas/pdv',
        loadComponent: () => import('./pages/nova-venda/nova-venda').then((m) => m.NovaVenda),
      },
      /*
        COMENTADO TEMPORARIAMENTE (Para não quebrar o ng serve até criarmos o arquivo)
        {
          path: 'vendas/historico',
          loadComponent: () => import('./pages/historico-venda/historico-venda').then((m) => m.HistoricoVendas),
        }
      */
    ],
  },

  // 4. Manutenção de compatibilidade para acessos diretos a rotas legadas
  {
    path: 'produtos',
    redirectTo: 'cadastros/produtos',
    pathMatch: 'full',
  },
  {
    path: 'cadastros',
    redirectTo: 'cadastros/produtos',
    pathMatch: 'full',
  },

  // 5. Wildcard (Rota coringa) redirecionando para a raiz protegida
  {
    path: '**',
    redirectTo: 'cadastros/produtos',
  },
];

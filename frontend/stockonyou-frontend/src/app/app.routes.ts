import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [

  {
    path: '',
    redirectTo: 'cadastros/produtos',
    pathMatch: 'full',
  },

 
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
  },


  {
    path: '',
    loadComponent: () =>
      import('./layouts/dashboard-layout/dashboard-layout').then((m) => m.DashboardLayout),
    canActivate: [authGuard],
    children: [

      {
        path: 'cadastros/categorias',
        loadComponent: () =>
          import('./pages/cadastros/categorias/categorias').then((m) => m.Categorias),
      },
      {
        path: 'cadastros/produtos',
        loadComponent: () => import('./pages/produtos/produtos').then((m) => m.Produtos),
      },


      {
        path: 'vendas/pdv',
        loadComponent: () => import('./pages/nova-venda/nova-venda').then((m) => m.NovaVenda),
      },
      {
        path: 'vendas/historico',
        loadComponent: () => import('./pages/historico-venda/historico-venda').then((m) => m.HistoricoVenda),
      }
    ],
  },


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

  {
    path: '**',
    redirectTo: 'cadastros/produtos',
  },
];

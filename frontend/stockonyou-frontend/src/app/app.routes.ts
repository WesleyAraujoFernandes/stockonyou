import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  // 1. Redirecionamento inicial para a tela de login customizada
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },

  // 2. Rota pública de Login (sem o layout de Dashboard/Sidebar)
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
  },

  // 3. Grupo de rotas protegidas que usam o Layout com Header + Sidebar Vertical
  {
    path: 'cadastros',
    loadComponent: () =>
      import('./layouts/dashboard-layout/dashboard-layout').then((m) => m.DashboardLayout),
    canActivate: [authGuard], // Protege o layout e todas as rotas filhas
    children: [
      {
        path: '',
        redirectTo: 'categorias',
        pathMatch: 'full',
      },
      {
        path: 'categorias',
        loadComponent: () =>
          import('./pages/cadastros/categorias/categorias').then((m) => m.Categorias),
      },
      {
        path: 'produtos',
        loadComponent: () => import('./pages/produtos/produtos').then((m) => m.Produtos),
      },
    ],
  },

  // 4. Manutenção de compatibilidade para acessos diretos à rota legada /produtos
  {
    path: 'produtos',
    redirectTo: 'cadastros/produtos',
    pathMatch: 'full',
  },

  // 5. Wildcard (Rota coringa) redirecionando para o login
  {
    path: '**',
    redirectTo: 'login',
  },
];

import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { Header } from '../../components/header/header';
import { LucideDynamicIcon, LucideIcon,LucideMenu, LucideTags, LucideBox, LucideShoppingCart, LucideGalleryHorizontalEnd } from '@lucide/angular';
import { Toast } from "../../components/toast/toast";

interface MenuItem {
  label: string;
  route: string;
  icon: LucideIcon;
  badge?: string;
}

@Component({
  selector: 'app-dashboard-layout',
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    Header,
    LucideDynamicIcon,
    LucideMenu
    ,
    Toast
  ],
  templateUrl: './dashboard-layout.html',
  styleUrl: './dashboard-layout.css',
})
export class DashboardLayout {
  isSidebarOpen = signal<boolean>(true);

  menuItems: MenuItem[] = [
    {
      label: 'Categorias',
      route: '/cadastros/categorias',
      icon: LucideTags,
    },
    {
      label: 'Produtos',
      route: '/cadastros/produtos',
      icon: LucideBox,
    },

    {
      label: 'Nova Venda',
      route: '/vendas/pdv',
      icon: LucideShoppingCart
    },
    {
      label: 'Histórico de Vendas',
      route: '/vendas/historico',
      icon: LucideGalleryHorizontalEnd
    }
  ];

  toggleSidebar(): void {
    this.isSidebarOpen.update((value) => !value);
  }
}

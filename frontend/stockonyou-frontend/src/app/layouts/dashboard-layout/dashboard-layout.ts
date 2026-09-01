import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { Header } from '../../components/header/header';
// IMPORTAÇÃO CORRETA: LucideDynamicIcon gerencia os binds [lucideIcon] dinâmicos
import { LucideDynamicIcon, LucideMenu, LucideTags, LucideBox } from '@lucide/angular';
import { Toast } from "../../components/toast/toast";

interface MenuItem {
  label: string;
  route: string;
  icon: any; 
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
    LucideDynamicIcon, // Dá superpoderes dinâmicos para a tag <svg [lucideIcon]="...">
    LucideMenu // Habilita o uso direto de <svg lucideMenu>
    ,
    Toast
],
  templateUrl: './dashboard-layout.html',
  styleUrl: './dashboard-layout.css',
})
export class DashboardLayout {
  isSidebarOpen = signal<boolean>(true);

  // Atribuindo os ícones oficiais
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
  ];

  toggleSidebar(): void {
    this.isSidebarOpen.update((value) => !value);
  }
}

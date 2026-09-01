import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { Header } from '../../components/header/header'; // Ajuste o caminho se necessário

interface MenuItem {
  label: string;
  route: string;
  iconPath: string;
  badge?: string;
}
@Component({
  selector: 'app-dashboard-layout',
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, Header],
  templateUrl: './dashboard-layout.html',
  styleUrl: './dashboard-layout.css',
})
export class DashboardLayout {
  isSidebarOpen = signal<boolean>(true);

  menuItems: MenuItem[] = [
    {
      label: 'Categorias',
      route: '/cadastros/categorias',
      // Ícone Folder / Tag
      iconPath:
        'M9.568 3 3.8 8.768A1 1 0 0 0 3.5 9.475V19.5A2.25 2.25 0 0 0 5.75 21h12.5A2.25 2.25 0 0 0 20.5 18.75V9.475a1 1 0 0 0-.3-.707L14.432 3a1 1 0 0 0-.707-.293H10.275a1 1 0 0 0-.707.293Z',
    },
    {
      label: 'Produtos',
      route: '/cadastros/produtos',
      // Ícone Box / Cube
      iconPath:
        'm21 7.5-9-5.25L3 7.5m18 0-9 5.25m9-5.25v9l-9 5.25M3 7.5l9 5.25M3 7.5v9l9 5.25m0-9v9',
    },
  ];

  toggleSidebar(): void {
    this.isSidebarOpen.update((value) => !value);
  }
}

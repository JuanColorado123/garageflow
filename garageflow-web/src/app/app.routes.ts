import { Routes } from '@angular/router';
import { auditoriaGuard } from './core/usuario/rol.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'ordenes', pathMatch: 'full' },
  {
    path: 'ordenes',
    loadComponent: () =>
      import('./features/ordenes/pages/ordenes-page.component').then(
        (m) => m.OrdenesPageComponent
      ),
  },
  {
    path: 'ordenes/:id',
    loadComponent: () =>
      import('./features/ordenes/pages/orden-detalle-page.component').then(
        (m) => m.OrdenDetallePageComponent
      ),
  },
  {
    path: 'auditoria',
    canMatch: [auditoriaGuard],
    loadComponent: () =>
      import('./features/auditoria/pages/auditoria-page.component').then(
        (m) => m.AuditoriaPageComponent
      ),
  },
  { path: '**', redirectTo: 'ordenes' },
];

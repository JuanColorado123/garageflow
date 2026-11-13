import { Component, inject, computed } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { UsuarioService } from './core/usuario/usuario.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="bg-slate-900 text-white">
      <div class="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-4 px-4 py-3">
        <a routerLink="/ordenes" class="flex items-center gap-2 rounded text-lg font-semibold tracking-tight focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white">
          <span class="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-500 text-sm font-bold" aria-hidden="true">GF</span>
          GarageFlow
        </a>
        <nav aria-label="Principal" class="flex gap-1">
          <a routerLink="/ordenes" routerLinkActive="bg-white/10 text-white" class="rounded-lg px-3 py-2 text-sm font-medium text-slate-300 hover:bg-white/5 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white">
            Órdenes
          </a>
          @if (veAuditoria()) {
            <a routerLink="/auditoria" routerLinkActive="bg-white/10 text-white" class="rounded-lg px-3 py-2 text-sm font-medium text-slate-300 hover:bg-white/5 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white">
              Auditoría
            </a>
          }
        </nav>
        <div class="flex items-center gap-2">
          <label for="usuario" class="text-sm text-slate-300">Usuario</label>
          <select id="usuario" class="rounded-lg border-0 bg-white/10 py-1.5 pl-3 pr-8 text-sm text-white focus:outline-none focus:ring-2 focus:ring-white" (change)="elegir($any($event.target).value)">
            @for (u of usuarios; track u.usuario) {
              <option class="text-slate-900" [value]="u.usuario" [selected]="u.usuario === usuarioActual()">
                {{ u.usuario }}
              </option>
            }
          </select>
          <span class="badge bg-white/10 text-white ring-white/20">{{ etiquetaRol() }}</span>
        </div>
      </div>
    </header>
    <main class="mx-auto max-w-6xl px-4 py-8">
      <router-outlet></router-outlet>
    </main>
  `,
})
export class AppComponent {
  private usuarioService = inject(UsuarioService);

  get usuarios() {
    return this.usuarioService.usuarios;
  }

  usuarioActual = computed(() => this.usuarioService.usuario());
  etiquetaRol = computed(() => this.usuarioService.etiquetaRol());
  veAuditoria = computed(() => this.usuarioService.veAuditoria());

  elegir(usuario: string) {
    this.usuarioService.elegir(usuario);
    window.location.reload();
  }
}

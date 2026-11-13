import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuditoriaApi } from '../data-access/auditoria.api';
import { HistorialTablaComponent } from '../components/historial-tabla.component';
import { CambioDeEstado } from '../data-access/auditoria.models';
import { mensajeDeError } from '../../../core/errores/mensaje-error';

@Component({
  selector: 'app-auditoria-page',
  standalone: true,
  imports: [CommonModule, HistorialTablaComponent],
  template: `
    <h1 class="text-2xl font-semibold text-slate-900">Auditoría</h1>
    <p class="mt-2 text-sm text-slate-500">Quién cambió el estado de cada orden y cuándo. Este registro no se puede modificar.</p>

    @if (mensajeError()) {
      <div role="alert" class="alert-error mt-6">{{ mensajeError() }}</div>
    }

    <section class="card mt-6">
      <div class="card-header">
        <h2 class="card-title">Últimos cambios</h2>
      </div>
      <app-historial-tabla [cambios]="cambios()" [mostrarOrden]="true"></app-historial-tabla>
    </section>
  `,
})
export class AuditoriaPageComponent implements OnInit {
  private auditoriaApi = inject(AuditoriaApi);

  cambios = signal<CambioDeEstado[]>([]);
  mensajeError = signal<string>('');

  ngOnInit() {
    this.cargarHistorial();
  }

  cargarHistorial() {
    this.auditoriaApi.historial().subscribe({
      next: (cambios) => {
        this.cambios.set(cambios);
      },
      error: (error) => {
        this.mensajeError.set(mensajeDeError(error));
      },
    });
  }
}


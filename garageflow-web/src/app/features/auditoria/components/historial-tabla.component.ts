import { Component, input } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CambioDeEstado } from '../data-access/auditoria.models';
import { estadoOrden } from '../../../shared/ui/etiquetas';

@Component({
  selector: 'app-historial-tabla',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink],
  template: `
    @if (cambios().length > 0) {
      <div class="overflow-x-auto">
        <table class="tabla">
          <thead>
            <tr>
              @if (mostrarOrden()) {
                <th>Orden</th>
              }
              <th>Fecha y hora</th>
              <th>Usuario</th>
              <th>Cambio</th>
              <th>Motivo</th>
            </tr>
          </thead>
          <tbody>
            @for (c of cambios(); track c.eventoId) {
              <tr>
                @if (mostrarOrden()) {
                  <td>
                    <a class="link font-mono" [routerLink]="['/ordenes', c.ordenId]">
                      {{ c.ordenId.slice(0, 8) }}
                    </a>
                  </td>
                }
                <td>{{ c.ocurridoEn | date: 'dd/MM/yyyy HH:mm' }}</td>
                <td>{{ c.usuario }}</td>
                <td>
                  <div class="flex flex-wrap items-center gap-2 text-sm">
                    @if (c.estadoAnterior) {
                      <span class="badge" [ngClass]="estadoOrden(c.estadoAnterior).clases">
                        {{ estadoOrden(c.estadoAnterior).texto }}
                      </span>
                    } @else {
                      <span class="text-slate-500 text-xs">Registro inicial</span>
                    }
                    <span class="text-slate-400" aria-hidden="true">→</span>
                    <span class="badge" [ngClass]="estadoOrden(c.estadoNuevo).clases">
                      {{ estadoOrden(c.estadoNuevo).texto }}
                    </span>
                  </div>
                </td>
                <td>{{ c.motivo ?? '-' }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    } @else {
      <p class="px-5 py-12 text-center text-sm text-slate-500">No hay cambios registrados.</p>
    }
  `,
})
export class HistorialTablaComponent {
  cambios = input.required<CambioDeEstado[]>();
  mostrarOrden = input(false);

  protected readonly estadoOrden = estadoOrden;
}


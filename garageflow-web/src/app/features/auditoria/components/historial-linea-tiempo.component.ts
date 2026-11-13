import { Component, input } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { CambioDeEstado } from '../data-access/auditoria.models';
import { estadoOrden } from '../../../shared/ui/etiquetas';

@Component({
  selector: 'app-historial-linea-tiempo',
  standalone: true,
  imports: [CommonModule, DatePipe],
  host: { class: 'block' },
  template: `
    @if (cambios().length === 0) {
      <p class="text-sm text-slate-500">Todavía no hay cambios registrados.</p>
    } @else {
      <ol class="space-y-0">
        @for (c of cambios(); track c.eventoId; let ultimo = $last) {
          <li class="flex gap-4">
            <!-- left column -->
            <div class="flex flex-col items-center">
              <span
                class="mt-1.5 h-2.5 w-2.5 shrink-0 rounded-full"
                [ngClass]="estadoOrden(c.estadoNuevo).punto"
              ></span>
              @if (!ultimo) {
                <span class="mt-1 w-px flex-1 bg-slate-200"></span>
              }
            </div>
            <!-- right column -->
            <div class="pb-6">
              <div class="flex flex-wrap items-center gap-2 text-sm">
                @if (c.estadoAnterior) {
                  <span class="sr-only">De</span>
                  <span class="badge" [ngClass]="estadoOrden(c.estadoAnterior).clases">
                    {{ estadoOrden(c.estadoAnterior).texto }}
                  </span>
                  <span class="text-slate-400" aria-hidden="true">→</span>
                  <span class="sr-only">a</span>
                } @else {
                  <span class="text-slate-500">Registro inicial:</span>
                }
                <span class="badge" [ngClass]="estadoOrden(c.estadoNuevo).clases">
                  {{ estadoOrden(c.estadoNuevo).texto }}
                </span>
              </div>
              <p class="mt-1 text-xs text-slate-500">
                <time>{{ c.ocurridoEn | date: 'dd/MM/yyyy HH:mm' }}</time> · {{ c.usuario }}
              </p>
              @if (c.motivo) {
                <p class="mt-2 rounded-lg bg-slate-50 px-3 py-2 text-sm text-slate-700">
                  Motivo: {{ c.motivo }}
                </p>
              }
            </div>
          </li>
        }
      </ol>
    }
  `,
})
export class HistorialLineaTiempoComponent {
  cambios = input.required<CambioDeEstado[]>();

  protected readonly estadoOrden = estadoOrden;
}

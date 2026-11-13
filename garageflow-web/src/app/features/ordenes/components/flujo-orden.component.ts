import { Component, input, computed } from '@angular/core';
import { NgClass } from '@angular/common';
import { FLUJO_ORDEN, estadoOrden } from '../../../shared/ui/etiquetas';

type Paso = 'completada' | 'actual' | 'pendiente';

@Component({
  selector: 'app-flujo-orden',
  standalone: true,
  imports: [NgClass],
  host: { class: 'block' },
  template: `
    <div>
      <div class="h-1.5 rounded-full bg-slate-100">
        <div
          class="h-1.5 rounded-full bg-indigo-600 transition-all"
          [style.width.%]="progreso()"
        ></div>
      </div>

      <ol
        class="mt-4 grid gap-3 sm:grid-cols-5"
        aria-label="Etapas de la orden"
      >
        @for (etapa of etapas(); track etapa.valor) {
          <li
            class="flex items-center gap-3 sm:flex-col sm:text-center"
            [attr.aria-current]="etapa.paso === 'actual' ? 'step' : null"
          >
            <span
              class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-sm font-semibold"
              [ngClass]="circulo[etapa.paso]"
            >
              @if (etapa.paso === 'completada') {
                <svg
                  class="h-4 w-4"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                  aria-hidden="true"
                >
                  <path
                    fill-rule="evenodd"
                    d="M16.704 4.153a.75.75 0 0 1 .143 1.052l-8 10.5a.75.75 0 0 1-1.127.075l-4.5-4.5a.75.75 0 0 1 1.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 0 1 1.05-.143Z"
                    clip-rule="evenodd"
                  />
                </svg>
              } @else {
                {{ etapa.indice + 1 }}
              }
            </span>
            <div class="flex flex-col gap-1">
              <span class="text-sm" [ngClass]="texto[etapa.paso]">
                {{ etapa.texto }}
              </span>
              @if (etapa.paso === 'actual') {
                <span class="text-xs text-indigo-600">Etapa actual</span>
              } @else if (etapa.paso === 'completada') {
                <span class="text-xs text-emerald-600">Completada</span>
              }
            </div>
          </li>
        }
      </ol>
    </div>
  `,
})
export class FlujoOrdenComponent {
  estado = input.required<string>();

  protected readonly circulo: Record<Paso, string> = {
    completada: 'bg-emerald-600 text-white',
    actual: 'bg-indigo-600 text-white ring-4 ring-indigo-100',
    pendiente: 'bg-white text-slate-400 ring-1 ring-inset ring-slate-300',
  };

  protected readonly texto: Record<Paso, string> = {
    completada: 'text-slate-700',
    actual: 'font-semibold text-indigo-700',
    pendiente: 'text-slate-400',
  };

  etapas = computed(() => {
    const estadoActual = this.estado();
    const indiceActual = FLUJO_ORDEN.indexOf(
      estadoActual as (typeof FLUJO_ORDEN)[number]
    );

    return FLUJO_ORDEN.map((valor, indice) => {
      let paso: Paso;
      if (indice < indiceActual) {
        paso = 'completada';
      } else if (indice === indiceActual) {
        paso = 'actual';
      } else {
        paso = 'pendiente';
      }

      return {
        valor,
        texto: estadoOrden(valor).texto,
        paso,
        indice,
      };
    });
  });

  progreso = computed(() => {
    const estadoActual = this.estado();
    const indiceActual = FLUJO_ORDEN.indexOf(
      estadoActual as (typeof FLUJO_ORDEN)[number]
    );
    return Math.max(0, indiceActual) / (FLUJO_ORDEN.length - 1) * 100;
  });
}

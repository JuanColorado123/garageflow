import { Component, OnInit, inject, signal, viewChild } from '@angular/core';
import { CommonModule, DatePipe, NgClass } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrdenesApi } from '../data-access/ordenes.api';
import { Orden } from '../data-access/ordenes.models';
import { CitasOrdenComponent } from '../../agenda/components/citas-orden.component';
import { FlujoOrdenComponent } from '../components/flujo-orden.component';
import { HistorialLineaTiempoComponent } from '../../auditoria/components/historial-linea-tiempo.component';
import { AuditoriaApi } from '../../auditoria/data-access/auditoria.api';
import { CambioDeEstado } from '../../auditoria/data-access/auditoria.models';
import { mensajeDeError } from '../../../core/errores/mensaje-error';
import { estadoOrden, origen } from '../../../shared/ui/etiquetas';

@Component({
  selector: 'app-orden-detalle-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    DatePipe,
    NgClass,
    CitasOrdenComponent,
    FlujoOrdenComponent,
    HistorialLineaTiempoComponent,
  ],
  template: `
    @if (orden(); as o) {
      <a routerLink="/ordenes" class="link text-sm">← Volver a órdenes</a>

      <div class="mt-3 flex flex-wrap items-start justify-between gap-4">
        <div>
          <p class="text-sm text-slate-500">
            Orden <span class="font-mono">{{ o.id.slice(0, 8) }}</span>
          </p>
          <h1 class="mt-1 text-2xl font-semibold text-slate-900">{{ o.clienteNombre }}</h1>
          <p class="mt-1 flex items-center gap-2">
            <span class="font-mono text-sm font-semibold uppercase text-slate-900">
              {{ o.vehiculoPlaca }}
            </span>
            @if (o.vehiculoDescripcion) {
              <span class="text-sm text-slate-500">{{ o.vehiculoDescripcion }}</span>
            }
          </p>
        </div>
        <span class="badge" [ngClass]="estadoOrden(o.estado).clases">
          {{ estadoOrden(o.estado).texto }}
        </span>
      </div>

      @if (mensajeError()) {
        <div role="alert" class="alert-error mt-4">{{ mensajeError() }}</div>
      }

      <section class="card mt-6" aria-labelledby="titulo-flujo">
        <div class="card-header flex items-center justify-between">
          <h2 id="titulo-flujo" class="card-title">Flujo de la orden</h2>
          <button type="button" (click)="actualizar()" class="btn btn-secondary btn-sm">
            Actualizar
          </button>
        </div>
        <div class="card-body">
          <app-flujo-orden [estado]="o.estado" />

          <div class="mt-6 flex flex-wrap items-end gap-4 border-t border-slate-100 pt-5">
            @if (o.accionesPermitidas.includes('AVANZAR')) {
              <button
                type="button"
                (click)="avanzar()"
                [disabled]="cargando()"
                class="btn btn-primary"
              >
                Avanzar a {{ estadoOrden(o.siguienteEstado!).texto }}
              </button>
            }
            @if (o.accionesPermitidas.includes('REABRIR')) {
              <form
                [formGroup]="reabrirForm"
                (ngSubmit)="reabrir()"
                class="flex w-full flex-col gap-3 sm:flex-row sm:items-end"
              >
                <div class="flex-1">
                  <label for="motivo" class="label">Motivo de la reapertura</label>
                  <textarea
                    id="motivo"
                    rows="2"
                    class="input"
                    formControlName="motivo"
                  ></textarea>
                </div>
                <button
                  type="submit"
                  [disabled]="cargando()"
                  class="btn btn-secondary"
                >
                  Reabrir orden
                </button>
              </form>
            }
            @if (
              !o.accionesPermitidas.includes('AVANZAR') &&
              !o.accionesPermitidas.includes('REABRIR')
            ) {
              <p class="text-sm text-slate-500">Esta orden no tiene acciones disponibles.</p>
            }
          </div>
        </div>
      </section>

      <div class="mt-6 grid gap-6 lg:grid-cols-3">
        <div class="space-y-6 lg:col-span-2">
          <app-citas-orden [ordenId]="o.id" (cambio)="actualizar()" />

          <section class="card">
            <div class="card-header">
              <h2 class="card-title">Historial</h2>
            </div>
            <div class="card-body">
              <app-historial-linea-tiempo [cambios]="historial()" />
            </div>
          </section>
        </div>

        <aside>
          <section class="card">
            <div class="card-header">
              <h2 class="card-title">Datos de la orden</h2>
            </div>
            <dl class="card-body grid gap-4 text-sm">
              <div>
                <dt class="text-xs font-medium uppercase tracking-wide text-slate-500">
                  Cliente
                </dt>
                <dd class="mt-1 text-slate-900">{{ o.clienteNombre }}</dd>
              </div>
              <div>
                <dt class="text-xs font-medium uppercase tracking-wide text-slate-500">
                  Teléfono
                </dt>
                <dd class="mt-1 text-slate-900">{{ o.clienteTelefono }}</dd>
              </div>
              <div>
                <dt class="text-xs font-medium uppercase tracking-wide text-slate-500">
                  Placa
                </dt>
                <dd class="mt-1 font-mono text-slate-900">{{ o.vehiculoPlaca }}</dd>
              </div>
              <div>
                <dt class="text-xs font-medium uppercase tracking-wide text-slate-500">
                  Vehículo
                </dt>
                <dd class="mt-1 text-slate-900">
                  {{ o.vehiculoDescripcion || '-' }}
                </dd>
              </div>
              <div>
                <dt class="text-xs font-medium uppercase tracking-wide text-slate-500">
                  Aseguradora
                </dt>
                <dd class="mt-1 text-slate-900">
                  {{ o.aseguradora || 'Particular' }}
                </dd>
              </div>
              <div>
                <dt class="text-xs font-medium uppercase tracking-wide text-slate-500">
                  Trabajo
                </dt>
                <dd class="mt-1 text-slate-900">{{ o.trabajo }}</dd>
              </div>
              <div>
                <dt class="text-xs font-medium uppercase tracking-wide text-slate-500">
                  Origen
                </dt>
                <dd class="mt-1 text-slate-900">{{ origen(o.origen) }}</dd>
              </div>
              <div>
                <dt class="text-xs font-medium uppercase tracking-wide text-slate-500">
                  Creada
                </dt>
                <dd class="mt-1 text-slate-900">
                  {{ o.creadaEn | date: 'dd/MM/yyyy HH:mm' }}
                </dd>
              </div>
              <div>
                <dt class="text-xs font-medium uppercase tracking-wide text-slate-500">
                  Actualizada
                </dt>
                <dd class="mt-1 text-slate-900">
                  {{ o.actualizadaEn | date: 'dd/MM/yyyy HH:mm' }}
                </dd>
              </div>
            </dl>
          </section>
        </aside>
      </div>
    } @else {
      <p class="text-slate-500">Cargando...</p>
    }
  `,
})
export class OrdenDetallePageComponent implements OnInit {
  private ordenesApi = inject(OrdenesApi);
  private auditoriaApi = inject(AuditoriaApi);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);

  protected readonly estadoOrden = estadoOrden;
  protected readonly origen = origen;

  orden = signal<Orden | null>(null);
  historial = signal<CambioDeEstado[]>([]);
  mensajeError = signal<string>('');
  cargando = signal(false);

  reabrirForm = this.fb.nonNullable.group({ motivo: [''] });

  citasComponent = viewChild(CitasOrdenComponent);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarOrden(id);
      this.cargarHistorial(id);
    }
  }

  cargarOrden(id: string) {
    this.ordenesApi.obtener(id).subscribe({
      next: (orden) => {
        this.orden.set(orden);
        this.mensajeError.set('');
      },
      error: (error) => {
        this.mensajeError.set(mensajeDeError(error));
      },
    });
  }

  cargarHistorial(id: string) {
    this.auditoriaApi.historial(id).subscribe({
      next: (cambios) => {
        this.historial.set(cambios);
      },
    });
  }

  avanzar() {
    const id = this.orden()?.id;
    if (!id) return;

    this.cargando.set(true);
    this.ordenesApi.avanzar(id).subscribe({
      next: (orden) => {
        this.orden.set(orden);
        this.cargarHistorial(id);
        this.cargarCitas();
        this.mensajeError.set('');
        this.cargando.set(false);
      },
      error: (error) => {
        this.mensajeError.set(mensajeDeError(error));
        this.cargando.set(false);
      },
    });
  }

  reabrir() {
    const id = this.orden()?.id;
    if (!id) return;

    this.cargando.set(true);
    this.ordenesApi.reabrir(id, this.reabrirForm.getRawValue().motivo).subscribe({
      next: (orden) => {
        this.orden.set(orden);
        this.cargarHistorial(id);
        this.cargarCitas();
        this.reabrirForm.reset();
        this.mensajeError.set('');
        this.cargando.set(false);
      },
      error: (error) => {
        this.mensajeError.set(mensajeDeError(error));
        this.cargando.set(false);
      },
    });
  }

  actualizar() {
    const id = this.orden()?.id;
    if (!id) return;

    this.cargarOrden(id);
    this.cargarHistorial(id);
    this.cargarCitas();
  }

  cargarCitas() {
    this.citasComponent()?.cargar();
  }
}

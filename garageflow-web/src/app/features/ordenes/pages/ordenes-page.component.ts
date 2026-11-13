import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UsuarioService } from '../../../core/usuario/usuario.service';
import { OrdenesApi } from '../data-access/ordenes.api';
import { Orden, RegistrarOrden } from '../data-access/ordenes.models';
import { mensajeDeError, erroresDeCampos } from '../../../core/errores/mensaje-error';
import { FLUJO_ORDEN, estadoOrden } from '../../../shared/ui/etiquetas';

@Component({
  selector: 'app-ordenes-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="space-y-6">
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">Órdenes</h1>
        <p class="mt-1 text-sm text-slate-500">
          Registra los vehículos y sigue en qué etapa va cada uno.
        </p>
      </div>

      @if (mensajeError()) {
        <div role="alert" class="alert-error">{{ mensajeError() }}</div>
      }

      <section aria-label="Órdenes por etapa" class="grid grid-cols-2 gap-3 sm:grid-cols-5">
        @for (item of conteo(); track item.estado) {
          <div class="card px-4 py-3">
            <div class="flex items-center gap-2">
              <span
                class="h-2 w-2 rounded-full"
                [ngClass]="item.etiqueta.punto"
              ></span>
              <span class="text-xs font-medium text-slate-500">
                {{ item.etiqueta.texto }}
              </span>
            </div>
            <p class="mt-1 text-2xl font-semibold text-slate-900">
              {{ item.total }}
            </p>
          </div>
        }
      </section>

      <div class="grid items-start gap-6" [ngClass]="puedeModificar() ? 'lg:grid-cols-3' : 'lg:grid-cols-3'">
        @if (puedeModificar()) {
          <section class="card lg:col-span-1">
            <div class="card-header">
              <h2 class="card-title">Nueva orden</h2>
            </div>
            <form [formGroup]="formulario" (ngSubmit)="registrar()" class="card-body space-y-4">
              <div>
                <label for="clienteNombre" class="label">
                  Cliente <span class="text-rose-600" aria-hidden="true">*</span>
                </label>
                <input
                  id="clienteNombre"
                  type="text"
                  class="input"
                  formControlName="clienteNombre"
                  required
                  aria-required="true"
                  [attr.aria-describedby]="erroresField()['clienteNombre'] ? 'error-clienteNombre' : null"
                />
                @if (erroresField()['clienteNombre']) {
                  <p id="error-clienteNombre" class="field-error">
                    {{ erroresField()['clienteNombre'] }}
                  </p>
                }
              </div>

              <div class="grid gap-4 sm:grid-cols-2">
                <div>
                  <label for="clienteTelefono" class="label">
                    Teléfono <span class="text-rose-600" aria-hidden="true">*</span>
                  </label>
                  <input
                    id="clienteTelefono"
                    type="text"
                    class="input"
                    formControlName="clienteTelefono"
                    required
                    aria-required="true"
                    [attr.aria-describedby]="erroresField()['clienteTelefono'] ? 'error-clienteTelefono' : null"
                  />
                  @if (erroresField()['clienteTelefono']) {
                    <p id="error-clienteTelefono" class="field-error">
                      {{ erroresField()['clienteTelefono'] }}
                    </p>
                  }
                </div>

                <div>
                  <label for="vehiculoPlaca" class="label">
                    Placa <span class="text-rose-600" aria-hidden="true">*</span>
                  </label>
                  <input
                    id="vehiculoPlaca"
                    type="text"
                    class="input"
                    formControlName="vehiculoPlaca"
                    required
                    aria-required="true"
                    [attr.aria-describedby]="erroresField()['vehiculoPlaca'] ? 'error-vehiculoPlaca' : null"
                  />
                  @if (erroresField()['vehiculoPlaca']) {
                    <p id="error-vehiculoPlaca" class="field-error">
                      {{ erroresField()['vehiculoPlaca'] }}
                    </p>
                  }
                </div>
              </div>

              <div>
                <label for="vehiculoDescripcion" class="label">Descripción del vehículo</label>
                <input
                  id="vehiculoDescripcion"
                  type="text"
                  class="input"
                  formControlName="vehiculoDescripcion"
                />
              </div>

              <div>
                <label for="aseguradora" class="label">Aseguradora</label>
                <input
                  id="aseguradora"
                  type="text"
                  class="input"
                  formControlName="aseguradora"
                />
              </div>

              <div>
                <label for="trabajo" class="label">
                  Trabajo <span class="text-rose-600" aria-hidden="true">*</span>
                </label>
                <textarea
                  id="trabajo"
                  rows="3"
                  class="input"
                  formControlName="trabajo"
                  required
                  aria-required="true"
                  [attr.aria-describedby]="erroresField()['trabajo'] ? 'error-trabajo' : null"
                ></textarea>
                @if (erroresField()['trabajo']) {
                  <p id="error-trabajo" class="field-error">
                    {{ erroresField()['trabajo'] }}
                  </p>
                }
              </div>

              <button type="submit" class="btn btn-primary w-full">
                Registrar orden
              </button>
            </form>
          </section>
        }

        <section class="card" [ngClass]="puedeModificar() ? 'lg:col-span-2' : 'lg:col-span-3'">
          <div class="card-header flex items-center justify-between">
            <h2 class="card-title">Listado</h2>
            <span class="badge bg-slate-100 text-slate-600 ring-slate-500/20">
              {{ ordenes().length }}
            </span>
          </div>
          <div class="overflow-x-auto">
            @if (ordenes().length > 0) {
              <table class="tabla">
                <thead>
                  <tr>
                    <th>Cliente</th>
                    <th>Vehículo</th>
                    <th>Aseguradora</th>
                    <th>Estado</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  @for (o of ordenes(); track o.id) {
                    <tr>
                      <td>
                        <p class="font-medium text-slate-900">{{ o.clienteNombre }}</p>
                        <p class="text-xs text-slate-500">{{ o.clienteTelefono }}</p>
                      </td>
                      <td>
                        <p class="font-mono text-sm font-semibold uppercase text-slate-900">
                          {{ o.vehiculoPlaca }}
                        </p>
                        @if (o.vehiculoDescripcion) {
                          <p class="text-xs text-slate-500">{{ o.vehiculoDescripcion }}</p>
                        }
                      </td>
                      <td>
                        @if (o.aseguradora) {
                          <span class="badge bg-sky-50 text-sky-700 ring-sky-600/20">
                            {{ o.aseguradora }}
                          </span>
                        } @else {
                          <span class="badge bg-slate-100 text-slate-600 ring-slate-500/20">
                            Particular
                          </span>
                        }
                      </td>
                      <td>
                        <span class="badge" [ngClass]="estadoOrden(o.estado).clases">
                          {{ estadoOrden(o.estado).texto }}
                        </span>
                      </td>
                      <td>
                        <a class="link" [routerLink]="['/ordenes', o.id]">Ver detalle</a>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            } @else {
              <p class="px-5 py-12 text-center text-sm text-slate-500">
                No hay órdenes registradas.
              </p>
            }
          </div>
        </section>
      </div>
    </div>
  `,
})
export class OrdenesPageComponent implements OnInit {
  private usuarioService = inject(UsuarioService);
  private ordenesApi = inject(OrdenesApi);
  private fb = inject(FormBuilder);

  protected readonly estadoOrden = estadoOrden;
  protected readonly puedeModificar = computed(() => this.usuarioService.puedeModificar());

  ordenes = signal<Orden[]>([]);
  mensajeError = signal<string>('');
  erroresField = signal<Record<string, string>>({});

  conteo = computed(() =>
    FLUJO_ORDEN.map((estado) => ({
      estado,
      etiqueta: estadoOrden(estado),
      total: this.ordenes().filter((o) => o.estado === estado).length,
    }))
  );

  formulario = this.fb.nonNullable.group({
    clienteNombre: ['', Validators.required],
    clienteTelefono: ['', Validators.required],
    vehiculoPlaca: ['', Validators.required],
    vehiculoDescripcion: [''],
    aseguradora: [''],
    trabajo: ['', Validators.required],
  });

  ngOnInit() {
    this.cargarOrdenes();
  }

  cargarOrdenes() {
    this.ordenesApi.listar().subscribe({
      next: (ordenes) => {
        this.ordenes.set(ordenes);
      },
      error: (error) => {
        this.mensajeError.set(mensajeDeError(error));
      },
    });
  }

  registrar() {
    const datos: RegistrarOrden = {
      clienteNombre: this.formulario.getRawValue().clienteNombre,
      clienteTelefono: this.formulario.getRawValue().clienteTelefono,
      vehiculoPlaca: this.formulario.getRawValue().vehiculoPlaca,
      vehiculoDescripcion: this.formulario.getRawValue().vehiculoDescripcion || '',
      aseguradora: this.formulario.getRawValue().aseguradora || '',
      trabajo: this.formulario.getRawValue().trabajo,
    };

    this.ordenesApi.registrar(datos).subscribe({
      next: (ordenNueva) => {
        this.ordenes.update((ordenes) => [ordenNueva, ...ordenes]);
        this.formulario.reset();
        this.mensajeError.set('');
        this.erroresField.set({});
      },
      error: (error) => {
        this.mensajeError.set(mensajeDeError(error));
        this.erroresField.set(erroresDeCampos(error));
      },
    });
  }
}

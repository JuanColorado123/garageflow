import { Component, input, output, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { UsuarioService } from '../../../core/usuario/usuario.service';
import { AgendaApi } from '../data-access/agenda.api';
import { Cita, Disponibilidad, Asesor } from '../data-access/agenda.models';
import { mensajeDeError } from '../../../core/errores/mensaje-error';
import { estadoCita, tipoCita, motivoCancelacion } from '../../../shared/ui/etiquetas';

@Component({
  selector: 'app-citas-orden',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DatePipe],
  host: { class: 'block' },
  template: `
    <section class="card" aria-labelledby="titulo-citas">
      <div class="card-header">
        <h2 id="titulo-citas" class="card-title">Citas</h2>
      </div>

      @if (mensajeError()) {
        <div class="px-5 pt-4">
          <div role="alert" class="alert-error">{{ mensajeError() }}</div>
        </div>
      }

      @if (citas().length > 0) {
        <div class="overflow-x-auto">
          <table class="tabla">
            <thead>
              <tr>
                <th>Fecha y hora</th>
                <th>Tipo</th>
                <th>Asesor</th>
                <th>Estado</th>
                <th>Motivo</th>
                <th>Acción</th>
              </tr>
            </thead>
            <tbody>
              @for (c of citas(); track c.id) {
                <tr>
                  <td>{{ c.franjaInicio | date: 'dd/MM/yyyy HH:mm' }}</td>
                  <td>{{ tipoCita(c.tipo) }}</td>
                  <td>{{ asesorPorId(c.asesorId) }}</td>
                  <td>
                    <span class="badge" [ngClass]="estadoCita(c.estado).clases">
                      {{ estadoCita(c.estado).texto }}
                    </span>
                  </td>
                  <td>{{ motivoCancelacion(c.motivoCancelacion) }}</td>
                  <td>
                    @if (c.accionesPermitidas.includes('CANCELAR')) {
                      <button type="button" class="btn btn-danger btn-sm" (click)="cancelarCita(c.id)">Cancelar</button>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      } @else {
        <p class="px-5 py-6 text-sm text-slate-500">Esta orden no tiene citas.</p>
      }

      @if (puedeModificar()) {
        <form [formGroup]="formulario" (ngSubmit)="agendar()" class="border-t border-slate-100 p-5">
          <h3 class="text-sm font-semibold text-slate-900">Agendar cita</h3>
          <div class="mt-4 grid gap-4 sm:grid-cols-3">
            <div>
              <label for="fecha" class="label">Fecha</label>
              <input
                id="fecha"
                type="date"
                formControlName="fecha"
                class="input"
                (change)="cargarDisponibilidad()"
              />
            </div>

            <div>
              <label for="franja" class="label">Franja</label>
              <select id="franja" formControlName="franja" class="input">
                <option value="">Seleccionar franja</option>
                @for (d of disponibilidad(); track clave(d)) {
                  @if (d.libres > 0) {
                    <option [value]="clave(d)">
                      {{ d.franjaInicio | slice: 11:16 }} · {{ d.asesorNombre }}
                      ({{ d.libres }} libre{{ d.libres === 1 ? '' : 's' }})
                    </option>
                  }
                }
              </select>
              @if (formulario.get('fecha')?.value && !hayFranjasLibres()) {
                <p class="mt-1 text-xs text-slate-500">No hay franjas libres ese día.</p>
              }
            </div>

            <div>
              <label for="tipo" class="label">Tipo</label>
              <select id="tipo" formControlName="tipo" class="input">
                <option value="">Seleccionar tipo</option>
                <option value="TRAER">Traer el vehículo</option>
                <option value="RECOGER">Recoger el vehículo</option>
              </select>
            </div>
          </div>

          <div class="mt-4 flex items-center justify-end gap-3">
            <button type="submit" class="btn btn-primary">Agendar cita</button>
          </div>
        </form>
      }
    </section>
  `,
})
export class CitasOrdenComponent implements OnInit {
  private usuarioService = inject(UsuarioService);
  private agendaApi = inject(AgendaApi);
  private fb = inject(FormBuilder);

  ordenId = input.required<string>();
  cambio = output<void>();

  citas = signal<Cita[]>([]);
  disponibilidad = signal<Disponibilidad[]>([]);
  asesores = signal<Asesor[]>([]);
  mensajeError = signal<string>('');

  protected readonly estadoCita = estadoCita;
  protected readonly tipoCita = tipoCita;
  protected readonly motivoCancelacion = motivoCancelacion;
  protected readonly puedeModificar = computed(() => this.usuarioService.puedeModificar());

  formulario = this.fb.nonNullable.group({
    fecha: [''],
    franja: [''],
    tipo: [''],
  });

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.cargarCitas();
    this.cargarAsesores();
  }

  cargarCitas() {
    this.agendaApi.citasDeOrden(this.ordenId()).subscribe({
      next: (citas) => {
        this.citas.set(citas);
      },
      error: (error) => {
        this.mensajeError.set(mensajeDeError(error));
      },
    });
  }

  cargarAsesores() {
    this.agendaApi.asesores().subscribe({
      next: (asesores) => {
        this.asesores.set(asesores);
      },
    });
  }

  cargarDisponibilidad() {
    const fecha = this.formulario.get('fecha')?.value;
    if (!fecha) {
      this.disponibilidad.set([]);
      return;
    }

    this.agendaApi.disponibilidad(fecha).subscribe({
      next: (disponibilidad) => {
        this.disponibilidad.set(disponibilidad);
        this.mensajeError.set('');
      },
      error: (error) => {
        this.mensajeError.set(mensajeDeError(error));
        this.disponibilidad.set([]);
      },
    });
  }

  asesorPorId(id: number): string {
    return this.asesores().find((a) => a.id === id)?.nombre ?? '';
  }

  clave(d: Disponibilidad): string {
    return d.asesorId + '|' + d.franjaInicio;
  }

  hayFranjasLibres(): boolean {
    return this.disponibilidad().some((d) => d.libres > 0);
  }

  agendar() {
    const franja = this.formulario.get('franja')?.value;
    const tipo = this.formulario.get('tipo')?.value;

    if (!franja || !tipo) {
      return;
    }

    const disponible = this.disponibilidad().find((d) => this.clave(d) === franja);
    if (!disponible) {
      return;
    }

    this.agendaApi
      .agendar({
        ordenId: this.ordenId(),
        asesorId: disponible.asesorId,
        franjaInicio: disponible.franjaInicio,
        tipo: tipo as 'TRAER' | 'RECOGER',
      })
      .subscribe({
        next: () => {
          this.formulario.reset();
          this.disponibilidad.set([]);
          this.mensajeError.set('');
          this.cargarCitas();
          this.cambio.emit();
        },
        error: (error) => {
          this.mensajeError.set(mensajeDeError(error));
          this.cargarDisponibilidad();
        },
      });
  }

  cancelarCita(id: string) {
    this.agendaApi.cancelar(id).subscribe({
      next: () => {
        this.mensajeError.set('');
        this.cargarCitas();
        this.cambio.emit();
      },
      error: (error) => {
        this.mensajeError.set(mensajeDeError(error));
      },
    });
  }
}

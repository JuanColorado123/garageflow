import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ConfiguracionService } from '../../../core/config/configuracion.service';
import { Cita, Asesor, Disponibilidad, AgendarCita } from './agenda.models';

@Injectable({ providedIn: 'root' })
export class AgendaApi {
  private http = inject(HttpClient);
  private config = inject(ConfiguracionService);

  asesores() {
    return this.http.get<Asesor[]>(`${this.config.agendaUrl}/api/agenda/asesores`);
  }

  disponibilidad(fecha: string) {
    const params = new HttpParams().set('fecha', fecha);
    return this.http.get<Disponibilidad[]>(`${this.config.agendaUrl}/api/agenda/disponibilidad`, {
      params,
    });
  }

  citasDeOrden(ordenId: string) {
    const params = new HttpParams().set('ordenId', ordenId);
    return this.http.get<Cita[]>(`${this.config.agendaUrl}/api/agenda/citas`, { params });
  }

  agendar(datos: AgendarCita) {
    return this.http.post<Cita>(`${this.config.agendaUrl}/api/agenda/citas`, datos);
  }

  cancelar(id: string) {
    return this.http.post<Cita>(
      `${this.config.agendaUrl}/api/agenda/citas/${id}/cancelar`,
      null
    );
  }
}

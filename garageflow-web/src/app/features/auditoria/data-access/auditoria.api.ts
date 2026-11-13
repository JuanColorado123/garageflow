import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ConfiguracionService } from '../../../core/config/configuracion.service';
import { CambioDeEstado } from './auditoria.models';

@Injectable({ providedIn: 'root' })
export class AuditoriaApi {
  private http = inject(HttpClient);
  private config = inject(ConfiguracionService);

  historial(ordenId?: string) {
    let params = new HttpParams();
    if (ordenId) {
      params = params.set('ordenId', ordenId);
    }
    return this.http.get<CambioDeEstado[]>(`${this.config.auditoriaUrl}/api/auditoria/historial`, {
      params,
    });
  }
}

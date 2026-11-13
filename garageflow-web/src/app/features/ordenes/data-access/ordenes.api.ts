import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ConfiguracionService } from '../../../core/config/configuracion.service';
import { Orden, RegistrarOrden } from './ordenes.models';

@Injectable({ providedIn: 'root' })
export class OrdenesApi {
  private http = inject(HttpClient);
  private config = inject(ConfiguracionService);

  listar() {
    return this.http.get<Orden[]>(`${this.config.ordenesUrl}/api/ordenes`);
  }

  obtener(id: string) {
    return this.http.get<Orden>(`${this.config.ordenesUrl}/api/ordenes/${id}`);
  }

  registrar(datos: RegistrarOrden) {
    return this.http.post<Orden>(`${this.config.ordenesUrl}/api/ordenes`, datos);
  }

  avanzar(id: string) {
    return this.http.post<Orden>(`${this.config.ordenesUrl}/api/ordenes/${id}/avanzar`, null);
  }

  reabrir(id: string, motivo: string) {
    return this.http.post<Orden>(
      `${this.config.ordenesUrl}/api/ordenes/${id}/reabrir`,
      { motivo }
    );
  }
}

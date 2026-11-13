import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

interface Configuracion {
  ordenesUrl: string;
  agendaUrl: string;
  auditoriaUrl: string;
}

@Injectable({ providedIn: 'root' })
export class ConfiguracionService {
  private config: Configuracion | undefined;
  private http = inject(HttpClient);

  async cargar(): Promise<void> {
    this.config = await firstValueFrom(this.http.get<Configuracion>('/config.json'));
  }

  get ordenesUrl(): string {
    return this.config?.ordenesUrl ?? '';
  }

  get agendaUrl(): string {
    return this.config?.agendaUrl ?? '';
  }

  get auditoriaUrl(): string {
    return this.config?.auditoriaUrl ?? '';
  }
}

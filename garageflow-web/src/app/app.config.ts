import { ApplicationConfig, provideZoneChangeDetection, provideAppInitializer, inject } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { usuarioInterceptor } from './core/usuario/usuario.interceptor';
import { ConfiguracionService } from './core/config/configuracion.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withFetch(), withInterceptors([usuarioInterceptor])),
    ConfiguracionService,
    provideAppInitializer(() => inject(ConfiguracionService).cargar()),
  ],
};

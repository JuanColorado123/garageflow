import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { UsuarioService } from './usuario.service';

export const auditoriaGuard: CanMatchFn = () =>
  inject(UsuarioService).veAuditoria() ? true : inject(Router).createUrlTree(['/ordenes']);

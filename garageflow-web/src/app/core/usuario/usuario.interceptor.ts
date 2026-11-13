import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { UsuarioService } from './usuario.service';

export const usuarioInterceptor: HttpInterceptorFn = (req, next) => {
  const usuario = inject(UsuarioService);
  return next(req.clone({ setHeaders: { 'X-Usuario': usuario.usuario(), 'X-Rol': usuario.rol() } }));
};

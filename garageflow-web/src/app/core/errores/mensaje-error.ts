import { HttpErrorResponse } from '@angular/common/http';

export function mensajeDeError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'No hay conexión con el servidor';
    }
    if (error.error && typeof error.error === 'object') {
      const body = error.error as any;
      if (body.detail) {
        const codigo = body.codigo ? ` (${body.codigo})` : '';
        return body.detail + codigo;
      }
    }
  }
  return 'Ocurrió un error inesperado';
}

export function erroresDeCampos(error: unknown): Record<string, string> {
  if (error instanceof HttpErrorResponse && error.error && typeof error.error === 'object') {
    const body = error.error as any;
    return body.errores || {};
  }
  return {};
}

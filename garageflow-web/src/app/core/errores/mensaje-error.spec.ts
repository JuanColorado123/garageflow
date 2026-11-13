import { HttpErrorResponse } from '@angular/common/http';
import { mensajeDeError, erroresDeCampos } from './mensaje-error';

describe('mensajeDeError', () => {
  it('06 REQ-2.4 muestra detail y codigo del problem details', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: {
        detail: 'El asesor no tiene cupo en esa franja',
        codigo: 'SIN_CUPO',
      },
    });

    const mensaje = mensajeDeError(error);
    expect(mensaje).toBe('El asesor no tiene cupo en esa franja (SIN_CUPO)');
  });

  it('muestra No hay conexión con el servidor para status 0', () => {
    const error = new HttpErrorResponse({ status: 0 });

    const mensaje = mensajeDeError(error);
    expect(mensaje).toBe('No hay conexión con el servidor');
  });

  it('devuelve detail sin codigo cuando no lo hay', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: { detail: 'Datos inválidos' },
    });

    const mensaje = mensajeDeError(error);
    expect(mensaje).toBe('Datos inválidos');
  });

  it('devuelve mensaje genérico cuando no hay error details', () => {
    const error = new HttpErrorResponse({ status: 500 });

    const mensaje = mensajeDeError(error);
    expect(mensaje).toBe('Ocurrió un error inesperado');
  });
});

describe('erroresDeCampos', () => {
  it('extrae errores de campos del problem details', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: {
        errores: {
          trabajo: 'El trabajo es obligatorio',
          clienteNombre: 'El nombre es requerido',
        },
      },
    });

    const errores = erroresDeCampos(error);
    expect(errores.trabajo).toBe('El trabajo es obligatorio');
    expect(errores.clienteNombre).toBe('El nombre es requerido');
  });

  it('devuelve objeto vacío si no hay errores de campos', () => {
    const error = new HttpErrorResponse({ status: 400, error: {} });

    const errores = erroresDeCampos(error);
    expect(errores).toEqual({});
  });
});

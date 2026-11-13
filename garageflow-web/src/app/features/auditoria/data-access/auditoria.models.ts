export interface CambioDeEstado {
  eventoId: string;
  ordenId: string;
  estadoAnterior: string | null;
  estadoNuevo: string;
  motivo: string | null;
  usuario: string;
  origen: string;
  ocurridoEn: string;
}

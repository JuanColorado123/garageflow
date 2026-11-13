export type EstadoOrden = 'REGISTRADA' | 'EN_DIAGNOSTICO' | 'EN_REPARACION' | 'REPARADA' | 'CERRADA';
export type AccionOrden = 'AVANZAR' | 'REABRIR';

export interface Orden {
  id: string;
  clienteNombre: string;
  clienteTelefono: string;
  vehiculoPlaca: string;
  vehiculoDescripcion: string | null;
  aseguradora: string | null;
  trabajo: string;
  origen: 'ASESOR' | 'ASEGURADORA';
  estado: EstadoOrden;
  siguienteEstado: EstadoOrden | null;
  accionesPermitidas: AccionOrden[];
  creadaEn: string;
  actualizadaEn: string;
}

export interface RegistrarOrden {
  clienteNombre: string;
  clienteTelefono: string;
  vehiculoPlaca: string;
  vehiculoDescripcion: string;
  aseguradora: string;
  trabajo: string;
}

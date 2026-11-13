export type TipoCita = 'TRAER' | 'RECOGER';
export type EstadoCita = 'PENDIENTE' | 'CANCELADA';

export interface Cita {
  id: string;
  ordenId: string;
  asesorId: number;
  franjaInicio: string;
  tipo: TipoCita;
  estado: EstadoCita;
  motivoCancelacion: 'MANUAL' | 'CIERRE_ORDEN' | null;
  clienteNombre: string;
  clienteTelefono: string;
  creadaEn: string;
  accionesPermitidas: string[];
}

export interface Asesor {
  id: number;
  nombre: string;
  cupoPorFranja: number;
}

export interface Disponibilidad {
  asesorId: number;
  asesorNombre: string;
  franjaInicio: string;
  cupo: number;
  ocupadas: number;
  libres: number;
}

export interface AgendarCita {
  ordenId: string;
  asesorId: number;
  franjaInicio: string;
  tipo: TipoCita;
}

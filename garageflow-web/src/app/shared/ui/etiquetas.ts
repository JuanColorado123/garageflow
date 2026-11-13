export interface Etiqueta {
  texto: string;
  clases: string;
  punto: string;
}

/** Las 5 etapas de la orden en el orden en que se dibujan. Solo presentacion: las acciones salen de accionesPermitidas. */
export const FLUJO_ORDEN = ['REGISTRADA', 'EN_DIAGNOSTICO', 'EN_REPARACION', 'REPARADA', 'CERRADA'] as const;

const NEUTRA: Etiqueta = { texto: '-', clases: 'bg-slate-100 text-slate-700 ring-slate-500/20', punto: 'bg-slate-400' };

const ESTADOS_ORDEN: Record<string, Etiqueta> = {
  REGISTRADA: { texto: 'Registrada', clases: 'bg-slate-100 text-slate-700 ring-slate-500/20', punto: 'bg-slate-400' },
  EN_DIAGNOSTICO: { texto: 'En diagnóstico', clases: 'bg-sky-50 text-sky-700 ring-sky-600/20', punto: 'bg-sky-500' },
  EN_REPARACION: { texto: 'En reparación', clases: 'bg-amber-50 text-amber-800 ring-amber-600/20', punto: 'bg-amber-500' },
  REPARADA: { texto: 'Reparada', clases: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20', punto: 'bg-emerald-500' },
  CERRADA: { texto: 'Cerrada', clases: 'bg-violet-50 text-violet-700 ring-violet-600/20', punto: 'bg-violet-500' },
};

const ESTADOS_CITA: Record<string, Etiqueta> = {
  PENDIENTE: { texto: 'Pendiente', clases: 'bg-amber-50 text-amber-800 ring-amber-600/20', punto: 'bg-amber-500' },
  CANCELADA: { texto: 'Cancelada', clases: 'bg-slate-100 text-slate-600 ring-slate-500/20', punto: 'bg-slate-400' },
};

const TIPOS_CITA: Record<string, string> = { TRAER: 'Traer el vehículo', RECOGER: 'Recoger el vehículo' };
const MOTIVOS_CANCELACION: Record<string, string> = { MANUAL: 'Cancelada por el asesor', CIERRE_ORDEN: 'Cierre de la orden' };
const ORIGENES: Record<string, string> = { ASESOR: 'Asesor', ASEGURADORA: 'Aseguradora' };

export function estadoOrden(valor: string | null | undefined): Etiqueta {
  if (!valor) {
    return NEUTRA;
  }
  return ESTADOS_ORDEN[valor] ?? { ...NEUTRA, texto: valor };
}

export function estadoCita(valor: string | null | undefined): Etiqueta {
  if (!valor) {
    return NEUTRA;
  }
  return ESTADOS_CITA[valor] ?? { ...NEUTRA, texto: valor };
}

export function tipoCita(valor: string | null | undefined): string {
  return valor ? (TIPOS_CITA[valor] ?? valor) : '-';
}

export function motivoCancelacion(valor: string | null | undefined): string {
  return valor ? (MOTIVOS_CANCELACION[valor] ?? valor) : '-';
}

export function origen(valor: string | null | undefined): string {
  return valor ? (ORIGENES[valor] ?? valor) : '-';
}

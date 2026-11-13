import { Injectable, signal, computed } from '@angular/core';

export type Rol = 'ASESOR' | 'AUDITOR' | 'ADMIN';

export interface UsuarioDemo {
  usuario: string;
  rol: Rol;
}

const USUARIOS: readonly UsuarioDemo[] = [
  { usuario: 'asesor.ana', rol: 'ASESOR' },
  { usuario: 'asesor.luis', rol: 'ASESOR' },
  { usuario: 'auditor.marta', rol: 'AUDITOR' },
  { usuario: 'admin.sofia', rol: 'ADMIN' },
] as const;

const ETIQUETAS_ROL: Record<Rol, string> = {
  ASESOR: 'Asesor',
  AUDITOR: 'Auditor',
  ADMIN: 'Admin',
};

const CLAVE = 'garageflow.usuario';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private _usuario = signal<string>(this.obtenerUsuarioInicial());

  private obtenerUsuarioInicial(): string {
    try {
      const guardado = sessionStorage.getItem(CLAVE);
      if (guardado && USUARIOS.some((u) => u.usuario === guardado)) {
        return guardado;
      }
    } catch {
      // sessionStorage no disponible; usar valor por defecto
    }
    return 'asesor.ana';
  }

  get usuarios(): readonly UsuarioDemo[] {
    return USUARIOS;
  }

  usuario = computed(() => this._usuario());

  rol = computed(() => {
    const u = this._usuario();
    const found = USUARIOS.find((usr) => usr.usuario === u);
    return found?.rol ?? 'ASESOR';
  });

  etiquetaRol = computed(() => ETIQUETAS_ROL[this.rol()]);

  puedeModificar = computed(() => {
    const rol = this.rol();
    return rol === 'ASESOR' || rol === 'ADMIN';
  });

  veAuditoria = computed(() => {
    const rol = this.rol();
    return rol === 'AUDITOR' || rol === 'ADMIN';
  });

  elegir(u: string) {
    if (USUARIOS.some((usr) => usr.usuario === u)) {
      this._usuario.set(u);
      try {
        sessionStorage.setItem(CLAVE, u);
      } catch {
        // sessionStorage no disponible
      }
    }
  }
}

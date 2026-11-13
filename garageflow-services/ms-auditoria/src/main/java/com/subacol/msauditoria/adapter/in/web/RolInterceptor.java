package com.subacol.msauditoria.adapter.in.web;

import com.subacol.msauditoria.domain.ErrorDeNegocio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que valida el rol en la cabecera X-Rol y aplica las reglas de acceso
 * para el endpoint GET /api/auditoria/historial.
 */
public class RolInterceptor implements HandlerInterceptor {
    public static final String CABECERA = "X-Rol";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Dejar pasar las peticiones preflight de CORS
        if (isPreFlightRequest(request)) {
            return true;
        }

        // Leer el rol de la cabecera
        var rolOpt = Rol.desde(request.getHeader(CABECERA));
        if (rolOpt.isEmpty()) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.PROHIBIDO,
                    "ROL_NO_PERMITIDO",
                    "Falta la cabecera X-Rol o no es ASESOR, AUDITOR ni ADMIN"
            );
        }

        Rol rol = rolOpt.get();

        // Validar que el rol tiene permiso para la petición
        String ordenId = request.getParameter("ordenId");
        if ((ordenId == null || ordenId.isBlank()) && !rol.puedeVerHistorialGeneral()) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.PROHIBIDO,
                    "ROL_NO_PERMITIDO",
                    "El rol " + rol + " solo puede ver el historial de una orden"
            );
        }

        // Guardar el rol en el atributo de la petición
        request.setAttribute("rol", rol);
        return true;
    }

    /**
     * Detecta si la petición es un preflight de CORS (OPTIONS con Origin y Access-Control-Request-Method).
     */
    private static boolean isPreFlightRequest(HttpServletRequest request) {
        return "OPTIONS".equals(request.getMethod())
                && request.getHeader("Origin") != null
                && request.getHeader("Access-Control-Request-Method") != null;
    }
}

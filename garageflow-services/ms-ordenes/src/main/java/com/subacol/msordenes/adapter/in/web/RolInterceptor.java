package com.subacol.msordenes.adapter.in.web;

import com.subacol.msordenes.domain.ErrorDeNegocio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

public class RolInterceptor implements HandlerInterceptor {
    public static final String CABECERA = "X-Rol";
    public static final String ATRIBUTO = "rol";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Dejar pasar el preflight de CORS sin revisar el rol
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        // Leer el rol de la cabecera
        Rol rol = Rol.desde(request.getHeader(CABECERA))
            .orElseThrow(() -> new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.PROHIBIDO,
                "ROL_NO_PERMITIDO",
                "Falta la cabecera X-Rol o no es ASESOR, AUDITOR ni ADMIN"
            ));

        // Comprobar si el rol puede realizar la operación
        String metodo = request.getMethod();
        if (!metodo.equalsIgnoreCase("GET") && !metodo.equalsIgnoreCase("HEAD")) {
            if (!rol.puedeModificar()) {
                throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.PROHIBIDO,
                    "ROL_NO_PERMITIDO",
                    "El rol " + rol + " solo puede consultar"
                );
            }
        }

        // Guardar el rol en el atributo de petición
        request.setAttribute(ATRIBUTO, rol);
        return true;
    }
}

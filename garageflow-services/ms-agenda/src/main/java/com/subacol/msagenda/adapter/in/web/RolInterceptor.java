package com.subacol.msagenda.adapter.in.web;

import com.subacol.msagenda.domain.ErrorDeNegocio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

public class RolInterceptor implements HandlerInterceptor {
    public static final String CABECERA = "X-Rol";
    public static final String ATRIBUTO = "rol";

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        Optional<Rol> rolOpt = Rol.desde(request.getHeader(CABECERA));
        if (rolOpt.isEmpty()) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.PROHIBIDO,
                    "ROL_NO_PERMITIDO",
                    "Falta la cabecera X-Rol o no es ASESOR, AUDITOR ni ADMIN");
        }

        Rol rol = rolOpt.get();
        String method = request.getMethod();
        if (!method.equals("GET") && !method.equals("HEAD") && !rol.puedeModificar()) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.PROHIBIDO,
                    "ROL_NO_PERMITIDO",
                    "El rol " + rol + " solo puede consultar");
        }

        request.setAttribute(ATRIBUTO, rol);
        return true;
    }
}

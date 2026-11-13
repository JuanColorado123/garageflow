package com.subacol.msauditoria.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.subacol.msauditoria.domain.ErrorDeNegocio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RolInterceptorTest {
    private final RolInterceptor interceptor = new RolInterceptor();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    @DisplayName("07 REQ-1.3 sin X-Rol o con un rol desconocido responde ROL_NO_PERMITIDO")
    void sinXRolOConRolDesconocido() {
        // Sin X-Rol
        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/auditoria/historial");
        request1.addParameter("ordenId", "o-1");

        assertThatThrownBy(() -> interceptor.preHandle(request1, response, null))
                .isInstanceOf(ErrorDeNegocio.class)
                .hasMessageContaining("Falta la cabecera X-Rol")
                .satisfies(e -> {
                    ErrorDeNegocio error = (ErrorDeNegocio) e;
                    assertThat(error.codigo()).isEqualTo("ROL_NO_PERMITIDO");
                    assertThat(error.tipo()).isEqualTo(ErrorDeNegocio.Tipo.PROHIBIDO);
                });

        // Con rol desconocido
        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/auditoria/historial");
        request2.addParameter("ordenId", "o-1");
        request2.addHeader(RolInterceptor.CABECERA, "JEFE");

        assertThatThrownBy(() -> interceptor.preHandle(request2, response, null))
                .isInstanceOf(ErrorDeNegocio.class)
                .hasMessageContaining("Falta la cabecera X-Rol")
                .satisfies(e -> {
                    ErrorDeNegocio error = (ErrorDeNegocio) e;
                    assertThat(error.codigo()).isEqualTo("ROL_NO_PERMITIDO");
                    assertThat(error.tipo()).isEqualTo(ErrorDeNegocio.Tipo.PROHIBIDO);
                });
    }

    @Test
    @DisplayName("07 REQ-1.3 el preflight de CORS pasa sin X-Rol")
    void preflightDeCorsPasaSinXRol() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auditoria/historial");
        request.addHeader("Origin", "http://localhost:4200");
        request.addHeader("Access-Control-Request-Method", "GET");

        // El interceptor debe retornar true (permitir) sin lanzar excepción
        boolean result = interceptor.preHandle(request, response, null);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("07 REQ-2.2 el ASESOR ve el historial de una orden")
    void asesorVeHistorialDeUnaOrden() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auditoria/historial");
        request.addParameter("ordenId", "o-1");
        request.addHeader(RolInterceptor.CABECERA, "ASESOR");

        boolean result = interceptor.preHandle(request, response, null);

        assertThat(result).isTrue();
        assertThat(request.getAttribute("rol")).isEqualTo(Rol.ASESOR);
    }

    @Test
    @DisplayName("07 REQ-2.3 el ASESOR no ve el historial general")
    void asesorNoVeHistorialGeneral() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auditoria/historial");
        // Sin ordenId (null)
        request.addHeader(RolInterceptor.CABECERA, "ASESOR");

        assertThatThrownBy(() -> interceptor.preHandle(request, response, null))
                .isInstanceOf(ErrorDeNegocio.class)
                .hasMessageContaining("solo puede ver el historial de una orden")
                .satisfies(e -> {
                    ErrorDeNegocio error = (ErrorDeNegocio) e;
                    assertThat(error.codigo()).isEqualTo("ROL_NO_PERMITIDO");
                    assertThat(error.tipo()).isEqualTo(ErrorDeNegocio.Tipo.PROHIBIDO);
                });
    }

    @Test
    @DisplayName("07 REQ-3.1 el AUDITOR puede consultar")
    void auditorPuedeConsultar() throws Exception {
        // Con ordenId
        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/auditoria/historial");
        request1.addParameter("ordenId", "o-1");
        request1.addHeader(RolInterceptor.CABECERA, "AUDITOR");

        boolean result1 = interceptor.preHandle(request1, response, null);

        assertThat(result1).isTrue();
        assertThat(request1.getAttribute("rol")).isEqualTo(Rol.AUDITOR);

        // Sin ordenId (historial general)
        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/auditoria/historial");
        request2.addHeader(RolInterceptor.CABECERA, "AUDITOR");

        boolean result2 = interceptor.preHandle(request2, response, null);

        assertThat(result2).isTrue();
        assertThat(request2.getAttribute("rol")).isEqualTo(Rol.AUDITOR);
    }

    @Test
    @DisplayName("07 REQ-4.1 el ADMIN puede todo")
    void adminPuedeTodo() throws Exception {
        // Con ordenId
        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/auditoria/historial");
        request1.addParameter("ordenId", "o-1");
        request1.addHeader(RolInterceptor.CABECERA, "ADMIN");

        boolean result1 = interceptor.preHandle(request1, response, null);

        assertThat(result1).isTrue();
        assertThat(request1.getAttribute("rol")).isEqualTo(Rol.ADMIN);

        // Sin ordenId (historial general)
        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/auditoria/historial");
        request2.addHeader(RolInterceptor.CABECERA, "ADMIN");

        boolean result2 = interceptor.preHandle(request2, response, null);

        assertThat(result2).isTrue();
        assertThat(request2.getAttribute("rol")).isEqualTo(Rol.ADMIN);
    }
}

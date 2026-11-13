package com.subacol.msagenda.adapter.in.web;

import static org.assertj.core.api.Assertions.*;

import com.subacol.msagenda.domain.ErrorDeNegocio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RolInterceptorTest {
    private final RolInterceptor interceptor = new RolInterceptor();

    @Test
    @DisplayName("07 REQ-1.3 sin X-Rol o con un rol desconocido responde ROL_NO_PERMITIDO")
    void testSinRol() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/agenda/citas");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> interceptor.preHandle(request, response, null))
                .isInstanceOf(ErrorDeNegocio.class)
                .extracting("codigo")
                .isEqualTo("ROL_NO_PERMITIDO");
    }

    @Test
    @DisplayName("07 REQ-1.3 sin X-Rol o con un rol desconocido responde ROL_NO_PERMITIDO")
    void testRolDesconocido() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/agenda/citas");
        request.addHeader("X-Rol", "JEFE");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> interceptor.preHandle(request, response, null))
                .isInstanceOf(ErrorDeNegocio.class)
                .extracting("codigo")
                .isEqualTo("ROL_NO_PERMITIDO");
    }

    @Test
    @DisplayName("07 REQ-1.3 el preflight de CORS pasa sin X-Rol")
    void testPreflightCORS() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/agenda");
        request.addHeader("Origin", "http://localhost:4200");
        request.addHeader("Access-Control-Request-Method", "POST");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean resultado = interceptor.preHandle(request, response, null);

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("07 REQ-2.1 el ASESOR puede consultar y modificar")
    void testAsesorPuedeModificar() throws Exception {
        MockHttpServletRequest getRequest =
                new MockHttpServletRequest("GET", "/api/agenda/citas");
        getRequest.addHeader("X-Rol", "ASESOR");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean resultado = interceptor.preHandle(getRequest, response, null);

        assertThat(resultado).isTrue();
        assertThat(getRequest.getAttribute("rol")).isEqualTo(Rol.ASESOR);

        MockHttpServletRequest postRequest =
                new MockHttpServletRequest("POST", "/api/agenda/citas");
        postRequest.addHeader("X-Rol", "ASESOR");
        response = new MockHttpServletResponse();

        resultado = interceptor.preHandle(postRequest, response, null);

        assertThat(resultado).isTrue();
        assertThat(postRequest.getAttribute("rol")).isEqualTo(Rol.ASESOR);
    }

    @Test
    @DisplayName("07 REQ-3.1 el AUDITOR puede consultar")
    void testAuditorPuedeConsultar() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/agenda/citas");
        request.addHeader("X-Rol", "AUDITOR");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean resultado = interceptor.preHandle(request, response, null);

        assertThat(resultado).isTrue();
        assertThat(request.getAttribute("rol")).isEqualTo(Rol.AUDITOR);
    }

    @Test
    @DisplayName("07 REQ-3.2 el AUDITOR no puede modificar")
    void testAuditorNoPuedeModificar() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/api/agenda/citas");
        request.addHeader("X-Rol", "AUDITOR");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> interceptor.preHandle(request, response, null))
                .isInstanceOf(ErrorDeNegocio.class)
                .extracting("codigo")
                .isEqualTo("ROL_NO_PERMITIDO");
    }

    @Test
    @DisplayName("07 REQ-4.1 el ADMIN puede todo")
    void testAdminPuedeTodo() throws Exception {
        MockHttpServletRequest getRequest =
                new MockHttpServletRequest("GET", "/api/agenda/citas");
        getRequest.addHeader("X-Rol", "ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean resultado = interceptor.preHandle(getRequest, response, null);

        assertThat(resultado).isTrue();
        assertThat(getRequest.getAttribute("rol")).isEqualTo(Rol.ADMIN);

        MockHttpServletRequest postRequest =
                new MockHttpServletRequest("POST", "/api/agenda/citas");
        postRequest.addHeader("X-Rol", "ADMIN");
        response = new MockHttpServletResponse();

        resultado = interceptor.preHandle(postRequest, response, null);

        assertThat(resultado).isTrue();
        assertThat(postRequest.getAttribute("rol")).isEqualTo(Rol.ADMIN);
    }
}

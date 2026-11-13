package com.subacol.msordenes.adapter.in.web;

import com.subacol.msordenes.domain.ErrorDeNegocio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.CorsUtils;

import static org.assertj.core.api.Assertions.*;

class RolInterceptorTest {
    private final RolInterceptor interceptor = new RolInterceptor();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    @DisplayName("07 REQ-1.3 sin X-Rol o con un rol desconocido responde ROL_NO_PERMITIDO")
    void sinXRolOConRolDesconocidoRespondeRolNOPermitido() {
        // Caso 1: sin cabecera X-Rol
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ordenes");
        
        assertThatThrownBy(() -> interceptor.preHandle(request, response, null))
            .isInstanceOf(ErrorDeNegocio.class)
            .extracting(e -> ((ErrorDeNegocio)e).codigo())
            .isEqualTo("ROL_NO_PERMITIDO");

        // Caso 2: con rol desconocido
        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/ordenes");
        request2.addHeader("X-Rol", "JEFE");
        
        assertThatThrownBy(() -> interceptor.preHandle(request2, response, null))
            .isInstanceOf(ErrorDeNegocio.class)
            .extracting(e -> ((ErrorDeNegocio)e).codigo())
            .isEqualTo("ROL_NO_PERMITIDO");
    }

    @Test
    @DisplayName("07 REQ-1.3 el preflight de CORS pasa sin X-Rol")
    void elPreflightDeCorsPasaSinXRol() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/ordenes");
        request.addHeader("Origin", "http://localhost:4200");
        request.addHeader("Access-Control-Request-Method", "POST");

        boolean resultado = interceptor.preHandle(request, response, null);

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("07 REQ-2.1 el ASESOR puede consultar y modificar")
    void elAsesorPuedeConsultarYModificar() throws Exception {
        // GET
        MockHttpServletRequest getRequest = new MockHttpServletRequest("GET", "/api/ordenes");
        getRequest.addHeader("X-Rol", "ASESOR");
        
        boolean resultadoGet = interceptor.preHandle(getRequest, response, null);
        
        assertThat(resultadoGet).isTrue();
        assertThat(getRequest.getAttribute(RolInterceptor.ATRIBUTO)).isEqualTo(Rol.ASESOR);

        // POST
        MockHttpServletRequest postRequest = new MockHttpServletRequest("POST", "/api/ordenes");
        postRequest.addHeader("X-Rol", "ASESOR");
        
        boolean resultadoPost = interceptor.preHandle(postRequest, response, null);
        
        assertThat(resultadoPost).isTrue();
        assertThat(postRequest.getAttribute(RolInterceptor.ATRIBUTO)).isEqualTo(Rol.ASESOR);
    }

    @Test
    @DisplayName("07 REQ-3.1 el AUDITOR puede consultar")
    void elAuditorPuedeConsultar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ordenes");
        request.addHeader("X-Rol", "AUDITOR");
        
        boolean resultado = interceptor.preHandle(request, response, null);
        
        assertThat(resultado).isTrue();
        assertThat(request.getAttribute(RolInterceptor.ATRIBUTO)).isEqualTo(Rol.AUDITOR);
    }

    @Test
    @DisplayName("07 REQ-3.2 el AUDITOR no puede modificar")
    void elAuditorNoPuedeModificar() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/ordenes");
        request.addHeader("X-Rol", "AUDITOR");
        
        assertThatThrownBy(() -> interceptor.preHandle(request, response, null))
            .isInstanceOf(ErrorDeNegocio.class)
            .extracting(e -> ((ErrorDeNegocio)e).codigo())
            .isEqualTo("ROL_NO_PERMITIDO");
    }

    @Test
    @DisplayName("07 REQ-4.1 el ADMIN puede todo")
    void elAdminPuedeTodo() throws Exception {
        // GET
        MockHttpServletRequest getRequest = new MockHttpServletRequest("GET", "/api/ordenes");
        getRequest.addHeader("X-Rol", "ADMIN");
        
        boolean resultadoGet = interceptor.preHandle(getRequest, response, null);
        
        assertThat(resultadoGet).isTrue();
        assertThat(getRequest.getAttribute(RolInterceptor.ATRIBUTO)).isEqualTo(Rol.ADMIN);

        // POST
        MockHttpServletRequest postRequest = new MockHttpServletRequest("POST", "/api/ordenes");
        postRequest.addHeader("X-Rol", "ADMIN");
        
        boolean resultadoPost = interceptor.preHandle(postRequest, response, null);
        
        assertThat(resultadoPost).isTrue();
        assertThat(postRequest.getAttribute(RolInterceptor.ATRIBUTO)).isEqualTo(Rol.ADMIN);
    }
}

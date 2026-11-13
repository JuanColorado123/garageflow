package com.subacol.msnotificaciones.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import com.subacol.msnotificaciones.application.port.out.AvisoRepository;
import com.subacol.msnotificaciones.application.port.out.Notificador;
import com.subacol.msnotificaciones.application.service.AvisosService;
import com.subacol.msnotificaciones.domain.Aviso;
import com.subacol.msnotificaciones.domain.DatosCita;
import com.subacol.msnotificaciones.domain.EstadoAviso;
import com.subacol.msnotificaciones.domain.MotivoCancelacion;
import com.subacol.msnotificaciones.domain.TipoCita;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("AvisosService application tests")
class AvisosServiceTest {

    private AvisoRepository avisoRepository;
    private Notificador notificador;
    private AvisosService avisosService;

    private static final String EVENTO_ID_1 = "evento-123";
    private static final String EVENTO_ID_2 = "evento-456";
    private static final String CITA_ID = "cita-789";
    private static final String ORDEN_ID = "orden-012";

    @BeforeEach
    void setUp() {
        avisoRepository = mock(AvisoRepository.class);
        notificador = mock(Notificador.class);
        avisosService = new AvisosService(avisoRepository, notificador);
    }

    @Test
    @DisplayName("04 REQ-1.1 una cita agendada se envía al teléfono del cliente")
    void testCitaAgendadaSeEnviaAlTelefono() {
        LocalDateTime franjaInicio = LocalDateTime.of(2026, 11, 17, 10, 0);
        DatosCita cita = new DatosCita(CITA_ID, ORDEN_ID, TipoCita.TRAER, franjaInicio, "Juan García", "3001234567");
        when(avisoRepository.registrarSiEsNuevo(any())).thenReturn(true);

        avisosService.citaAgendada(EVENTO_ID_1, cita);

        ArgumentCaptor<Aviso> captor = ArgumentCaptor.forClass(Aviso.class);
        verify(notificador).enviar(captor.capture());
        Aviso avisoEnviado = captor.getValue();
        
        assertThat(avisoEnviado.citaId()).isEqualTo(CITA_ID);
        assertThat(avisoEnviado.telefono()).isEqualTo("3001234567");
        assertThat(avisoEnviado.mensaje()).contains("Juan García");
    }

    @Test
    @DisplayName("04 REQ-1.3 un evento repetido avisa una sola vez")
    void testEventoRepetidoAvísaUnaSolaVez() {
        LocalDateTime franjaInicio = LocalDateTime.of(2026, 11, 17, 10, 0);
        DatosCita cita = new DatosCita(CITA_ID, ORDEN_ID, TipoCita.TRAER, franjaInicio, "Juan García", "3001234567");
        
        // Primer evento
        when(avisoRepository.registrarSiEsNuevo(any())).thenReturn(true);
        avisosService.citaAgendada(EVENTO_ID_1, cita);
        verify(notificador, times(1)).enviar(any());

        // Evento repetido
        when(avisoRepository.registrarSiEsNuevo(any())).thenReturn(false);
        avisosService.citaAgendada(EVENTO_ID_1, cita);
        
        verify(notificador, times(1)).enviar(any());
    }

    @Test
    @DisplayName("04 REQ-1.4 el aviso enviado queda registrado como ENVIADO")
    void testAvisoRegistradoComoEnviado() {
        LocalDateTime franjaInicio = LocalDateTime.of(2026, 11, 17, 10, 0);
        DatosCita cita = new DatosCita(CITA_ID, ORDEN_ID, TipoCita.TRAER, franjaInicio, "Juan García", "3001234567");
        when(avisoRepository.registrarSiEsNuevo(any())).thenReturn(true);

        avisosService.citaAgendada(EVENTO_ID_1, cita);

        ArgumentCaptor<Aviso> captor = ArgumentCaptor.forClass(Aviso.class);
        verify(avisoRepository).registrarSiEsNuevo(captor.capture());
        Aviso avisoRegistrado = captor.getValue();
        
        assertThat(avisoRegistrado.estado()).isEqualTo(EstadoAviso.ENVIADO);
    }

    @Test
    @DisplayName("05 REQ-2.1 una cancelación por CIERRE_ORDEN no llama al notificador")
    void testCierreOrdenNoLlamaNotificador() {
        LocalDateTime franjaInicio = LocalDateTime.of(2026, 11, 21, 11, 0);
        DatosCita cita = new DatosCita(CITA_ID, ORDEN_ID, TipoCita.TRAER, franjaInicio, "Carlos Ruiz", "3157894561");
        when(avisoRepository.registrarSiEsNuevo(any())).thenReturn(true);

        avisosService.citaCancelada(EVENTO_ID_2, cita, MotivoCancelacion.CIERRE_ORDEN);

        verify(notificador, never()).enviar(any());
    }

    @Test
    @DisplayName("05 REQ-2.2 el aviso omitido queda registrado como OMITIDO")
    void testAvisoOmitidoRegistradoComoOmitido() {
        LocalDateTime franjaInicio = LocalDateTime.of(2026, 11, 21, 11, 0);
        DatosCita cita = new DatosCita(CITA_ID, ORDEN_ID, TipoCita.TRAER, franjaInicio, "Carlos Ruiz", "3157894561");
        when(avisoRepository.registrarSiEsNuevo(any())).thenReturn(true);

        avisosService.citaCancelada(EVENTO_ID_2, cita, MotivoCancelacion.CIERRE_ORDEN);

        ArgumentCaptor<Aviso> captor = ArgumentCaptor.forClass(Aviso.class);
        verify(avisoRepository).registrarSiEsNuevo(captor.capture());
        Aviso avisoRegistrado = captor.getValue();
        
        assertThat(avisoRegistrado.estado()).isEqualTo(EstadoAviso.OMITIDO);
    }
}

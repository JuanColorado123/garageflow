package com.subacol.msagenda.adapter.out.http;

import com.subacol.msagenda.application.port.out.OrdenInfo;
import com.subacol.msagenda.application.port.out.OrdenesConsulta;
import com.subacol.msagenda.domain.ErrorDeNegocio;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class OrdenesRestClient implements OrdenesConsulta {
    private final RestClient restClient;

    public OrdenesRestClient(@Value("${app.ordenes.url}") String url) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(2));
        this.restClient =
                RestClient.builder()
                        .baseUrl(url)
                        .requestFactory(factory)
                        .requestInterceptor(
                                (request, body, execution) -> {
                                    ServletRequestAttributes attrs =
                                            (ServletRequestAttributes)
                                                    RequestContextHolder.getRequestAttributes();
                                    if (attrs != null) {
                                        var httpRequest = attrs.getRequest();
                                        if (httpRequest != null) {
                                            String xRol = httpRequest.getHeader("X-Rol");
                                            if (xRol != null) {
                                                request.getHeaders().set("X-Rol", xRol);
                                            }
                                            String xUsuario =
                                                    httpRequest.getHeader("X-Usuario");
                                            if (xUsuario != null) {
                                                request.getHeaders().set("X-Usuario", xUsuario);
                                            }
                                        }
                                    }
                                    return execution.execute(request, body);
                                })
                        .build();
    }

    @Override
    public Optional<OrdenInfo> obtener(String ordenId) {
        try {
            OrdenRemotaV1 orden =
                    restClient
                            .get()
                            .uri("/api/ordenes/{id}", ordenId)
                            .retrieve()
                            .body(OrdenRemotaV1.class);
            return Optional.of(
                    new OrdenInfo(orden.estado(), orden.clienteNombre(), orden.clienteTelefono()));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.NO_DISPONIBLE,
                    "ORDENES_NO_DISPONIBLE",
                    "El servicio de ordenes no responde; intente de nuevo");
        }
    }
}

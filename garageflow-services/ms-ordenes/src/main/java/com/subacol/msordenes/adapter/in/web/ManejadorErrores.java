package com.subacol.msordenes.adapter.in.web;

import com.subacol.msordenes.domain.ErrorDeNegocio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorErrores {
    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ErrorDeNegocio.class)
    public ProblemDetail errorDeNegocio(ErrorDeNegocio e) {
        HttpStatus status = switch (e.tipo()) {
            case INVALIDO -> HttpStatus.BAD_REQUEST;
            case NO_ENCONTRADO -> HttpStatus.NOT_FOUND;
            case CONFLICTO -> HttpStatus.CONFLICT;
            case NO_DISPONIBLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case PROHIBIDO -> HttpStatus.FORBIDDEN;
        };

        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        detail.setProperty("codigo", e.codigo());
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail datosInvalidos(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fe -> {
            String campo = fe.getField();
            String mensaje = fe.getDefaultMessage();
            errores.putIfAbsent(campo, mensaje);
        });

        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Hay datos invalidos en la peticion"
        );
        detail.setProperty("codigo", "DATOS_INVALIDOS");
        detail.setProperty("errores", errores);
        return detail;
    }

    @ExceptionHandler({
        org.springframework.http.converter.HttpMessageNotReadableException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class,
        HandlerMethodValidationException.class
    })
    public ProblemDetail peticionInvalida(Exception e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "La peticion no es valida"
        );
        detail.setProperty("codigo", "DATOS_INVALIDOS");
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail errorInesperado(Exception e) {
        if (e instanceof org.springframework.web.ErrorResponse er) {
            return er.getBody();
        }

        log.error("Error no controlado", e);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocurrio un error inesperado"
        );
        detail.setProperty("codigo", "ERROR_INTERNO");
        return detail;
    }
}

package com.subacol.msauditoria.adapter.in.web;

import com.subacol.msauditoria.domain.ErrorDeNegocio;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ManejadorErrores {
    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ErrorDeNegocio.class)
    public ResponseEntity<ProblemDetail> manejarErrorDeNegocio(ErrorDeNegocio ex) {
        HttpStatus status = switch (ex.tipo()) {
            case INVALIDO -> HttpStatus.BAD_REQUEST;
            case NO_ENCONTRADO -> HttpStatus.NOT_FOUND;
            case CONFLICTO -> HttpStatus.CONFLICT;
            case NO_DISPONIBLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case PROHIBIDO -> HttpStatus.FORBIDDEN;
        };
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        detail.setProperty("codigo", ex.codigo());
        return ResponseEntity.status(status).body(detail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> manejarValidacion(MethodArgumentNotValidException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Los datos no son válidos"
        );
        detail.setProperty("codigo", "DATOS_INVALIDOS");
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );
        detail.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler({
            org.springframework.http.converter.HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            HandlerMethodValidationException.class
    })
    public ResponseEntity<ProblemDetail> manejarDatosInvalidos(Exception ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Los datos no son válidos"
        );
        detail.setProperty("codigo", "DATOS_INVALIDOS");
        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> manejarGenerico(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            return ResponseEntity
                    .status(errorResponse.getStatusCode())
                    .body(errorResponse.getBody());
        }
        log.error("Error inesperado", ex);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado"
        );
        detail.setProperty("codigo", "ERROR_INTERNO");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(detail);
    }
}

package com.subacol.msagenda.adapter.in.web;

import com.subacol.msagenda.domain.ErrorDeNegocio;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class ManejadorErrores {
    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ErrorDeNegocio.class)
    public ResponseEntity<ProblemDetail> manejarErrorDeNegocio(ErrorDeNegocio e) {
        HttpStatus status =
                switch (e.tipo()) {
                    case INVALIDO -> HttpStatus.BAD_REQUEST;
                    case NO_ENCONTRADO -> HttpStatus.NOT_FOUND;
                    case CONFLICTO -> HttpStatus.CONFLICT;
                    case NO_DISPONIBLE -> HttpStatus.SERVICE_UNAVAILABLE;
                    case PROHIBIDO -> HttpStatus.FORBIDDEN;
                };

        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, e.mensaje());
        detail.setProperty("codigo", e.codigo());
        return ResponseEntity.of(detail).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> manejarValidacionArgumentos(
            MethodArgumentNotValidException e) {
        ProblemDetail detail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST, "Los datos no son válidos");
        Map<String, String> errores = new HashMap<>();
        e.getBindingResult()
                .getFieldErrors()
                .forEach(fe -> errores.put(fe.getField(), fe.getDefaultMessage()));
        detail.setProperty("codigo", "DATOS_INVALIDOS");
        detail.setProperty("errores", errores);
        return ResponseEntity.of(detail).build();
    }

    @ExceptionHandler({
        org.springframework.http.converter.HttpMessageNotReadableException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class,
        HandlerMethodValidationException.class
    })
    public ResponseEntity<ProblemDetail> manejarErroresDatos(Exception e) {
        ProblemDetail detail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST, "Los datos no son válidos");
        detail.setProperty("codigo", "DATOS_INVALIDOS");
        return ResponseEntity.of(detail).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> manejarErrorGeneral(Exception e) {
        if (e instanceof org.springframework.web.ErrorResponse) {
            org.springframework.web.ErrorResponse er = (org.springframework.web.ErrorResponse) e;
            return ResponseEntity.status(er.getStatusCode()).body(er.getBody());
        }
        log.error("Error inesperado", e);
        ProblemDetail detail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Ocurrio un error inesperado");
        detail.setProperty("codigo", "ERROR_INTERNO");
        return ResponseEntity.of(detail).build();
    }
}

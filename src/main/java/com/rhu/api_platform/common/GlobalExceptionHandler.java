package com.rhu.api_platform.common;

import com.rhu.api_platform.common.dto.RespuestaError;
import com.rhu.api_platform.common.exception.ConflictoException;
import com.rhu.api_platform.common.exception.RecursoNoEncontradoException;
import com.rhu.api_platform.common.exception.ValidacionNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarValidacion(MethodArgumentNotValidException ex) {
        List<RespuestaError.DetalleError> detalles = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new RespuestaError.DetalleError(e.getField(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(RespuestaError.builder()
                .codigo("VALIDACION")
                .mensaje("La solicitud contiene errores de validación.")
                .detalles(detalles)
                .build());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RespuestaError.builder()
                .codigo("NO_ENCONTRADO")
                .mensaje(ex.getMessage())
                .build());
    }

    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<RespuestaError> manejarNegocio(ValidacionNegocioException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(RespuestaError.builder()
                .codigo("NEGOCIO")
                .mensaje(ex.getMessage())
                .build());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<RespuestaError> manejarConflicto(ConflictoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(RespuestaError.builder()
                .codigo("CONFLICTO")
                .mensaje(ex.getMessage())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarGenerico(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RespuestaError.builder()
                .codigo("ERROR_INTERNO")
                .mensaje("Ocurrió un error interno en el servidor.")
                .build());
    }
}

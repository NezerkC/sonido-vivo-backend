package cl.duoc.sonidovivo.infrastructure.adapter.in.rest;

import cl.duoc.sonidovivo.domain.exception.CustomerNotFoundException;
import cl.duoc.sonidovivo.domain.exception.DuplicateProductCodeException;
import cl.duoc.sonidovivo.domain.exception.InsufficientStockException;
import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import cl.duoc.sonidovivo.domain.exception.InvalidPriceMarginException;
import cl.duoc.sonidovivo.domain.exception.ProductNotFoundException;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Centralized REST exception advice translating validation, security, and domain errors
 * into standardized {@link ErrorResponseDTO} payloads.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            fieldErrors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Constraint validation failed",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler({
        HandlerMethodValidationException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class,
        InvalidDomainDataException.class,
        InvalidPriceMarginException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationFailure(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid authentication credentials",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Access denied for the requested resource",
                request,
                Map.of()
        );
    }

    @ExceptionHandler({ProductNotFoundException.class, CustomerNotFoundException.class})
    public ResponseEntity<ErrorResponseDTO> handleNotFound(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler({
        DuplicateProductCodeException.class,
        InsufficientStockException.class,
        DuplicateKeyException.class,
        DataIntegrityViolationException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleConflict(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        String message = (ex instanceof DuplicateKeyException || ex instanceof DataIntegrityViolationException)
                ? "Resource conflicts with an existing unique record"
                : ex.getMessage();
        return buildResponse(HttpStatus.CONFLICT, message, request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpectedError(
            Exception ex,
            HttpServletRequest request
    ) {
        LOGGER.error("Unhandled error processing {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal error occurred",
                request,
                Map.of()
        );
    }

    private static ResponseEntity<ErrorResponseDTO> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors
    ) {
        ErrorResponseDTO body = fieldErrors.isEmpty()
                ? ErrorResponseDTO.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI())
                : ErrorResponseDTO.withFieldErrors(
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI(),
                        fieldErrors
                );
        return ResponseEntity.status(status).body(body);
    }
}

package br.com.lectek.copainsider.adapters.inbound.handler;

import br.com.lectek.copainsider.application.core.exception.InvalidCredentialsException;
import br.com.lectek.copainsider.application.core.exception.ImportInProgressException;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String VALIDATION_ERRORS = "validationErrors";
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "validation_failed", "Validation failed", request);

        Map<String, String> fields = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }

        StringBuilder global = new StringBuilder();
        for (ObjectError ge : ex.getBindingResult().getGlobalErrors()) {
            if (!global.isEmpty()) global.append("; ");
            global.append(ge.getDefaultMessage());
        }
        if (!global.isEmpty()) {
            body.put("globalError", global.toString());
        }

        body.put(VALIDATION_ERRORS, fields);
        log.debug("Validation failed: {}", fields);

        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBindException(BindException ex, HttpServletRequest req) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "binding_failed", "Binding failed", req);

        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));

        if (!ex.getBindingResult().getGlobalErrors().isEmpty()) {
            StringBuilder global = new StringBuilder();
            ex.getBindingResult().getGlobalErrors().forEach(ge -> {
                if (!global.isEmpty()) global.append("; ");
                global.append(ge.getDefaultMessage());
            });
            body.put("globalError", global.toString());
        }

        body.put(VALIDATION_ERRORS, fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "constraint_violation", "Constraint violation", req);

        Map<String, String> fields = new HashMap<>();
        ex.getConstraintViolations()
                .forEach(cv -> fields.put(cv.getPropertyPath().toString(), cv.getMessage()));
        body.put(VALIDATION_ERRORS, fields);

        return ResponseEntity.badRequest().body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "malformed_json", "Malformed JSON request", request);
        log.debug("Malformed JSON", ex);
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, Object> body = baseBody(
                HttpStatus.BAD_REQUEST,
                "missing_parameter",
                "Missing required parameter: " + ex.getParameterName(),
                request
        );
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String paramName = (ex instanceof MethodArgumentTypeMismatchException matme)
                ? matme.getName()
                : ex.getPropertyName();

        Map<String, Object> body = baseBody(
                HttpStatus.BAD_REQUEST,
                "type_mismatch",
                "Invalid value for parameter: " + (paramName != null ? paramName : "unknown"),
                request
        );
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, Object> body = baseBody(
                HttpStatus.METHOD_NOT_ALLOWED,
                "method_not_allowed",
                "Method not allowed: " + ex.getMethod(),
                request
        );
        return handleExceptionInternal(ex, body, headers, HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, Object> body = baseBody(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "unsupported_media_type",
                "Unsupported media type: " + ex.getContentType(),
                request
        );
        return handleExceptionInternal(ex, body, headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE, request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        Map<String, Object> body = baseBody(HttpStatus.UNAUTHORIZED, "invalid_credentials", ex.getMessage(), req);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        Map<String, Object> body = baseBody(HttpStatus.FORBIDDEN, "access_denied", "Acesso negado.", req);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<Object> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        Map<String, Object> body = baseBody(
                HttpStatus.NOT_FOUND,
                "not_found",
                ex.getMessage() != null ? ex.getMessage() : "Recurso não encontrado.",
                req
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "bad_request", ex.getMessage(), req);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ImportInProgressException.class)
    public ResponseEntity<Object> handleImportInProgress(ImportInProgressException ex, HttpServletRequest req) {
        Map<String, Object> body = baseBody(HttpStatus.CONFLICT, "import_in_progress", ex.getMessage(), req);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        String friendly = "Violação de integridade de dados.";
        Throwable root = ex.getMostSpecificCause();
        String msg = root.getMessage();

        if (msg != null) {
            String lower = msg.toLowerCase();
            if (lower.contains("codigo_barras") || lower.contains("codigo barras")
                    || lower.contains("unique") || lower.contains("uk_")
                    || lower.contains("duplicate") || lower.contains("uniq")) {
                friendly = "Código de barras já cadastrado para outro produto.";
            }
        }

        Map<String, Object> body = baseBody(HttpStatus.CONFLICT, "data_integrity", friendly, req);
        log.warn("Data integrity violation: {}", msg);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, Object> body = baseBody(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "payload_too_large",
                "Arquivo muito grande. Envie imagem de ate 20MB.",
                request
        );
        return handleExceptionInternal(
                ex,
                body,
                headers,
                HttpStatus.PAYLOAD_TOO_LARGE,
                request
        );
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Object> handleMultipartException(MultipartException ex, HttpServletRequest req) {
        Map<String, Object> body = baseBody(
                HttpStatus.BAD_REQUEST,
                "multipart_error",
                "Falha ao processar upload da imagem. Use JPG, PNG ou WEBP.",
                req
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(OtpServicePort.OtpException.class)
    public ResponseEntity<Object> handleOtp(OtpServicePort.OtpException ex, HttpServletRequest req) {
        Locale locale = LocaleContextHolder.getLocale();
        String key = "otp.error." + ex.reason();
        String i18n = resolveOrFallback(key, ex.getMessage(), locale);

        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "otp_error", i18n, req);
        body.put("code", ex.reason());

        return ResponseEntity.badRequest().body(body);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        Map<String, Object> std = baseBody(
                status,
                "error",
                ex.getMessage() != null ? ex.getMessage() : status.getReasonPhrase(),
                request
        );

        if (body instanceof Map<?, ?> given) {
            given.forEach((k, v) -> std.put(String.valueOf(k), v));
        } else if (body != null) {
            std.put("detail", body);
        }

        if (status.is5xxServerError()) {
            log.error("handleExceptionInternal (5xx)", ex);
        } else {
            log.debug("handleExceptionInternal (4xx): {}", ex.getMessage());
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(headers);

        // MVC requests may arrive with text/html preset; force JSON for map error bodies.
        MediaType contentType = responseHeaders.getContentType();
        if (contentType != null && contentType.isCompatibleWith(MediaType.TEXT_HTML)) {
            responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        }

        return new ResponseEntity<>(std, responseHeaders, status);
    }

    // ===== Helpers =====

    private Map<String, Object> baseBody(HttpStatus status, String code, String message, WebRequest request) {
        String path = null;
        if (request instanceof ServletWebRequest swr) {
            path = swr.getRequest().getRequestURI();
        }
        return baseBody(status, code, message, path);
    }

    private Map<String, Object> baseBody(HttpStatus status, String code, String message, @Nullable HttpServletRequest req) {
        String path = (req != null) ? req.getRequestURI() : null;
        return baseBody(status, code, message, path);
    }

    private Map<String, Object> baseBody(HttpStatus status, String code, String message, @Nullable String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("code", code);
        body.put("message", message);
        if (path != null) {
            body.put("path", path);
        }
        return body;
    }

    private String resolveOrFallback(String key, @Nullable String fallback, Locale locale) {
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (Exception e) {
            return (fallback != null) ? fallback : key;
        }
    }
}

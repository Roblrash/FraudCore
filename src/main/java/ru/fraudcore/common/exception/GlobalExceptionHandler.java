package ru.fraudcore.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
@SuppressWarnings("unused")
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ApiErrorDetail> details = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Ошибка валидации", request.getRequestURI(), details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintValidation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<ApiErrorDetail> details = exception.getConstraintViolations().stream()
                .map(cv -> ApiErrorDetail.builder().field(cv.getPropertyPath().toString()).message(cv.getMessage()).build())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Ошибка валидации", request.getRequestURI(), details);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler({ConflictException.class, org.springframework.orm.ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(Exception exception, HttpServletRequest request) {
        String message = exception instanceof org.springframework.orm.ObjectOptimisticLockingFailureException
                ? "Конфликт версий при обновлении данных. Повторите запрос."
                : exception.getMessage();
        return build(HttpStatus.CONFLICT, message, request.getRequestURI(), List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Конфликт данных или нарушение уникальности", request.getRequestURI(), List.of());
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ApiErrorResponse> handleForbidden(Exception exception, HttpServletRequest request) {
        String message = exception instanceof ForbiddenException ? exception.getMessage() : "Доступ запрещен";
        return build(HttpStatus.FORBIDDEN, message, request.getRequestURI(), List.of());
    }

    @ExceptionHandler({UnauthorizedException.class, AuthenticationException.class})
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(Exception exception, HttpServletRequest request) {
        String message = exception instanceof UnauthorizedException ? exception.getMessage() : "Требуется авторизация";
        return build(HttpStatus.UNAUTHORIZED, message, request.getRequestURI(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOther(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception for {}", request.getRequestURI(), exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Непредвиденная ошибка сервера", request.getRequestURI(), List.of());
    }

    private ApiErrorDetail toDetail(FieldError fieldError) {
        return ApiErrorDetail.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .build();
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            String path,
            List<ApiErrorDetail> details
    ) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(toRussianStatus(status))
                .message(message)
                .path(path)
                .details(details)
                .build();
        return ResponseEntity.status(status).body(response);
    }

    private String toRussianStatus(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Неверный запрос";
            case UNAUTHORIZED -> "Не авторизован";
            case FORBIDDEN -> "Доступ запрещен";
            case NOT_FOUND -> "Не найдено";
            case CONFLICT -> "Конфликт";
            case INTERNAL_SERVER_ERROR -> "Внутренняя ошибка сервера";
            default -> status.getReasonPhrase();
        };
    }
}

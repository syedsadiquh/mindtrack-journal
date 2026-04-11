package com.syedsadiquh.coreservice.journal.exception;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.syedsadiquh.coreservice.journal")
public class JournalExceptionHandler {

    @ExceptionHandler(JournalException.class)
    public ResponseEntity<BaseResponse<String>> handleJournalException(JournalException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(JournalNotFoundException.class)
    public ResponseEntity<BaseResponse<String>> handleJournalNotFoundException(JournalNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(JournalBadRequestException.class)
    public ResponseEntity<BaseResponse<String>> handleJournalBadRequestException(JournalBadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse<>(false, message));
    }

    @ExceptionHandler(TagAlreadyExistsException.class)
    public ResponseEntity<BaseResponse<String>> handleTagAlreadyExistsException(TagAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(TenantAccessDeniedException.class)
    public ResponseEntity<BaseResponse<String>> handleTenantAccessDeniedException(TenantAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new BaseResponse<>(false, ex.getMessage()));
    }
}

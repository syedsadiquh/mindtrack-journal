package com.syedsadiquh.coreservice.journal.exception;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.syedsadiquh.coreservice.journal")
public class JournalExceptionHandler {

    @ExceptionHandler(JournalException.class)
    public ResponseEntity<BaseResponse<String>> handleJournalException(JournalException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse<>(false, ex.getMessage()));
    }
}


package com.syedsadiquh.userservice.exception;

import com.syedsadiquh.userservice.controller.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<String>> handleRuntimeException(RuntimeException ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new BaseResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<BaseResponse<String>> handleUserException(UserException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BaseResponse<>(false, ex.getMessage()));
    }

}

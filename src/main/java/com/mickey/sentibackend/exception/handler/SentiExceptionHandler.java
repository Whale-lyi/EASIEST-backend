package com.mickey.sentibackend.exception.handler;

import com.mickey.sentibackend.entity.Result;
import com.mickey.sentibackend.exception.SentiException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = {"com.mickey.sentibackend.controller"})
public class SentiExceptionHandler {

    @ExceptionHandler(SentiException.class)
    @ResponseBody
    private Result<Object> handleMyServiceException(SentiException e) {
        e.printStackTrace();
        return new Result<>(e.getCode(), e.getMessage(), null);
    }
}

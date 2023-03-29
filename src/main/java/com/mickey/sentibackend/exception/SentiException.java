package com.mickey.sentibackend.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class SentiException extends RuntimeException {

    private final Integer code;

    public SentiException(int code, String message) {
        super(message);
        this.code = code;
    }
}

package org.example.expert.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProfileUploadException extends RuntimeException {

    private final HttpStatus httpStatus;

    public ProfileUploadException(String message, HttpStatus status) {
        super(message);
        this.httpStatus = status;
    }
}

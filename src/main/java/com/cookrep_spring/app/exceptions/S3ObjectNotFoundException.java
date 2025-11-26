package com.cookrep_spring.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class S3ObjectNotFoundException extends RuntimeException{
    public S3ObjectNotFoundException(String message){
        super(message);
    }


}

package com.ominous.tylerutils.http;

public class HttpException extends Exception {
    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Exception causedBy) {
        super(message, causedBy);
    }
}

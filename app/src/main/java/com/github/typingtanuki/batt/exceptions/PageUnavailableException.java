package com.github.typingtanuki.batt.exceptions;

public class PageUnavailableException extends Exception {
    public PageUnavailableException(String url, Throwable cause) {
        super("Error accessing " + url, cause);
    }
}

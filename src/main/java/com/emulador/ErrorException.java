package com.emulador;

public class ErrorException extends Exception {
    public ErrorException(String mensaje) {
        super("ERROR: " + mensaje);
    }
}
package com.emulador;

public class ErrorSemanticoException extends Exception {
    public ErrorSemanticoException(String mensaje) {
        super("ERROR SEMÁNTICO: " + mensaje);
    }
}
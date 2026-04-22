package com.emulador;

public class ErrorSintacticoException extends Exception {
    public ErrorSintacticoException(String mensaje) {
        super("ERROR SINTÁCTICO: " + mensaje);
    }
}
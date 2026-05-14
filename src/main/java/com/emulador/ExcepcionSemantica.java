package com.emulador;

public class ExcepcionSemantica extends Exception {
    public ExcepcionSemantica(String mensaje) {
        super("ERROR SEMÁNTICO: " + mensaje);
    }
}
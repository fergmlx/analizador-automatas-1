package com.emulador;

public class ExcepcionSintactica extends Exception {
    public ExcepcionSintactica(String mensaje) {
        super("ERROR SINTÁCTICO: " + mensaje);
    }
}
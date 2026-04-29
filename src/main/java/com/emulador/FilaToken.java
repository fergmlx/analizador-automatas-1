package com.emulador;

public class FilaToken {
    public String token;
    public String lexema;
    public String patron;
    public String reservada;

    public FilaToken(String token, String lexema, String patron, String reservada) {
        this.token = token;
        this.lexema = lexema;
        this.patron = patron;
        this.reservada = reservada;
    }
}
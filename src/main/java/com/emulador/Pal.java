package com.emulador;

public class Pal {
    private String valor;

    public Pal(String valorStr) throws FormatoInvalidoException {
        if (!valorStr.startsWith("\"") || !valorStr.endsWith("\"")) {
            throw new FormatoInvalidoException("pal", valorStr);
        }
        this.valor = valorStr.replace("\"", "");
    }

    public Pal(String valor, boolean esInterno) {
        this.valor = valor;
    }

    public String getValor() { return valor; }

    public Pal sumar(Pal otro) {
        return new Pal(this.valor + otro.getValor(), true);
    }
    
    public Pal restar(Pal otro) throws TiposIncompatiblesException {
        throw new TiposIncompatiblesException("No se pueden restar tipos 'Pal'.");
    }
    public Pal multiplicar(Pal otro) throws TiposIncompatiblesException {
        throw new TiposIncompatiblesException("No se pueden multiplicar tipos 'Pal'.");
    }
    public Pal dividir(Pal otro) throws TiposIncompatiblesException {
        throw new TiposIncompatiblesException("No se pueden dividir tipos 'Pal'.");
    }
}
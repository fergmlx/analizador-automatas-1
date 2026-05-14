package com.emulador;

public class Pal {
    private String valor;

    public Pal(String valorStr) throws ExcepcionSemantica {
        if (!valorStr.startsWith("\"") || !valorStr.endsWith("\"")) {
            throw new ExcepcionSemantica("El valor '" + valorStr + "' no es un 'pal' válido.");
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
    
    public Pal restar(Pal otro) throws ExcepcionSemantica {
        throw new ExcepcionSemantica("No se pueden restar tipos 'pal'.");
    }
    public Pal multiplicar(Pal otro) throws ExcepcionSemantica {
        throw new ExcepcionSemantica("No se pueden multiplicar tipos 'pal'.");
    }
    public Pal dividir(Pal otro) throws ExcepcionSemantica {
        throw new ExcepcionSemantica("No se pueden dividir tipos 'pal'.");
    }
}
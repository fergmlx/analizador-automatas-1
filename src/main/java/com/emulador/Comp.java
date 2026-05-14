package com.emulador;

import java.util.regex.Pattern;

public class Comp {
    private int valor;
    private static final String REGEX_COMP = "^\\d{1,10}$";

    public Comp(String valorStr) throws ExcepcionSemantica {
        if (!Pattern.matches(REGEX_COMP, valorStr)) {
            throw new ExcepcionSemantica("El valor '" + valorStr + "' no cumple con el formato o límite de 'comp'.");
        }
        this.valor = Integer.parseInt(valorStr);
    }

    public Comp(int valor) {
        this.valor = valor;
    }

    public int getValor() { return valor; }

    public Comp sumar(Comp otro) { return new Comp(this.valor + otro.getValor()); }
    
    public Comp restar(Comp otro) { return new Comp(this.valor - otro.getValor()); }
    
    public Comp multiplicar(Comp otro) { return new Comp(this.valor * otro.getValor()); }
    
    public Comp dividir(Comp otro) throws ExcepcionSemantica {
        if (otro.getValor() == 0) throw new ExcepcionSemantica("División por cero en 'comp'.");
        return new Comp(this.valor / otro.getValor());
    }
}
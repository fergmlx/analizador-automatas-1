package com.emulador;

import java.util.regex.Pattern;

public class Comp {
    private int valor;
    private static final String REGEX_COMP = "^\\d{1,10}$";

    public Comp(String valorStr) throws FormatoInvalidoException {
        if (!Pattern.matches(REGEX_COMP, valorStr)) {
            throw new FormatoInvalidoException("comp", valorStr);
        }
        this.valor = Integer.parseInt(valorStr);
    }

    public Comp(int valor) {
        this.valor = valor;
    }

    public int getValor() { 
        return valor; 
    }

    public Comp sumar(Comp otro) { 
        return new Comp(this.valor + otro.getValor()); 
    }
    
    public Comp restar(Comp otro) { 
        return new Comp(this.valor - otro.getValor()); 
    }
    
    public Comp multiplicar(Comp otro) { 
        return new Comp(this.valor * otro.getValor()); 
    }
    
    public Comp dividir(Comp otro) throws TiposIncompatiblesException {
        if (otro.getValor() == 0) throw new TiposIncompatiblesException("División por cero en 'comp'.");
        return new Comp(this.valor / otro.getValor());
    }
}
package com.emulador;

import java.util.regex.Pattern;

public class Med {
    private double valor;
    private static final String REGEX_MED = "^\\d{1,10}(\\.\\d{1,8})?$";

    public Med(String valorStr) throws FormatoInvalidoException {
        if (!Pattern.matches(REGEX_MED, valorStr)) {
            throw new FormatoInvalidoException("med", valorStr);
        }
        this.valor = Double.parseDouble(valorStr);
    }

    public Med(double valor) {
        this.valor = valor;
    }

    public double getValor() { 
        return valor; 
    }
    
    public Med sumar(Med otro) { 
        return new Med(this.valor + otro.getValor()); 
    }
    
    public Med restar(Med otro) { 
        return new Med(this.valor - otro.getValor()); 
    }
    
    public Med multiplicar(Med otro) { 
        return new Med(this.valor * otro.getValor()); 
    }
    
    public Med dividir(Med otro) throws TiposIncompatiblesException {
        if (otro.getValor() == 0.0) throw new TiposIncompatiblesException("División por cero en 'med'.");
        return new Med(this.valor / otro.getValor());
    }
}
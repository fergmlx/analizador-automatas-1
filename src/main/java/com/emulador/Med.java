package com.emulador;

import java.util.regex.Pattern;

public class Med {
    private double valor;
    private static final String REGEX_MED = "^\\d{1,10}(\\.\\d{1,8})?$";

    public Med(String valorStr) throws ExcepcionSemantica {
        if (!Pattern.matches(REGEX_MED, valorStr)) {
            throw new ExcepcionSemantica("El valor '" + valorStr + "' no cumple con el formato 'med' (10.8).");
        }
        this.valor = Double.parseDouble(valorStr);
    }

    public Med(double valor) {
        this.valor = valor;
    }

    public double getValor() { return valor; }

    public Med sumar(Med otro) { return new Med(this.valor + otro.getValor()); }
    
    public Med restar(Med otro) { return new Med(this.valor - otro.getValor()); }
    
    public Med multiplicar(Med otro) { return new Med(this.valor * otro.getValor()); }
    
    public Med dividir(Med otro) throws ExcepcionSemantica {
        if (otro.getValor() == 0.0) throw new ExcepcionSemantica("División por cero en 'med'.");
        return new Med(this.valor / otro.getValor());
    }
}
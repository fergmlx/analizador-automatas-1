package com.emulador;

public class Comp {
    private int valor;

    public static final int MAX_DIGITOS = 10;

    public static final String PATRON_MOSTRAR = "\\d{1,10}";

    public Comp(String valorStr) throws ExcepcionSemantica {
        if (valorStr == null || valorStr.trim().isEmpty()) {
            throw new ExcepcionSemantica("Literal comp vacío.");
        }
        valorStr = valorStr.trim();

        if (!valorStr.matches("\\d+")) {
            throw new ExcepcionSemantica("Literal comp inválido: '" + valorStr + "'. Solo se permiten dígitos (0-9).");
        }
        if (valorStr.length() > MAX_DIGITOS) {
            throw new ExcepcionSemantica("Literal comp fuera de rango: '" + valorStr + "'. Máximo " + MAX_DIGITOS + " dígitos.");
        }

        try {
            this.valor = Integer.parseInt(valorStr);
        } catch (NumberFormatException ex) {
            throw new ExcepcionSemantica("Literal comp no cabe en int: '" + valorStr + "'.");
        }
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
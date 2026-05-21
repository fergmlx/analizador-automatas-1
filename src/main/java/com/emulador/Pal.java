package com.emulador;

public class Pal {
    private String valor;

    public static final String REGEX_PAL = "^\"[^\"]*\"$";

    public Pal(String valorStr) throws ExcepcionSemantica {
        if (valorStr == null) {
            throw new ExcepcionSemantica("Literal Pal nulo.");
        }
        valorStr = valorStr.trim();

        boolean inicia = valorStr.startsWith("\"");
        boolean termina = valorStr.endsWith("\"");

        if (!inicia && !termina) {
            throw new ExcepcionSemantica("Literal Pal inválido: '" + valorStr + "'. Faltan comillas al inicio y al final.");
        }
        if (!inicia) {
            throw new ExcepcionSemantica("Literal Pal inválido: '" + valorStr + "'. Falta comilla inicial (\").");
        }
        if (!termina) {
            throw new ExcepcionSemantica("Literal Pal inválido: '" + valorStr + "'. Falta comilla final (\").");
        }

        // contenido sin comillas
        this.valor = valorStr.substring(1, valorStr.length() - 1);
    }

    public Pal(String valor, boolean esInterno) {
        this.valor = valor;
    }

    public String getValor() { return valor; }

    public Pal sumar(Pal otro) {
        return new Pal(this.valor + otro.getValor(), true);
    }

    public Pal restar(Pal otro) throws ExcepcionSemantica {
        throw new ExcepcionSemantica("Operación inválida: no se puede restar 'Pal'.");
    }
    public Pal multiplicar(Pal otro) throws ExcepcionSemantica {
        throw new ExcepcionSemantica("Operación inválida: no se puede multiplicar 'Pal'.");
    }
    public Pal dividir(Pal otro) throws ExcepcionSemantica {
        throw new ExcepcionSemantica("Operación inválida: no se puede dividir 'Pal'.");
    }
}
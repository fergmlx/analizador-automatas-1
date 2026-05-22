package com.emulador;

public class Med {
    private double valor;

    public static final int MAX_ENTEROS = 10;
    public static final int MAX_DECIMALES = 8;

    public static final String PATRON_MOSTRAR = "\\d{1,10}(\\.\\d{1,8})?";

    public Med(String valorStr) throws ExcepcionSemantica {
        if (valorStr == null || valorStr.trim().isEmpty()) {
            throw new ExcepcionSemantica("Literal med vacío.");
        }
        valorStr = valorStr.trim();

        if (!valorStr.matches("[0-9.]+")) {
            throw new ExcepcionSemantica("Literal med inválido: '" + valorStr + "'. Solo se permiten dígitos y '.'.");
        }

        int puntos = 0;
        for (int i = 0; i < valorStr.length(); i++) if (valorStr.charAt(i) == '.') puntos++;
        if (puntos > 1) {
            throw new ExcepcionSemantica("Literal med inválido: '" + valorStr + "'. Solo se permite un punto decimal.");
        }
        if (valorStr.startsWith(".") || valorStr.endsWith(".")) {
            throw new ExcepcionSemantica("Literal med inválido: '" + valorStr + "'. El punto no puede ir al inicio o al final.");
        }

        String[] partes = valorStr.split("\\.");
        String parteEntera = partes[0];
        String parteDecimal = (partes.length == 2) ? partes[1] : "";

        if (parteEntera.length() < 1) {
            throw new ExcepcionSemantica("Literal med inválido: falta parte entera en '" + valorStr + "'.");
        }
        if (parteEntera.length() > MAX_ENTEROS) {
            throw new ExcepcionSemantica("Literal med fuera de rango: '" + valorStr + "'. Máximo " + MAX_ENTEROS + " dígitos enteros.");
        }
        if (!parteDecimal.isEmpty() && parteDecimal.length() > MAX_DECIMALES) {
            throw new ExcepcionSemantica("Literal med fuera de rango: '" + valorStr + "'. Máximo " + MAX_DECIMALES + " dígitos decimales.");
        }

        // Validación final contra la regla (sin anclas, validamos con matches() total usando anclas implícitas)
        if (!valorStr.matches("\\d{1,10}(\\.\\d{1,8})?")) {
            throw new ExcepcionSemantica("Literal med inválido: '" + valorStr + "'. Patrón: " + PATRON_MOSTRAR);
        }

        try {
            this.valor = Double.parseDouble(valorStr);
        } catch (NumberFormatException ex) {
            throw new ExcepcionSemantica("Literal med inválido: no se pudo convertir '" + valorStr + "' a número.");
        }
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
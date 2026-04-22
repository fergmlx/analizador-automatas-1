package com.emulador;

public class Main {
    public static void main(String[] args) {
        Analizador analizador = new Analizador();
        int hola = 20;
        int bu = 2;
        
        String[] codigoFuente = {
            "// Declaramos las variables",
            "comp edad $ 20",
            "med altura $ 1.75",
            "Pal nombre $ \"Fer\"",
            "// Probando reemplazo y operaciones",
            "comp nuevaEdad $ edad + 5",
            "// Error semántico: Incompatibilidad de tipos",
            "med error $ altura + nombre",
            "// Error sintáctico: Falta operando",
            "hola+",
            "+edad",
            "// Error semántico: Variable no declarada",
            "saludo",
            "// Error semántico: Excede el límite de 10 dígitos (Formato inválido)",
            "comp numGigante $ 12345678901",
        };
        
        for (int i = 0; i < codigoFuente.length; i++) {
            try {
                System.out.println("Analizando: " + codigoFuente[i]);
                analizador.procesarLinea(codigoFuente[i]);
            } catch (Exception e) {
                System.err.println("Linea " + (i+1) + " -> " + e.getMessage());
            }
        }
    }   
}
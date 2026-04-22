package com.analizador;

import java.io.StringReader;

public class App {
    public static void main(String[] args) throws Exception {
        String codigo = "x = 3 + 5 ; ";
        
        Lexer lexer = new Lexer(new StringReader(codigo));
        Parser parser = new Parser(lexer);
        
        System.out.println("Analizando: " + codigo);
        parser.parse();
        System.out.println("Análisis completado sin errores.");
    }
}
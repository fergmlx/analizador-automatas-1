package com.emulador;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analizador {
    
    private Map<String, Object> tablaSimbolos = new HashMap<>();
    private static final String REGEX_COMENTARIO = "%.*";
    private static final String SIMBOLO_ASIGNACION = "$";
    
    public void procesarLinea(String linea) throws Exception {
        linea = linea.replaceAll(REGEX_COMENTARIO, "").trim();
        if (linea.isEmpty()) return;
        
        if (linea.endsWith("+") || linea.startsWith("+") ||
            linea.endsWith("-") || linea.startsWith("-") ||
            linea.endsWith("*") || linea.startsWith("*") ||
            linea.endsWith("/") || linea.startsWith("/")) {
            throw new FaltaOperandoException(linea);
        }

        if (!linea.contains(SIMBOLO_ASIGNACION)) {
            evaluarExpresion(linea, ""); 
            return;
        }

        String[] partes = linea.split("\\" + SIMBOLO_ASIGNACION);
        String ladoIzquierdo = partes[0].trim();
        String expresion = partes.length > 1 ? partes[1].trim() : "";
        
        if (expresion.isEmpty()) {
            throw new Exception("ERROR SINTÁCTICO: Falta valor después de la asignación '$'.");
        }
        
        String tipo = "";
        String nombreVar = "";
        
        String[] dec = ladoIzquierdo.split(" ");
        if (dec.length == 2) {
            tipo = dec[0];
            nombreVar = dec[1];
        } else if (dec.length == 1) {
            nombreVar = dec[0];
            if (!tablaSimbolos.containsKey(nombreVar)) {
                throw new VariableNoDeclaradaException(nombreVar);
            }
            
            Object varGuardada = tablaSimbolos.get(nombreVar);
            if (varGuardada instanceof Comp) tipo = "comp";
            else if (varGuardada instanceof Med) tipo = "med";
            else if (varGuardada instanceof Pal) tipo = "Pal";
        } else {
            throw new Exception("ERROR SINTÁCTICO: Declaración mal formada en '" + ladoIzquierdo + "'");
        }
        
        Object valorFinal = evaluarExpresion(expresion, tipo);
        tablaSimbolos.put(nombreVar, valorFinal);
        
        String valorStr = "";
        if (valorFinal instanceof Comp) valorStr = String.valueOf(((Comp)valorFinal).getValor());
        if (valorFinal instanceof Med) valorStr = String.valueOf(((Med)valorFinal).getValor());
        if (valorFinal instanceof Pal) valorStr = "\"" + ((Pal)valorFinal).getValor() + "\"";
        
        System.out.println("-> OK: " + nombreVar + " $ " + valorStr + " [" + tipo + "]");
    }
    
    public String procesarLineaFrame(String linea) throws Exception {
        linea = linea.replaceAll(REGEX_COMENTARIO, "").trim();
        if (linea.isEmpty()) return "";

        if (linea.endsWith("+") || linea.startsWith("+") ||
            linea.endsWith("-") || linea.startsWith("-") ||
            linea.endsWith("*") || linea.startsWith("*") ||
            linea.endsWith("/") || linea.startsWith("/")) {
            throw new FaltaOperandoException(linea);
        }

        if (!linea.contains(SIMBOLO_ASIGNACION)) {
            evaluarExpresion(linea, ""); 
            return "";
        }

        String[] partes = linea.split("\\" + SIMBOLO_ASIGNACION);
        String ladoIzquierdo = partes[0].trim();
        String expresion = partes.length > 1 ? partes[1].trim() : "";
        
        if (expresion.isEmpty()) {
            throw new Exception("ERROR SINTÁCTICO: Falta valor después de la asignación '$'.");
        }
        
        String tipo = "";
        String nombreVar = "";
        
        String[] dec = ladoIzquierdo.split(" ");
        if (dec.length == 2) {
            tipo = dec[0];
            nombreVar = dec[1];
        } else if (dec.length == 1) {
            nombreVar = dec[0];
            if (!tablaSimbolos.containsKey(nombreVar)) {
                throw new VariableNoDeclaradaException(nombreVar);
            }
            
            Object varGuardada = tablaSimbolos.get(nombreVar);
            if (varGuardada instanceof Comp) tipo = "comp";
            else if (varGuardada instanceof Med) tipo = "med";
            else if (varGuardada instanceof Pal) tipo = "Pal";
        } else {
            throw new Exception("ERROR SINTÁCTICO: Declaración mal formada en '" + ladoIzquierdo + "'");
        }
        
        Object valorFinal = evaluarExpresion(expresion, tipo);
        tablaSimbolos.put(nombreVar, valorFinal);
        
        String valorStr = "";
        if (valorFinal instanceof Comp) valorStr = String.valueOf(((Comp)valorFinal).getValor());
        if (valorFinal instanceof Med) valorStr = String.valueOf(((Med)valorFinal).getValor());
        if (valorFinal instanceof Pal) valorStr = "\"" + ((Pal)valorFinal).getValor() + "\"";

        return "-> OK: " + nombreVar + " $ " + valorStr + " [" + tipo + "]";
    }
    
    private Object evaluarExpresion(String expresion, String tipoEsperado) throws Exception {
        String operador = "";
        String regexSplit = "";
        
        if (expresion.contains("+")) { operador = "+"; regexSplit = "\\+"; }
        else if (expresion.contains("-")) { operador = "-"; regexSplit = "-"; }
        else if (expresion.contains("*")) { operador = "*"; regexSplit = "\\*"; }
        else if (expresion.contains("/")) { operador = "/"; regexSplit = "/"; }
        
        if (!operador.isEmpty()) {
            String[] operandos = expresion.split(regexSplit);
            if (operandos.length != 2) {
                throw new FaltaOperandoException(expresion);
            }
            
            Object op1 = obtenerValorOLiteral(operandos[0].trim(), tipoEsperado);
            Object op2 = obtenerValorOLiteral(operandos[1].trim(), tipoEsperado);
            
            return ejecutarOperacion(op1, op2, operador, tipoEsperado); 
        }
        
        return obtenerValorOLiteral(expresion, tipoEsperado);
    }
    
    private Object obtenerValorOLiteral(String token, String tipoEsperado) throws Exception {
        if (tablaSimbolos.containsKey(token)) {
            Object var = tablaSimbolos.get(token);
            // Si validamos asignación, verificamos que el tipo coincida
            if (!tipoEsperado.isEmpty()) {
                if (tipoEsperado.equals("comp") && !(var instanceof Comp)) throw new TiposIncompatiblesException("'" + token + "' no es 'comp'.");
                if (tipoEsperado.equals("med") && !(var instanceof Med)) throw new TiposIncompatiblesException("'" + token + "' no es 'med'.");
                if (tipoEsperado.equals("Pal") && !(var instanceof Pal)) throw new TiposIncompatiblesException("'" + token + "' no es 'Pal'.");
            }
            return var;
        }
        
        // Si no está en la tabla de símbolos y no hay tipo esperado, es una variable que no existe
        if (tipoEsperado.isEmpty()) {
            throw new VariableNoDeclaradaException(token);
        }
        
        // Parsear directo al tipo
        switch (tipoEsperado) {
            case "comp": return new Comp(token);
            case "med": return new Med(token);
            case "Pal": return new Pal(token);
            default: throw new TiposIncompatiblesException("Tipo desconocido.");
        }
    }

    private Object ejecutarOperacion(Object op1, Object op2, String operador, String tipoEsperado) throws Exception {
        // ejecuta la operación utilizando el método de cada clase
        if (op1 instanceof Comp && op2 instanceof Comp) {
            Comp c1 = (Comp) op1; Comp c2 = (Comp) op2;
            switch(operador) {
                case "+": return c1.sumar(c2);
                case "-": return c1.restar(c2);
                case "*": return c1.multiplicar(c2);
                case "/": return c1.dividir(c2);
            }
        } else if (op1 instanceof Med && op2 instanceof Med) {
            Med m1 = (Med) op1; Med m2 = (Med) op2;
            switch(operador) {
                case "+": return m1.sumar(m2);
                case "-": return m1.restar(m2);
                case "*": return m1.multiplicar(m2);
                case "/": return m1.dividir(m2);
            }
        } else if (op1 instanceof Pal && op2 instanceof Pal) {
            Pal p1 = (Pal) op1; Pal p2 = (Pal) op2;
            switch(operador) {
                case "+": return p1.sumar(p2);
                case "-": return p1.restar(p2);
                case "*": return p1.multiplicar(p2);
                case "/": return p1.dividir(p2);
            }
        }
        
        throw new TiposIncompatiblesException("No se pueden operar tipos diferentes.");
    }
    
    public List<FilaToken> obtenerTokensLexicos(String linea) {
        List<FilaToken> tokensDetectados = new ArrayList<>();
        
        linea = linea.replaceAll("%.*", "").trim();
        if (linea.isEmpty()) return tokensDetectados;

        // encontrar qué es
        String regex = "(\"[^\"]*\")|(\\bcomp\\b|\\bmed\\b|\\bPal\\b)|([a-zA-Z][a-zA-Z0-9]*)|(\\d+\\.\\d+)|(\\d+)|([\\$\\+\\-\\*\\/])";
        Matcher matcher = Pattern.compile(regex).matcher(linea);

        while (matcher.find()) {
            String lexema = matcher.group();
            
            if (matcher.group(1) != null) {
                tokensDetectados.add(new FilaToken("Cadena", lexema, "\"[^\"]*\"", "no"));
            } else if (matcher.group(2) != null) {
                tokensDetectados.add(new FilaToken("Tipo de dato", lexema, "\\b(comp|med|Pal)\\b", "yes"));
            } else if (matcher.group(3) != null) {
                tokensDetectados.add(new FilaToken("Identificador", lexema, "[a-zA-Z][a-zA-Z0-9]*", "no"));
            } else if (matcher.group(4) != null) {
                tokensDetectados.add(new FilaToken("Decimal", lexema, "\\d+\\.\\d+", "no"));
            } else if (matcher.group(5) != null) {
                tokensDetectados.add(new FilaToken("Entero", lexema, "\\d+", "no"));
            } else if (matcher.group(6) != null) {
                tokensDetectados.add(new FilaToken(lexema, lexema, "[\\$\\+\\-\\*\\/]", "yes")); 
            }
        }
        
        return tokensDetectados;
    }
}
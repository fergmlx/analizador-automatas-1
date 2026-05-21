package com.emulador;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analizador {

    private final Map<String, Object> tablaSimbolos = new HashMap<>();
    private static final String REGEX_COMENTARIO = "%.*";
    private static final String SIMBOLO_ASIGNACION = "$";

    private static final String REGEX_LEXER =
            "(\"[^\"]*\")" +                          // 1) cadena
            "|(\\bcomp\\b|\\bmed\\b|\\bpal\\b)" +     // 2) tipos reservados
            "|([a-zA-Z][a-zA-Z0-9]*)" +               // 3) identificador
            "|(\\d+\\.\\d+)" +                        // 4) decimal simple
            "|(\\d+)" +                               // 5) entero simple
            "|([\\$\\+\\-\\*\\/\\{\\}\\(\\)])";       // 6) símbolos (+ {} ())

    public void procesarLineaFrame(String linea) throws Exception {
        linea = limpiarComentarios(linea);
        if (linea.isEmpty()) return;

        if (linea.startsWith("if")) {
            validarIfHeader(linea);
            return;
        }

        if (linea.matches("^[\\+\\-\\*\\/].*") || linea.matches(".*[\\+\\-\\*\\/]$")) {
            throw new ExcepcionSintactica("Falta un operando en la expresión: '" + linea + "'");
        }

        if (!linea.contains(SIMBOLO_ASIGNACION)) {
            throw new ExcepcionSintactica("Formato inválido. Se espera: identificador tipo $ expresión");
        }

        int countDollar = contarCaracter(linea, '$');
        if (countDollar != 1) {
            throw new ExcepcionSintactica("La línea debe contener exactamente una asignación '$': '" + linea + "'");
        }

        String[] partes = linea.split("\\$");
        String ladoIzquierdo = partes[0].trim();
        String expresion = (partes.length > 1) ? partes[1].trim() : "";

        if (ladoIzquierdo.isEmpty()) {
            throw new ExcepcionSintactica("Falta el lado izquierdo antes de '$'.");
        }
        if (expresion.isEmpty()) {
            throw new ExcepcionSintactica("Falta valor después de '$'.");
        }

        // iden tipo
        String[] dec = ladoIzquierdo.split("\\s+");
        if (dec.length != 2) {
            throw new ExcepcionSintactica("Lado izquierdo inválido. Se espera: identificador tipo");
        }

        String nombreVar = dec[0].trim();
        String tipo = dec[1].trim();

        if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9]*$", nombreVar)) {
            throw new ExcepcionSintactica("Identificador inválido: '" + nombreVar + "'");
        }

        if (tablaSimbolos.containsKey(nombreVar)) {
            throw new ExcepcionSemantica("La variable '" + nombreVar + "' ya fue declarada previamente.");
        }

        // tipo conocido
        if (!tipo.equals("comp") && !tipo.equals("med") && !tipo.equals("pal")) {
            throw new ExcepcionSemantica("Tipo de dato desconocido: '" + tipo + "'. Tipos válidos: comp, med, pal.");
        }

        Object valorFinal = evaluarExpresion(expresion, tipo);
        tablaSimbolos.put(nombreVar, valorFinal);
    }

    private void validarIfHeader(String linea) throws Exception {
        String sinEspacios = linea.replaceAll("\\s+", "");

        // debe iniciar con if(
        if (!sinEspacios.startsWith("if(")) {
            throw new ExcepcionSintactica("Formato if inválido. Se espera: if (true) {");
        }

        int p1 = sinEspacios.indexOf('(');
        int p2 = sinEspacios.indexOf(')', p1 + 1);
        if (p2 < 0) {
            throw new ExcepcionSintactica("Formato if inválido: falta ')'.");
        }

        String cond = sinEspacios.substring(p1 + 1, p2);
        if (!cond.equals("true") && !cond.equals("false")) {
            throw new ExcepcionSemantica("Condición if inválida. Solo se aceptan booleanos: true o false.");
        }

        // debe abrir bloque con {
        if (!sinEspacios.substring(p2 + 1).startsWith("{")) {
            throw new ExcepcionSintactica("Formato if inválido: falta '{' para abrir el bloque.");
        }
    }

    private Object evaluarExpresion(String expresion, String tipoEsperado) throws Exception {
        expresion = expresion.trim();
        if (expresion.isEmpty()) {
            throw new ExcepcionSintactica("Expresión vacía.");
        }

        String operador = "";
        String regexSplit = "";

        if (expresion.contains("+")) { operador = "+"; regexSplit = "\\+"; }
        else if (expresion.contains("-")) { operador = "-"; regexSplit = "-"; }
        else if (expresion.contains("*")) { operador = "*"; regexSplit = "\\*"; }
        else if (expresion.contains("/")) { operador = "/"; regexSplit = "/"; }

        if (!operador.isEmpty()) {
            String[] operandos = expresion.split(regexSplit);
            if (operandos.length != 2) {
                throw new ExcepcionSintactica("Expresión inválida, se esperaba 'a " + operador + " b': '" + expresion + "'");
            }

            Object op1 = obtenerValorOLiteral(operandos[0].trim(), tipoEsperado);
            Object op2 = obtenerValorOLiteral(operandos[1].trim(), tipoEsperado);

            return ejecutarOperacion(op1, op2, operador);
        }

        return obtenerValorOLiteral(expresion, tipoEsperado);
    }

    private Object obtenerValorOLiteral(String token, String tipoEsperado) throws Exception {
        token = token.trim();

        if (tablaSimbolos.containsKey(token)) {
            Object var = tablaSimbolos.get(token);
            if (tipoEsperado.equals("comp") && !(var instanceof Comp)) throw new ExcepcionSemantica("'" + token + "' no es tipo 'comp'.");
            if (tipoEsperado.equals("med") && !(var instanceof Med)) throw new ExcepcionSemantica("'" + token + "' no es tipo 'med'.");
            if (tipoEsperado.equals("pal") && !(var instanceof Pal)) throw new ExcepcionSemantica("'" + token + "' no es tipo 'pal'.");
            return var;
        }

        switch (tipoEsperado) {
            case "comp": return new Comp(token);
            case "med": return new Med(token);
            case "pal": return new Pal(token);
            default: throw new ExcepcionSemantica("Tipo de dato desconocido: " + tipoEsperado);
        }
    }

    private Object ejecutarOperacion(Object op1, Object op2, String operador) throws Exception {
        if (op1 instanceof Comp && op2 instanceof Comp) {
            Comp c1 = (Comp) op1; Comp c2 = (Comp) op2;
            switch (operador) {
                case "+": return c1.sumar(c2);
                case "-": return c1.restar(c2);
                case "*": return c1.multiplicar(c2);
                case "/": return c1.dividir(c2);
            }
        } else if (op1 instanceof Med && op2 instanceof Med) {
            Med m1 = (Med) op1; Med m2 = (Med) op2;
            switch (operador) {
                case "+": return m1.sumar(m2);
                case "-": return m1.restar(m2);
                case "*": return m1.multiplicar(m2);
                case "/": return m1.dividir(m2);
            }
        } else if (op1 instanceof Pal && op2 instanceof Pal) {
            Pal p1 = (Pal) op1; Pal p2 = (Pal) op2;
            switch (operador) {
                case "+": return p1.sumar(p2);
                case "-": return p1.restar(p2);
                case "*": return p1.multiplicar(p2);
                case "/": return p1.dividir(p2);
            }
        }

        throw new ExcepcionSemantica("No se pueden operar variables de tipos diferentes.");
    }

    public List<FilaToken> obtenerTokensLexicos(String linea) {
        List<FilaToken> tokensDetectados = new ArrayList<>();
        linea = limpiarComentarios(linea);
        if (linea.isEmpty()) return tokensDetectados;

        Matcher matcher = Pattern.compile(REGEX_LEXER).matcher(linea);

        while (matcher.find()) {
            String lexema = matcher.group();

            // cadena
            if (matcher.group(1) != null) {
                tokensDetectados.add(new FilaToken("Cadena", lexema, "^\"[^\"]*\"$", "no"));
                continue;
            }

            // tipos reservados
            if (matcher.group(2) != null) {
                String patron;
                if ("comp".equals(lexema)) patron = Comp.REGEX_COMP;
                else if ("med".equals(lexema)) patron = Med.REGEX_MED;
                else patron = Pal.REGEX_PAL;

                patron = patron.replace("\\\\", "\\"); // mostrar \ real en la tabla

                tokensDetectados.add(new FilaToken("Tipo de dato", lexema, patron, "sí"));
                continue;
            }

            // identificador
            if (matcher.group(3) != null) {
                tokensDetectados.add(new FilaToken("Identificador", lexema, "^[a-zA-Z][a-zA-Z0-9]*$", "no"));
                continue;
            }

            // decimal 
            if (matcher.group(4) != null) {
                String patron = Med.REGEX_MED.replace("\\\\", "\\");
                tokensDetectados.add(new FilaToken("Decimal", lexema, patron, "no"));
                continue;
            }

            // entero 
            if (matcher.group(5) != null) {
                String patron = Comp.REGEX_COMP.replace("\\\\", "\\");
                tokensDetectados.add(new FilaToken("Entero", lexema, patron, "no"));
                continue;
            }

            // símbolos
            if (matcher.group(6) != null) {
                String patron = "^[\\$\\+\\-\\*\\/\\{\\}\\(\\)]$".replace("\\\\", "\\");
                tokensDetectados.add(new FilaToken("Signo", lexema, patron, "sí"));
            }
        }

        return tokensDetectados;
    }

    private String limpiarComentarios(String linea) {
        return linea.replaceAll(REGEX_COMENTARIO, "").trim();
    }

    private int contarCaracter(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == c) count++;
        return count;
    }
}
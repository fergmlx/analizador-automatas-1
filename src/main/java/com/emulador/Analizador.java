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
            "(\"[^\"]*\")" +                               // cadena
            "|(\\bcomp\\b|\\bmed\\b|\\bpal\\b|\\bif\\b)" + // reservadas
            "|(==|!=|<=|>=|<|>)" +                         // relacionales
            "|([a-zA-Z][a-zA-Z0-9]*)" +                    // identificador
            "|(\\d+\\.\\d+)" +                             // decimal simple
            "|(\\d+)" +                                    // entero simple
            "|([\\$\\+\\-\\*\\/\\{\\}\\(\\)])";            // símbolos


    public void procesarLineaFrame(String linea) throws Exception {
        linea = limpiarComentarios(linea);
        if (linea.isEmpty()) return;

        if (linea.startsWith("if")) {
            validarIfHeader(linea);
            return;
        }

        if (!linea.contains(SIMBOLO_ASIGNACION)) {
            throw new ExcepcionSintactica("Formato inválido. Se espera: identificador tipo $ expresión");
        }

        int countDollar = contarCaracter(linea, '$');
        if (countDollar != 1) {
            throw new ExcepcionSintactica("La línea debe contener exactamente una asignación '$': '" + linea + "'");
        }

        String[] partes = linea.split("\\$");
        String ladoIzq = partes[0].trim();
        String expr = (partes.length > 1) ? partes[1].trim() : "";

        if (expr.isEmpty()) throw new ExcepcionSintactica("Falta valor después de '$'.");

        String[] dec = ladoIzq.split("\\s+");
        if (dec.length != 2) {
            throw new ExcepcionSintactica("Lado izquierdo inválido. Se espera: identificador tipo");
        }

        String id = dec[0].trim();
        String tipo = dec[1].trim();

        if (!id.matches("[a-zA-Z][a-zA-Z0-9]*")) {
            throw new ExcepcionSintactica("Identificador inválido: '" + id + "'");
        }

        if (tablaSimbolos.containsKey(id)) {
            throw new ExcepcionSemantica("La variable '" + id + "' ya fue declarada previamente.");
        }

        if (!tipo.equals("comp") && !tipo.equals("med") && !tipo.equals("pal")) {
            throw new ExcepcionSemantica("Tipo de dato desconocido: '" + tipo + "'. Tipos válidos: comp, med, pal.");
        }

        Object valorFinal = evaluarExpresion(expr, tipo);
        tablaSimbolos.put(id, valorFinal);
    }

    public boolean evaluarCondicionIf(String lineaIf) throws Exception {
        String s = limpiarComentarios(lineaIf);
        validarIfHeader(s);

        int p1 = s.indexOf('(');
        int p2 = s.lastIndexOf(')');
        String cond = s.substring(p1 + 1, p2).trim();

        String[] ops = new String[] { "==", "!=", "<=", ">=", "<", ">" };
        String opEncontrado = null;
        int idx = -1;
        for (String op : ops) {
            idx = cond.indexOf(op);
            if (idx > 0) { opEncontrado = op; break; }
        }
        if (opEncontrado == null) {
            throw new ExcepcionSintactica("Condición inválida. Operador relacional esperado: == != < <= > >=");
        }

        String izq = cond.substring(0, idx).trim();
        String der = cond.substring(idx + opEncontrado.length()).trim();

        Object a = resolverValorCondicion(izq);
        Object b = resolverValorCondicion(der);

        if (!a.getClass().equals(b.getClass())) {
            throw new ExcepcionSemantica("Comparación inválida: tipos diferentes en if.");
        }

        if (a instanceof Comp) {
            int x = ((Comp) a).getValor();
            int y = ((Comp) b).getValor();
            return compararNumeros(x, y, opEncontrado);
        }

        if (a instanceof Med) {
            double x = ((Med) a).getValor();
            double y = ((Med) b).getValor();
            return compararNumeros(x, y, opEncontrado);
        }

        if (a instanceof Pal) {
            String x = ((Pal) a).getValor();
            String y = ((Pal) b).getValor();
            return compararStrings(x, y, opEncontrado);
        }

        throw new ExcepcionSemantica("Tipo desconocido en condición if.");
    }

    private void validarIfHeader(String linea) throws Exception {
        String s = linea.trim();

        int p1 = s.indexOf('(');
        int p2 = s.lastIndexOf(')');
        if (p1 < 0 || p2 < 0 || p2 < p1) {
            throw new ExcepcionSintactica("Formato de if inválido. Se espera: if (a > b) {");
        }
        if (!s.substring(p2).contains("{")) {
            throw new ExcepcionSintactica("Formato de if inválido: falta '{' para abrir el bloque.");
        }

        String cond = s.substring(p1 + 1, p2).trim();
        if (cond.isEmpty()) throw new ExcepcionSintactica("Condición del if vacía.");
    }

    private Object resolverValorCondicion(String t) throws Exception {
        t = t.trim();

        if (tablaSimbolos.containsKey(t)) return tablaSimbolos.get(t);

        if (t.startsWith("\"") && t.endsWith("\"")) return new Pal(t);

        if (t.matches("\\d+\\.\\d+")) return new Med(t);

        if (t.matches("\\d+")) return new Comp(t);

        throw new ExcepcionSemantica("En if, '" + t + "' no es literal válido ni variable declarada.");
    }

    private boolean compararNumeros(double x, double y, String op) throws Exception {
        switch (op) {
            case "==": return x == y;
            case "!=": return x != y;
            case "<":  return x < y;
            case "<=": return x <= y;
            case ">":  return x > y;
            case ">=": return x >= y;
        }
        throw new ExcepcionSintactica("Operador relacional inválido: " + op);
    }

    private boolean compararStrings(String x, String y, String op) throws Exception {
        int c = x.compareTo(y);
        switch (op) {
            case "==": return x.equals(y);
            case "!=": return !x.equals(y);
            case "<":  return c < 0;
            case "<=": return c <= 0;
            case ">":  return c > 0;
            case ">=": return c >= 0;
        }
        throw new ExcepcionSintactica("Operador relacional inválido: " + op);
    }

    private Object evaluarExpresion(String expresion, String tipoEsperado) throws Exception {
        String operador = "";
        String regexSplit = "";

        if (expresion.contains("+")) { operador = "+"; regexSplit = "\\+"; }
        else if (expresion.contains("-")) { operador = "-"; regexSplit = "-"; }
        else if (expresion.contains("*")) { operador = "*"; regexSplit = "\\*"; }
        else if (expresion.contains("/")) { operador = "/"; regexSplit = "/"; }

        if (!operador.isEmpty()) {
            String[] ops = expresion.split(regexSplit);
            if (ops.length != 2) throw new ExcepcionSintactica("Falta un operando en la expresión: '" + expresion + "'");
            Object op1 = obtenerValorOLiteral(ops[0].trim(), tipoEsperado);
            Object op2 = obtenerValorOLiteral(ops[1].trim(), tipoEsperado);
            return ejecutarOperacion(op1, op2, operador);
        }

        return obtenerValorOLiteral(expresion.trim(), tipoEsperado);
    }

    private Object obtenerValorOLiteral(String token, String tipoEsperado) throws Exception {
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
            Comp a = (Comp) op1; Comp b = (Comp) op2;
            switch (operador) {
                case "+": return a.sumar(b);
                case "-": return a.restar(b);
                case "*": return a.multiplicar(b);
                case "/": return a.dividir(b);
            }
        } else if (op1 instanceof Med && op2 instanceof Med) {
            Med a = (Med) op1; Med b = (Med) op2;
            switch (operador) {
                case "+": return a.sumar(b);
                case "-": return a.restar(b);
                case "*": return a.multiplicar(b);
                case "/": return a.dividir(b);
            }
        } else if (op1 instanceof Pal && op2 instanceof Pal) {
            Pal a = (Pal) op1; Pal b = (Pal) op2;
            switch (operador) {
                case "+": return a.sumar(b);
                case "-": return a.restar(b);
                case "*": return a.multiplicar(b);
                case "/": return a.dividir(b);
            }
        }

        throw new ExcepcionSemantica("No se pueden operar variables de tipos diferentes.");
    }

    public List<FilaToken> obtenerTokensLexicos(String linea) {
        List<FilaToken> out = new ArrayList<>();
        linea = limpiarComentarios(linea);
        if (linea.isEmpty()) return out;

        Matcher m = Pattern.compile(REGEX_LEXER).matcher(linea);

        while (m.find()) {
            String lex = m.group();

            if (m.group(1) != null) {
                out.add(new FilaToken("Cadena", lex, "\"[^\"]*\"", "no"));
                continue;
            }

            if (m.group(2) != null) {
                String patron;
                switch (lex) {
                    case "comp": patron = "\\d{1,10}"; break;
                    case "med": patron = "\\d{1,10}(\\.\\d{1,8})?"; break;
                    case "pal": patron = "\"[^\"]*\""; break;
                    case "if":  patron = "\\bif\\b"; break;
                    default: patron = ""; break;
                }
                out.add(new FilaToken("Reservada", lex, patron, "sí"));
                continue;
            }

            if (m.group(3) != null) {
                out.add(new FilaToken("Operador relacional", lex, "(==|!=|<=|>=|<|>)", "sí"));
                continue;
            }

            if (m.group(4) != null) {
                out.add(new FilaToken("Identificador", lex, "(a-zA-Z)(a-zA-Z0-9)*", "no"));
                continue;
            }

            if (m.group(5) != null) {
                out.add(new FilaToken("Decimal", lex, "\\d{1,10}(\\.\\d{1,8})?", "no"));
                continue;
            }

            if (m.group(6) != null) {
                out.add(new FilaToken("Entero", lex, "\\d{1,10}", "no"));
                continue;
            }

            if (m.group(7) != null) {
                out.add(new FilaToken("Signo", lex, "(\\$|\\+|\\-|\\*|\\/|\\{|\\}|\\(|\\))", "sí"));
            }
        }

        return out;
    }

    public String gramaticaDeLinea(String linea) {
        linea = limpiarComentarios(linea);
        if (linea.isEmpty()) return "";

        if (linea.startsWith("if")) {
            return "(<bloque> : <paréntesis> : <expresión>)";
        }

        String expr = "";
        try {
            String[] partes = linea.split("\\$");
            expr = (partes.length > 1) ? partes[1].trim() : "";
        } catch (Exception e) {
            expr = "";
        }

        String claseLiteral = "<expresion>";
        String literalMostrar = expr;

        if (expr.matches("\\d+")) {
            claseLiteral = "<numeros>";
        } else if (expr.matches("\\d+\\.\\d+")) {
            claseLiteral = "<numeros>";
        } else if (expr.startsWith("\"") && expr.endsWith("\"")) {
            claseLiteral = "<cadena>";
        } else {
            claseLiteral = "<expresion>";
        }

        return "(<expresion> : <id> : <tipo de dato> : <asignacion> : <expresion> : "
                + claseLiteral + " : " + literalMostrar + " : <break>)";
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
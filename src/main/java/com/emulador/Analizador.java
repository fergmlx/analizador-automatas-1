package com.emulador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analizador {

    private static final String REGEX_COMENTARIO = "%.*";
    private static final String SIMBOLO_ASIGNACION = "$";

    public static final String KW_SI = "si";
    public static final String KW_SINO = "sino";

    // global
    private final Map<String, Object> tablaGlobal = new HashMap<>();

    // temporal del bloque actual (si/sino)
    private Map<String, Object> tablaBloque = null;

    private static final String REGEX_LEXER =
            "(\"[^\"]*\")" +
            "|(\\bcomp\\b|\\bmed\\b|\\bpal\\b|\\bsi\\b|\\bsino\\b)" +
            "|(==|!=|<=|>=|<|>)" +
            "|([a-zA-Z][a-zA-Z0-9]*)" +
            "|(\\d+\\.\\d+)" +
            "|(\\d+)" +
            "|([\\$\\+\\-\\*\\/\\{\\}\\(\\)])";

    public String limpiar(String linea) {
        return (linea == null) ? "" : linea.replaceAll(REGEX_COMENTARIO, "").trim();
    }

    public boolean esSoloLlaveCierre(String linea) { return "}".equals(limpiar(linea)); }
    public boolean esSoloLlaveApertura(String linea) { return "{".equals(limpiar(linea)); }
    public boolean esLineaSi(String linea) { return limpiar(linea).startsWith(KW_SI); }
    public boolean esLineaSino(String linea) { return limpiar(linea).startsWith(KW_SINO); }

    public boolean esLineaCierreMasSino(String linea) {
        String s = limpiar(linea);
        return s.matches("^\\}\\s*" + KW_SINO + "\\s*\\{\\s*$");
    }

    private boolean esIdentificador(String s) {
        return s != null && s.matches("[a-zA-Z][a-zA-Z0-9]*");
    }


    public void iniciarBloque() {
        tablaBloque = new HashMap<>();
    }

    public void terminarBloque(boolean seEjecuta) {
        if (tablaBloque == null) return;

        if (seEjecuta) {
            // commit: lo declarado en el bloque pasa a existir globalmente
            tablaGlobal.putAll(tablaBloque);
        }
        // rollback implícito si no se ejecuta
        tablaBloque = null;
    }


    public String gramaticaDeLinea(String linea) {
        String s = limpiar(linea);
        if (s.isEmpty()) return "";

        if (esLineaCierreMasSino(s)) {
            return "(<corchete> : <bloque> : <sino> : <corchete>)";
        }
        if (esLineaSi(s)) {
            return "(<bloque> : <si> : <parentesis> : <expresion> : <identificador> : <operador> : <identificador> : <parentesis> : <corchete>)";
        }
        if (esLineaSino(s)) {
            return "(<corchete> : <bloque> : <sino> : <corchete>)";
        }
        if (esSoloLlaveApertura(s) || esSoloLlaveCierre(s)) {
            return "(<corchete>)";
        }

        String expr = "";
        try {
            String[] partes = s.split("\\$");
            expr = (partes.length > 1) ? partes[1].trim() : "";
        } catch (Exception e) {
            expr = "";
        }

        String claseLiteral = "<expresion>";
        if (expr.matches("\\d+") || expr.matches("\\d+\\.\\d+")) claseLiteral = "<numeros>";
        else if (expr.startsWith("\"") && expr.endsWith("\"")) claseLiteral = "<cadena>";

        return "(<expresion> : <identificador> : <tipo de dato> : <asignacion> : <expresion> : "
                + claseLiteral + " : " + expr + " : <break>)";
    }


    public void validarSiHeader(String linea) throws Exception {
        String s = limpiar(linea);

        if (!s.startsWith(KW_SI)) throw new ExcepcionSintactica("Se esperaba 'si (...) {'.");
        int p1 = s.indexOf('(');
        int p2 = s.lastIndexOf(')');
        if (p1 < 0 || p2 < 0 || p2 < p1) throw new ExcepcionSintactica("Formato inválido. Se espera: si (a > b) {");
        if (!s.substring(p2).contains("{")) throw new ExcepcionSintactica("Formato inválido: falta '{' para abrir el bloque.");

        String cond = s.substring(p1 + 1, p2).trim();
        if (cond.isEmpty()) throw new ExcepcionSintactica("Condición vacía en 'si'.");
    }

    public void validarSinoHeader(String linea) throws Exception {
        String s = limpiar(linea);
        if (!s.startsWith(KW_SINO)) throw new ExcepcionSintactica("Se esperaba 'sino {'.");
        if (!s.contains("{")) throw new ExcepcionSintactica("Formato inválido. Se espera: sino {");
    }

    public boolean evaluarCondicionSi(String lineaSi) throws Exception {
        String s = limpiar(lineaSi);
        validarSiHeader(s);

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
        if (opEncontrado == null) throw new ExcepcionSintactica("Operador esperado: == != < <= > >=");

        String izq = cond.substring(0, idx).trim();
        String der = cond.substring(idx + opEncontrado.length()).trim();

        Object a = resolverValor(izq, null);
        Object b = resolverValor(der, null);

        if (!a.getClass().equals(b.getClass())) {
            throw new ExcepcionSemantica("Comparación inválida: tipos diferentes en condición.");
        }

        if (a instanceof Comp) return compararNumeros(((Comp)a).getValor(), ((Comp)b).getValor(), opEncontrado);
        if (a instanceof Med)  return compararNumeros(((Med)a).getValor(), ((Med)b).getValor(), opEncontrado);
        if (a instanceof Pal)  return compararStrings(((Pal)a).getValor(), ((Pal)b).getValor(), opEncontrado);

        throw new ExcepcionSemantica("Tipo desconocido en condición.");
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

    public String procesarAsignacion(String linea) throws Exception {
        String s = limpiar(linea);
        if (s.isEmpty()) return "";

        if (!s.contains(SIMBOLO_ASIGNACION)) {
            throw new ExcepcionSintactica("Formato inválido. Se espera: identificador tipo $ expresión");
        }
        if (contarCaracter(s, '$') != 1) {
            throw new ExcepcionSintactica("Debe haber exactamente una '$' en la asignación.");
        }

        String[] partes = s.split("\\$");
        String ladoIzq = partes[0].trim();
        String expr = (partes.length > 1) ? partes[1].trim() : "";

        if (expr.isEmpty()) throw new ExcepcionSintactica("Falta valor después de '$'.");

        String[] dec = ladoIzq.split("\\s+");
        if (dec.length != 2) throw new ExcepcionSintactica("Lado izquierdo inválido. Se espera: identificador tipo");

        String id = dec[0].trim();
        String tipo = dec[1].trim();

        if (!esIdentificador(id)) throw new ExcepcionSintactica("Identificador inválido: '" + id + "'");

        // Rededeclaración en lo que “ya existe” + lo que se está declarando en el bloque actual
        if (tablaGlobal.containsKey(id) || (tablaBloque != null && tablaBloque.containsKey(id))) {
            throw new ExcepcionSemantica("La variable '" + id + "' ya fue declarada previamente.");
        }

        if (!tipo.equals("comp") && !tipo.equals("med") && !tipo.equals("pal")) {
            throw new ExcepcionSemantica("Tipo de dato desconocido: '" + tipo + "'. Tipos válidos: comp, med, pal.");
        }

        Object valorFinal = evaluarExpresion(expr, tipo);

        // Si estamos dentro de un bloque, declaramos “temporal”. Si no, declaramos global.
        if (tablaBloque != null) tablaBloque.put(id, valorFinal);
        else tablaGlobal.put(id, valorFinal);

        return "-> OK: " + id + " $ " + valorComoString(valorFinal) + " [" + tipo + "]";
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
            Object op1 = resolverValor(ops[0].trim(), tipoEsperado);
            Object op2 = resolverValor(ops[1].trim(), tipoEsperado);
            return ejecutarOperacion(op1, op2, operador);
        }

        return resolverValor(expresion.trim(), tipoEsperado);
    }

    private Object resolverValor(String token, String tipoEsperado) throws Exception {
        token = token.trim();

        // Buscar primero en el bloque (si existe), luego en global
        if (tablaBloque != null && tablaBloque.containsKey(token)) {
            Object var = tablaBloque.get(token);
            if (tipoEsperado != null) validarTipo(token, var, tipoEsperado);
            return var;
        }
        if (tablaGlobal.containsKey(token)) {
            Object var = tablaGlobal.get(token);
            if (tipoEsperado != null) validarTipo(token, var, tipoEsperado);
            return var;
        }

        if (esIdentificador(token)) {
            throw new ExcepcionSemantica("Variable no declarada: '" + token + "'.");
        }

        if (tipoEsperado == null) {
            if (token.startsWith("\"") && token.endsWith("\"")) return new Pal(token);
            if (token.matches("\\d+\\.\\d+")) return new Med(token);
            if (token.matches("\\d+")) return new Comp(token);
            throw new ExcepcionSemantica("Valor inválido: '" + token + "'.");
        }

        switch (tipoEsperado) {
            case "comp": return new Comp(token);
            case "med": return new Med(token);
            case "pal": return new Pal(token);
            default: throw new ExcepcionSemantica("Tipo de dato desconocido: " + tipoEsperado);
        }
    }

    private void validarTipo(String nombre, Object var, String tipoEsperado) throws ExcepcionSemantica {
        if (tipoEsperado.equals("comp") && !(var instanceof Comp)) throw new ExcepcionSemantica("'" + nombre + "' no es tipo 'comp'.");
        if (tipoEsperado.equals("med") && !(var instanceof Med)) throw new ExcepcionSemantica("'" + nombre + "' no es tipo 'med'.");
        if (tipoEsperado.equals("pal") && !(var instanceof Pal)) throw new ExcepcionSemantica("'" + nombre + "' no es tipo 'pal'.");
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

    private String valorComoString(Object v) {
        if (v instanceof Comp) return String.valueOf(((Comp) v).getValor());
        if (v instanceof Med) return String.valueOf(((Med) v).getValor());
        if (v instanceof Pal) return "\"" + ((Pal) v).getValor() + "\"";
        return String.valueOf(v);
    }

    private int contarCaracter(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == c) count++;
        return count;
    }

    public List<FilaToken> obtenerTokensLexicos(String linea) {
        List<FilaToken> out = new ArrayList<>();
        String s = limpiar(linea);
        if (s.isEmpty()) return out;

        Matcher m = Pattern.compile(REGEX_LEXER).matcher(s);

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
                    case "si": patron = "\\bsi\\b"; break;
                    case "sino": patron = "\\bsino\\b"; break;
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
}
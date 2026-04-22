package com.analizador;

import java_cup.runtime.*;

%%

%class Lexer
%unicode
%cup
%line
%column
%public

%{
  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}

%%

// Palabras reservadas
"if"                      { return symbol(sym.IF); }
"else"                    { return symbol(sym.ELSE); }
"while"                   { return symbol(sym.WHILE); }

// Números
[0-9]+                    { return symbol(sym.NUMERO, Integer.parseInt(yytext())); }

// Identificadores
[a-zA-Z_][a-zA-Z0-9_]*   { return symbol(sym.ID, yytext()); }

// Operadores
"+"                       { return symbol(sym.MAS); }
"-"                       { return symbol(sym.MENOS); }
"*"                       { return symbol(sym.POR); }
"/"                       { return symbol(sym.DIV); }
"="                       { return symbol(sym.ASIGN); }
"=="                      { return symbol(sym.IGUAL); }

// Delimitadores
"("                       { return symbol(sym.LPAREN); }
")"                       { return symbol(sym.RPAREN); }
";"                       { return symbol(sym.PUNTOYCOMA); }

// Ignorar espacios y saltos de línea
[ \t\n\r]+                { /* ignorar */ }

// Cualquier otro carácter
.                         { System.err.println("Carácter no reconocido: " + yytext()); }
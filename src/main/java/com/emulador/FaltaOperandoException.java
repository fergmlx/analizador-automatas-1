/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.emulador;

/**
 *
 * @author Fer
 */
public class FaltaOperandoException extends Exception {
    public FaltaOperandoException(String linea) {
        super("ERROR: Falta un operando en la expresión -> '" + linea + "'");
    }
}

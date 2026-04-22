/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.emulador;

/**
 *
 * @author Fer
 */
public class FormatoInvalidoException extends Exception {
    public FormatoInvalidoException(String tipo, String valor) {
        super("ERROR SEMÁNTICO: El valor '" + valor + "' no cumple con el formato o límite de '" + tipo + "'.");
    }
}

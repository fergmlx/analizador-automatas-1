/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.emulador;

/**
 *
 * @author Fer
 */
public class TiposIncompatiblesException extends Exception {
    public TiposIncompatiblesException(String mensaje) {
        super("ERROR SEMÁNTICO: Incompatibilidad de tipos. " + mensaje);
    }
}

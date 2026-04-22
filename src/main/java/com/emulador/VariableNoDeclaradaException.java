/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.emulador;

/**
 *
 * @author Fer
 */
public class VariableNoDeclaradaException extends Exception {
    public VariableNoDeclaradaException(String variable) {
        super("ERROR SEMÁNTICO: La variable '" + variable + "' no ha sido declarada.");
    }
}
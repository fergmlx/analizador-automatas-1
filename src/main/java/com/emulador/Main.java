package com.emulador;

public class Main {
    public static void main(String[] args) {
        //Analizador analizador = new Analizador();
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Principal().setVisible(true);
            }
        });
    }   
}
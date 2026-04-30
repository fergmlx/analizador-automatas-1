package com.emulador;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            FlatMacLightLaf.setup();
            UIManager.put("Button.arc", 999);
            UIManager.put("TextComponent.arc", 55);
            UIManager.put("Component.arc", 55);
            UIManager.put("TextField.margin", new Insets(5, 10, 5, 5));
            UIManager.put("PasswordField.margin", new Insets(5, 5, 5, 5));
            UIManager.put("defaultFont", new Font("Poppins", Font.PLAIN, 13));
            
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new Principal().setVisible(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }   
}
package main;

import boundary.MainLoginPage;

import javax.swing.SwingUtilities;

public class DentalCareSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainLoginPage().setVisible(true));
    }
}

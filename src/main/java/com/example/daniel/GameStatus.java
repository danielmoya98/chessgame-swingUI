package com.example.daniel;

import javax.swing.*;
import java.awt.*;

public class GameStatus extends JPanel {
    private boolean whiteTurn = true;
    private boolean check = false;
    private JLabel statusLabel;

    public GameStatus() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        statusLabel = new JLabel("Turno: Blancas");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(statusLabel);
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public void toggleTurn() {
        whiteTurn = !whiteTurn;
        updateStatus();
    }

    // Agregar este método
    public void setTurn(boolean isWhiteTurn) {
        whiteTurn = isWhiteTurn;
        updateStatus();
    }

    public void setCheck(boolean check) {
        this.check = check;
        updateStatus();
    }

    public void reset() {
        whiteTurn = true;
        check = false;
        updateStatus();
    }

    private void updateStatus() {
        String status = "Turno: " + (whiteTurn ? "Blancas" : "Negras");
        if (check) {
            status += " - ¡JAQUE!";
        }
        statusLabel.setText(status);
    }
}
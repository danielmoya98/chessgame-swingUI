package com.example.daniel;

import javax.swing.*;
import java.awt.*;

public class ScoreBoard extends JPanel {
    private int whiteScore = 0;
    private int blackScore = 0;
    private JLabel scoreLabel;

    public ScoreBoard() {
        setBorder(BorderFactory.createTitledBorder("Puntuación"));
        setLayout(new FlowLayout(FlowLayout.CENTER));
        scoreLabel = new JLabel(String.format("Blancas: %d - Negras: %d", whiteScore, blackScore));
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(scoreLabel);
    }

    public void addPoints(boolean isWhite, int points) {
        if (isWhite) {
            whiteScore += points;
        } else {
            blackScore += points;
        }
        updateScore();
    }

    public void reset() {
        whiteScore = 0;
        blackScore = 0;
        updateScore();
    }

    private void updateScore() {
        scoreLabel.setText(String.format("Blancas: %d - Negras: %d", whiteScore, blackScore));
    }
}
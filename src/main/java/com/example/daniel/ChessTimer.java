package com.example.daniel;

import javax.swing.*;
import java.awt.*;

public class ChessTimer extends JPanel {
    private static final int INITIAL_TIME = 600; // 10 minutos
    private JLabel whiteTimeLabel;
    private JLabel blackTimeLabel;
    private Timer whiteTimer;
    private Timer blackTimer;
    private int whiteSeconds;
    private int blackSeconds;

    public ChessTimer() {
        setLayout(new GridLayout(2, 1));
        setBorder(BorderFactory.createTitledBorder("Tiempo"));

        whiteTimeLabel = new JLabel("Blancas: 10:00");
        blackTimeLabel = new JLabel("Negras: 10:00");

        add(whiteTimeLabel);
        add(blackTimeLabel);

        setupTimers();
        resetTime();
    }

    private void setupTimers() {
        whiteTimer = new Timer(1000, e -> updateWhiteTime());
        blackTimer = new Timer(1000, e -> updateBlackTime());
    }

    private void resetTime() {
        whiteSeconds = INITIAL_TIME;
        blackSeconds = INITIAL_TIME;
        updateLabels();
    }

    public void startTimers() {
        whiteTimer.start();
    }

    public void stopTimers() {
        if (whiteTimer != null) whiteTimer.stop();
        if (blackTimer != null) blackTimer.stop();
    }

    public void toggleTimers() {
        if (whiteTimer.isRunning()) {
            whiteTimer.stop();
            blackTimer.start();
        } else {
            blackTimer.stop();
            whiteTimer.start();
        }
    }

    public void setTimes(int whiteTime, int blackTime) {
        this.whiteSeconds = whiteTime;
        this.blackSeconds = blackTime;
        updateLabels();
    }

    public int getWhiteTime() {
        return whiteSeconds;
    }

    public int getBlackTime() {
        return blackSeconds;
    }

    private void updateWhiteTime() {
        if (whiteSeconds > 0) {
            whiteSeconds--;
            updateLabels();
            if (whiteSeconds == 0) {
                timeUp(true);
            }
        }
    }

    private void updateBlackTime() {
        if (blackSeconds > 0) {
            blackSeconds--;
            updateLabels();
            if (blackSeconds == 0) {
                timeUp(false);
            }
        }
    }

    private void updateLabels() {
        whiteTimeLabel.setText(String.format("Blancas: %02d:%02d", whiteSeconds / 60, whiteSeconds % 60));
        blackTimeLabel.setText(String.format("Negras: %02d:%02d", blackSeconds / 60, blackSeconds % 60));
    }

    private void timeUp(boolean whiteTimerUp) {
        stopTimers();
        String winner = whiteTimerUp ? "Negras" : "Blancas";
        JOptionPane.showMessageDialog(this,
                "¡Tiempo agotado! Ganan las " + winner,
                "Fin del juego",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void reset() {
        stopTimers();
        resetTime();
    }
}
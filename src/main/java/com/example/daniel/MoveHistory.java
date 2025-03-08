package com.example.daniel;

import javax.swing.*;
import java.awt.*;

public class MoveHistory extends JPanel {
    private JTextArea historyArea;

    public MoveHistory() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Historial de Movimientos"));

        historyArea = new JTextArea(10, 20);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(historyArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addMove(String move) {
        historyArea.append(move + "\n");
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
    }

    public void clear() {
        historyArea.setText("");
    }
}
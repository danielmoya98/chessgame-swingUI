package com.example.daniel;

public class App {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ChessBoard board = new ChessBoard();
            board.setVisible(true);
        });
    }
}
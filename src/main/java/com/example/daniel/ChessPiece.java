package com.example.daniel;

import java.io.Serializable;

public class ChessPiece implements Serializable {
    private String type;
    private boolean isWhite;

    public ChessPiece(String type, boolean isWhite) {
        this.type = type;
        this.isWhite = isWhite;
    }

    public String getType() {
        return type;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public String getSymbol() {
        return switch (type.toLowerCase()) {
            case "king" -> isWhite ? "♔" : "♚";
            case "queen" -> isWhite ? "♕" : "♛";
            case "rook" -> isWhite ? "♖" : "♜";
            case "bishop" -> isWhite ? "♗" : "♝";
            case "knight" -> isWhite ? "♘" : "♞";
            case "pawn" -> isWhite ? "♙" : "♟";
            default -> "";
        };
    }
}
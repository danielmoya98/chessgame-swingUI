package com.example.daniel;

import java.awt.Point;
import java.io.Serializable;

public class Move implements Serializable {
    private Point from;
    private Point to;
    private ChessPiece movedPiece;
    private ChessPiece capturedPiece;

    public Move(Point from, Point to, ChessPiece movedPiece, ChessPiece capturedPiece) {
        this.from = from;
        this.to = to;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
    }

    public Point getFrom() {
        return from;
    }

    public Point getTo() {
        return to;
    }

    public ChessPiece getMovedPiece() {
        return movedPiece;
    }

    public ChessPiece getCapturedPiece() {
        return capturedPiece;
    }
}
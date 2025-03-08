package com.example.daniel;

import java.io.Serializable;
import java.util.Stack;

public class GameState implements Serializable {
    private ChessPiece[][] pieces;
    private Stack<Move> moveHistory;
    private boolean whiteTurn;
    private int whiteTime;
    private int blackTime;

    public GameState(ChessPiece[][] pieces, Stack<Move> moveHistory,
                    boolean whiteTurn, int whiteTime, int blackTime) {
        this.pieces = pieces;
        this.moveHistory = moveHistory;
        this.whiteTurn = whiteTurn;
        this.whiteTime = whiteTime;
        this.blackTime = blackTime;
    }

    public ChessPiece[][] getPieces() { return pieces; }
    public Stack<Move> getMoveHistory() { return moveHistory; }
    public boolean isWhiteTurn() { return whiteTurn; }
    public int getWhiteTime() { return whiteTime; }
    public int getBlackTime() { return blackTime; }
}
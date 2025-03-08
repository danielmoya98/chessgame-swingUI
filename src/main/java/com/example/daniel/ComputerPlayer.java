package com.example.daniel;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class ComputerPlayer {
    private int difficulty;
    private Random random;

    public ComputerPlayer(int difficulty) {
        this.difficulty = difficulty;
        this.random = new Random();
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public Point[] selectMove(List<Point[]> possibleMoves) {
        if (possibleMoves.isEmpty()) {
            return null;
        }
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }
}
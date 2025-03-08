package com.example.daniel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import javax.sound.sampled.*;

public class ChessBoard extends JFrame {
    private final int BOARD_SIZE = 8;
    private final int TILE_SIZE = 75;
    private JLabel[][] boardSquares = new JLabel[BOARD_SIZE][BOARD_SIZE];
    private ChessPiece[][] pieces = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
    private Point selectedPiece = null;
    private GameStatus gameStatus;
    private Stack<Move> moveHistory = new Stack<>();
    private JPanel capturedPiecesPanel;
    private MoveHistory moveHistoryPanel;
    private ChessTimer timer;
    private ScoreBoard scoreBoard;
    private boolean soundEnabled = true;
    private ComputerPlayer computerPlayer;
    private boolean vsComputer = false;
    private Timer computerMoveTimer;
    private List<Point> possibleMoves = new ArrayList<>();
    private final Color HIGHLIGHT_COLOR = new Color(119, 185, 125, 128);
    private final Color SELECTED_COLOR = new Color(255, 255, 0, 128);


    public ChessBoard() {
        setTitle("Ajedrez");
        setSize(BOARD_SIZE * TILE_SIZE + 300, (BOARD_SIZE * TILE_SIZE) + 120);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setupMenuBar();
        setupSidePanels();
        setupComputerMoveTimer();

        JPanel mainPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        setupBoard(mainPanel);

        gameStatus = new GameStatus();
        add(mainPanel, BorderLayout.CENTER);
        add(gameStatus, BorderLayout.SOUTH);

        computerPlayer = new ComputerPlayer(1);
        initializePieces();
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menú Juego
        JMenu gameMenu = new JMenu("Juego");
        addMenuItem(gameMenu, "Nuevo Juego", e -> resetGame());
        addMenuItem(gameMenu, "Deshacer Movimiento", e -> undoLastMove());
        gameMenu.addSeparator();
        addMenuItem(gameMenu, "Guardar Partida", e -> saveGame());
        addMenuItem(gameMenu, "Cargar Partida", e -> loadGame());

        // Menú Opciones
        JMenu optionsMenu = new JMenu("Opciones");
        JCheckBoxMenuItem soundItem = new JCheckBoxMenuItem("Sonidos", true);
        soundItem.addActionListener(e -> soundEnabled = soundItem.isSelected());
        optionsMenu.add(soundItem);

        // Menú Modo de Juego
        JMenu modeMenu = new JMenu("Modo de Juego");
        ButtonGroup modeGroup = new ButtonGroup();
        JRadioButtonMenuItem pvpItem = new JRadioButtonMenuItem("Jugador vs Jugador", true);
        JRadioButtonMenuItem pvcItem = new JRadioButtonMenuItem("Jugador vs Computadora", false);

        pvpItem.addActionListener(e -> setComputerMode(false));
        pvcItem.addActionListener(e -> setComputerMode(true));

        modeGroup.add(pvpItem);
        modeGroup.add(pvcItem);
        modeMenu.add(pvpItem);
        modeMenu.add(pvcItem);

        // Menú Dificultad
        JMenu difficultyMenu = new JMenu("Dificultad");
        ButtonGroup diffGroup = new ButtonGroup();
        String[] difficulties = {"Fácil", "Medio", "Difícil"};
        for (int i = 0; i < difficulties.length; i++) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(difficulties[i], i == 0);
            final int level = i + 1;
            item.addActionListener(e -> computerPlayer.setDifficulty(level));
            diffGroup.add(item);
            difficultyMenu.add(item);
        }

        menuBar.add(gameMenu);
        menuBar.add(optionsMenu);
        menuBar.add(modeMenu);
        menuBar.add(difficultyMenu);
        setJMenuBar(menuBar);
    }

    private void setupSidePanels() {
        // Panel derecho
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        // Temporizador
        timer = new ChessTimer();
        rightPanel.add(timer);

        // Marcador
        scoreBoard = new ScoreBoard();
        rightPanel.add(scoreBoard);

        // Historial de movimientos
        moveHistoryPanel = new MoveHistory();
        rightPanel.add(moveHistoryPanel);

        add(rightPanel, BorderLayout.EAST);

        // Panel de piezas capturadas
        setupCapturedPiecesPanel();
    }

    private void addMenuItem(JMenu menu, String text, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        menu.add(item);
    }

    private void setComputerMode(boolean enabled) {
        vsComputer = enabled;
        resetGame();
        if (enabled && !gameStatus.isWhiteTurn()) {
            computerMoveTimer.start();
        }
    }

    private void playSound(String soundType) {
        if (!soundEnabled) return;
        try {
            String soundFile = switch (soundType) {
                case "move" -> "/sounds/move.wav";
                case "capture" -> "/sounds/capture.wav";
                case "check" -> "/sounds/check.wav";
                default -> "/sounds/move.wav";
            };

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                    getClass().getResource(soundFile));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error al reproducir sonido: " + e.getMessage());
        }
    }

    private void saveGame() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(fileChooser.getSelectedFile()))) {
                GameState state = new GameState(pieces, moveHistory,
                        gameStatus.isWhiteTurn(), timer.getWhiteTime(), timer.getBlackTime());
                out.writeObject(state);
                JOptionPane.showMessageDialog(this, "Partida guardada correctamente");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al guardar la partida: " + e.getMessage());
            }
        }
    }

    private void loadGame() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(fileChooser.getSelectedFile()))) {
                GameState state = (GameState) in.readObject();
                pieces = state.getPieces();
                moveHistory = state.getMoveHistory();
                gameStatus.setTurn(state.isWhiteTurn());
                timer.setTimes(state.getWhiteTime(), state.getBlackTime());
                updateAllSquares();
                JOptionPane.showMessageDialog(this, "Partida cargada correctamente");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al cargar la partida: " + e.getMessage());
            }
        }
    }

    private void updateAllSquares() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                updateSquare(row, col);
                resetSquareColor(row, col);
            }
        }
    }

    private void setupCapturedPiecesPanel() {
        capturedPiecesPanel = new JPanel();
        capturedPiecesPanel.setLayout(new FlowLayout());
        capturedPiecesPanel.setBorder(BorderFactory.createTitledBorder("Captured Pieces"));
        add(capturedPiecesPanel, BorderLayout.NORTH);
    }

    private void setupBoard(JPanel mainPanel) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boardSquares[row][col] = new JLabel();
                boardSquares[row][col].setOpaque(true);
                boardSquares[row][col].setHorizontalAlignment(JLabel.CENTER);
                boardSquares[row][col].setFont(new Font("Segoe UI Symbol", Font.PLAIN, 48));

                if ((row + col) % 2 == 0) {
                    boardSquares[row][col].setBackground(new Color(240, 217, 181));
                } else {
                    boardSquares[row][col].setBackground(new Color(181, 136, 99));
                }

                final int r = row;
                final int c = col;
                boardSquares[row][col].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleSquareClick(r, c);
                    }
                });

                mainPanel.add(boardSquares[row][col]);
            }
        }
    }

    private void setupComputerMoveTimer() {
        computerMoveTimer = new Timer(500, e -> {
            if (vsComputer && !gameStatus.isWhiteTurn()) {
                makeComputerMove();
            }
        });
        computerMoveTimer.setRepeats(false);
    }

    private void makeComputerMove() {
        List<Point[]> possibleMoves = new ArrayList<>();

        // Recopilar todos los movimientos posibles
        for (int fromRow = 0; fromRow < BOARD_SIZE; fromRow++) {
            for (int fromCol = 0; fromCol < BOARD_SIZE; fromCol++) {
                if (pieces[fromRow][fromCol] != null && !pieces[fromRow][fromCol].isWhite()) {
                    for (int toRow = 0; toRow < BOARD_SIZE; toRow++) {
                        for (int toCol = 0; toCol < BOARD_SIZE; toCol++) {
                            if (isValidMove(fromRow, fromCol, toRow, toCol) &&
                                    isPathClear(fromRow, fromCol, toRow, toCol)) {
                                Point[] move = new Point[] {
                                    new Point(fromRow, fromCol),
                                    new Point(toRow, toCol)
                                };
                                possibleMoves.add(move);
                            }
                        }
                    }
                }
            }
        }

        if (!possibleMoves.isEmpty()) {
            Point[] move = computerPlayer.selectMove(possibleMoves);
            if (move != null) {
                Point from = move[0];
                Point to = move[1];

                ChessPiece capturedPiece = pieces[to.x][to.y];
                ChessPiece movedPiece = pieces[from.x][from.y];

                moveHistory.push(new Move(from, to, movedPiece, capturedPiece));
                movePiece(from.x, from.y, to.x, to.y);

                if (capturedPiece != null) {
                    addCapturedPiece(capturedPiece);
                    playSound("capture");
                } else {
                    playSound("move");
                }

                if (isInCheck(true)) {
                    gameStatus.setCheck(true);
                    playSound("check");
                } else {
                    gameStatus.setCheck(false);
                }

                gameStatus.toggleTurn();
            }
        }
    }

    private void initializePieces() {
        // Initialize pawns
        for (int col = 0; col < BOARD_SIZE; col++) {
            pieces[1][col] = new ChessPiece("pawn", false);
            pieces[6][col] = new ChessPiece("pawn", true);
            updateSquare(1, col);
            updateSquare(6, col);
        }

        // Initialize other pieces
        String[] backRow = {"rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"};
        for (int col = 0; col < BOARD_SIZE; col++) {
            pieces[0][col] = new ChessPiece(backRow[col], false);
            pieces[7][col] = new ChessPiece(backRow[col], true);
            updateSquare(0, col);
            updateSquare(7, col);
        }
    }

    private void handleSquareClick(int row, int col) {
        if (vsComputer && !gameStatus.isWhiteTurn()) {
            return;
        }

        if (selectedPiece == null) {
            if (pieces[row][col] != null && pieces[row][col].isWhite() == gameStatus.isWhiteTurn()) {
                selectedPiece = new Point(row, col);
                boardSquares[row][col].setBackground(SELECTED_COLOR);
                showPossibleMoves(row, col);
            }
        } else {
            int oldRow = (int) selectedPiece.getX();
            int oldCol = (int) selectedPiece.getY();

            if (isValidMove(oldRow, oldCol, row, col) && isPathClear(oldRow, oldCol, row, col)) {
                makeMove(oldRow, oldCol, row, col);
                if (vsComputer && !gameStatus.isWhiteTurn()) {
                    computerMoveTimer.start();
                }
            }

            clearHighlights();
            selectedPiece = null;
        }
    }

    private void showPossibleMoves(int fromRow, int fromCol) {
        possibleMoves.clear();

        for (int toRow = 0; toRow < BOARD_SIZE; toRow++) {
            for (int toCol = 0; toCol < BOARD_SIZE; toCol++) {
                if (isValidMove(fromRow, fromCol, toRow, toCol) &&
                        isPathClear(fromRow, fromCol, toRow, toCol)) {
                    possibleMoves.add(new Point(toRow, toCol));
                    highlightSquare(toRow, toCol);
                }
            }
        }
    }

    private void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece capturedPiece = pieces[toRow][toCol];
        ChessPiece movedPiece = pieces[fromRow][fromCol];

        moveHistory.push(new Move(
                new Point(fromRow, fromCol),
                new Point(toRow, toCol),
                movedPiece,
                capturedPiece
        ));

        movePiece(fromRow, fromCol, toRow, toCol);

        if (capturedPiece != null) {
            addCapturedPiece(capturedPiece);
            playSound("capture");
        } else {
            playSound("move");
        }

        if (isInCheck(!gameStatus.isWhiteTurn())) {
            gameStatus.setCheck(true);
            playSound("check");
        } else {
            gameStatus.setCheck(false);
        }

        gameStatus.toggleTurn();
    }

    private void highlightSquare(int row, int col) {
        JLabel square = boardSquares[row][col];
        square.setOpaque(true);
        square.setBackground(HIGHLIGHT_COLOR);
    }

    private void clearHighlights() {
        for (Point move : possibleMoves) {
            resetSquareColor((int)move.getX(), (int)move.getY());
        }
        if (selectedPiece != null) {
            resetSquareColor((int)selectedPiece.getX(), (int)selectedPiece.getY());
        }
        possibleMoves.clear();
    }


    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece piece = pieces[fromRow][fromCol];
        ChessPiece targetPiece = pieces[toRow][toCol];

        if (targetPiece != null && targetPiece.isWhite() == piece.isWhite()) {
            return false;
        }

        return switch (piece.getType().toLowerCase()) {
            case "pawn" -> isValidPawnMove(fromRow, fromCol, toRow, toCol);
            case "rook" -> isValidRookMove(fromRow, fromCol, toRow, toCol);
            case "knight" -> isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case "bishop" -> isValidBishopMove(fromRow, fromCol, toRow, toCol);
            case "queen" -> isValidQueenMove(fromRow, fromCol, toRow, toCol);
            case "king" -> isValidKingMove(fromRow, fromCol, toRow, toCol);
            default -> false;
        };
    }

    private boolean isValidPawnMove(int fromRow, int fromCol, int toRow, int toCol) {
        boolean isWhite = pieces[fromRow][fromCol].isWhite();
        int direction = isWhite ? -1 : 1;
        int startRow = isWhite ? 6 : 1;

        if (fromCol == toCol && toRow == fromRow + direction && pieces[toRow][toCol] == null) {
            return true;
        }

        if (fromRow == startRow && fromCol == toCol &&
                toRow == fromRow + (2 * direction) &&
                pieces[toRow][toCol] == null &&
                pieces[fromRow + direction][toCol] == null) {
            return true;
        }

        if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + direction &&
                pieces[toRow][toCol] != null &&
                pieces[toRow][toCol].isWhite() != isWhite) {
            return true;
        }

        return false;
    }

    private boolean isValidRookMove(int fromRow, int fromCol, int toRow, int toCol) {
        return fromRow == toRow || fromCol == toCol;
    }

    private boolean isValidKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        return (Math.abs(fromRow - toRow) == 2 && Math.abs(fromCol - toCol) == 1) ||
                (Math.abs(fromRow - toRow) == 1 && Math.abs(fromCol - toCol) == 2);
    }

    private boolean isValidBishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        return Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol);
    }

    private boolean isValidQueenMove(int fromRow, int fromCol, int toRow, int toCol) {
        return isValidRookMove(fromRow, fromCol, toRow, toCol) ||
                isValidBishopMove(fromRow, fromCol, toRow, toCol);
    }

    private boolean isValidKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        return Math.abs(fromRow - toRow) <= 1 && Math.abs(fromCol - toCol) <= 1;
    }

    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        if (pieces[fromRow][fromCol].getType().equalsIgnoreCase("knight")) {
            return true;
        }

        int rowStep = Integer.compare(toRow - fromRow, 0);
        int colStep = Integer.compare(toCol - fromCol, 0);

        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;

        while (currentRow != toRow || currentCol != toCol) {
            if (pieces[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        return true;
    }

    private boolean isInCheck(boolean whiteKing) {
        Point kingPos = findKing(whiteKing);
        if (kingPos == null) return false;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = pieces[row][col];
                if (piece != null && piece.isWhite() != whiteKing) {
                    if (isValidMove(row, col, kingPos.x, kingPos.y) &&
                            isPathClear(row, col, kingPos.x, kingPos.y)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Point findKing(boolean isWhite) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = pieces[row][col];
                if (piece != null && piece.getType().equals("king") &&
                        piece.isWhite() == isWhite) {
                    return new Point(row, col);
                }
            }
        }
        return null;
    }

    private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        pieces[toRow][toCol] = pieces[fromRow][fromCol];
        pieces[fromRow][fromCol] = null;
        updateSquare(toRow, toCol);
        updateSquare(fromRow, fromCol);
    }

    private void addCapturedPiece(ChessPiece piece) {
        JLabel pieceLabel = new JLabel(piece.getSymbol());
        pieceLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
        capturedPiecesPanel.add(pieceLabel);
        capturedPiecesPanel.revalidate();

        // Agregar puntos basados en el tipo de pieza capturada
        int points = switch (piece.getType().toLowerCase()) {
            case "queen" -> 9;
            case "rook" -> 5;
            case "bishop", "knight" -> 3;
            case "pawn" -> 1;
            default -> 0;
        };
        scoreBoard.addPoints(gameStatus.isWhiteTurn(), points);
    }

    private void resetGame() {
        // Reset game state
        pieces = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        moveHistory.clear();
        selectedPiece = null;
        possibleMoves.clear();

        // Reset UI components
        capturedPiecesPanel.removeAll();
        capturedPiecesPanel.revalidate();
        capturedPiecesPanel.repaint();
        scoreBoard.reset();
        gameStatus.reset();
        timer.reset();

        // Reset board colors and pieces
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                resetSquareColor(row, col);
                boardSquares[row][col].setText("");
            }
        }

        // Initialize new pieces
        initializePieces();
        updateAllSquares();

        // Start computer if needed
        if (vsComputer && !gameStatus.isWhiteTurn()) {
            computerMoveTimer.start();
        }
    }



    private void undoLastMove() {
        if (!moveHistory.isEmpty()) {
            Move lastMove = moveHistory.pop();
            Point from = lastMove.getFrom();
            Point to = lastMove.getTo();

            pieces[from.x][from.y] = lastMove.getMovedPiece();
            pieces[to.x][to.y] = lastMove.getCapturedPiece();

            updateSquare(from.x, from.y);
            updateSquare(to.x, to.y);

            if (lastMove.getCapturedPiece() != null) {
                capturedPiecesPanel.remove(capturedPiecesPanel.getComponentCount() - 1);
                capturedPiecesPanel.revalidate();
                capturedPiecesPanel.repaint();
            }

            gameStatus.toggleTurn();
            gameStatus.setCheck(false);
        }
    }

    private void updateSquare(int row, int col) {
        if (pieces[row][col] != null) {
            boardSquares[row][col].setText(pieces[row][col].getSymbol());
        } else {
            boardSquares[row][col].setText("");
        }
    }

    private void resetSquareColor(int row, int col) {
        if ((row + col) % 2 == 0) {
            boardSquares[row][col].setBackground(new Color(240, 217, 181));
        } else {
            boardSquares[row][col].setBackground(new Color(181, 136, 99));
        }
    }
}
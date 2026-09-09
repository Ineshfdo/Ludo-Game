package game;

import players.components.LudoPiece;
import players.components.PlayerColor;

public class LudoBoard {
    private static LudoBoard instance;

    public static final int STANDARD_PATH_LENGTH = 52;
    public static final int HOME_STRAIGHT_LENGTH = 5;

    private Cell[] standardPath;
    private Cell[] redHomeStraight;
    private Cell[] greenHomeStraight;
    private Cell[] yellowHomeStraight;
    private Cell[] blueHomeStraight;

    private LudoBoard() {
        standardPath = new Cell[STANDARD_PATH_LENGTH];

        for (int cellIndex = 0; cellIndex < STANDARD_PATH_LENGTH; cellIndex++) {
            boolean isStart = (cellIndex == 2 || cellIndex == 15 || cellIndex == 28 || cellIndex == 41);
            boolean isApproach = (cellIndex == 0 || cellIndex == 13 || cellIndex == 26 || cellIndex == 39);
            String cellId = String.valueOf(cellIndex);
            standardPath[cellIndex] = new Cell(cellId, cellIndex, (PlayerColor) null, isStart, isApproach);
        }

        redHomeStraight = initializeHomeStraight(PlayerColor.RED);
        greenHomeStraight = initializeHomeStraight(PlayerColor.GREEN);
        yellowHomeStraight = initializeHomeStraight(PlayerColor.YELLOW);
        blueHomeStraight = initializeHomeStraight(PlayerColor.BLUE);
    }

    private Cell[] initializeHomeStraight(PlayerColor color) {
        Cell[] straight = new Cell[HOME_STRAIGHT_LENGTH];
        String colorPrefix = color.name().substring(0, 1).toUpperCase() + color.name().substring(1).toLowerCase();

        for (int cellIndex = 0; cellIndex < HOME_STRAIGHT_LENGTH; cellIndex++) {
            String cellId = colorPrefix + "home path" + cellIndex;
            straight[cellIndex] = new Cell(cellId, cellIndex, color, false, false);
        }
        return straight;
    }

    public static LudoBoard getInstance() {
        if (instance == null) {
            instance = new LudoBoard();
        }
        return instance;
    }

    public Cell[] getStandardPath() {
        return standardPath;
    }

    public void removePieceFromBoard(LudoPiece piece) {
        if (piece.getState().equals("STANDARD")) {
            standardPath[piece.getPosition()].removePiece(piece);
        } else if (piece.getState().equals("HOME_STRAIGHT")) {
            Cell[] straight = getHomeStraight(piece.getColor());
            straight[piece.getPosition()].removePiece(piece);
        }
    }

    public void removeFromCurrentCell(LudoPiece piece) {
        removePieceFromBoard(piece);
    }

    public Cell[] getHomeStraight(PlayerColor color) {
        switch (color) {
            case RED:
                return redHomeStraight;
            case GREEN:
                return greenHomeStraight;
            case YELLOW:
                return yellowHomeStraight;
            case BLUE:
                return blueHomeStraight;
            default:
                return null;
        }
    }

    public boolean hasPiecesOnStandardPath() {
        for (Cell cell : standardPath) {
            if (!cell.getPieces().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
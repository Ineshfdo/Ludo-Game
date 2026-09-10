package game;

import java.util.ArrayList;
import java.util.List;

import game.players.components.LudoPiece;
import game.players.components.PlayerColor;

// Represents a single square cell on the Ludo board.

public class Cell {
    private String id;
    private int index;
    private PlayerColor color;
    private boolean isStart;
    private boolean isApproach;
    private List<LudoPiece> pieces;

    public Cell(String id, int index, PlayerColor color, boolean isStart, boolean isApproach) {
        this.id = id;
        this.index = index;
        this.color = color;
        this.isStart = isStart;
        this.isApproach = isApproach;
        this.pieces = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public PlayerColor getColor() {
        return color;
    }

    public boolean isStart() {
        return isStart;
    }

    public boolean isApproach() {
        return isApproach;
    }

    public List<LudoPiece> getPieces() {
        return pieces;
    }

    public void addPiece(LudoPiece piece) {
        pieces.add(piece);
    }

    public void removePiece(LudoPiece piece) {
        pieces.remove(piece);
    }
}

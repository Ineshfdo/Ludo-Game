package game.players.components;

import game.states.PieceState;
import game.states.BaseState;

// Represents a single playing piece on the board.

public class LudoPiece {
    private String id;
    private PlayerColor color;

    private PieceState state;

    private int position; // -1 for Base
    private int approachPasses; // Tracks how many times a piece has passed its approach index

    // The 3 explicit directions:
    private boolean xChoiceDirectionClockwise; // True for clockwise, false for counter-clockwise
    private boolean combinedBlockDirectionClockwise;
    private boolean breakBlockDirectionClockwise;

    private int captures; // Number of opponents captured by this piece


    public static final int POSITION_BASE = -1;
    public static final int POSITION_REMOVED = -2;

    public LudoPiece(String id, PlayerColor color) {
        this.id = id;
        this.color = color;
        this.position = POSITION_BASE;
        this.state = new BaseState();
        this.approachPasses = 0;
        this.xChoiceDirectionClockwise = true;
        this.combinedBlockDirectionClockwise = true;
        this.breakBlockDirectionClockwise = true;
        this.captures = 0;
    }

    public String getId() {
        return id;
    }

    public PlayerColor getColor() {
        return color;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public PieceState getState() {
        return state;
    }

    public void setState(PieceState state) {
        this.state = state;
    }

    public boolean isXChoiceDirectionClockwise() {
        return xChoiceDirectionClockwise;
    }

    public void setXChoiceDirectionClockwise(boolean clockwise) {
        this.xChoiceDirectionClockwise = clockwise;
        // Automatically sync the break direction to remember the original X choice
        this.breakBlockDirectionClockwise = clockwise;
    }

    public boolean isCombinedBlockDirectionClockwise() {
        return combinedBlockDirectionClockwise;
    }

    public void setCombinedBlockDirectionClockwise(boolean clockwise) {
        this.combinedBlockDirectionClockwise = clockwise;
    }

    public boolean isBreakBlockDirectionClockwise() {
        return breakBlockDirectionClockwise;
    }

    public void setBreakBlockDirectionClockwise(boolean clockwise) {
        this.breakBlockDirectionClockwise = clockwise;
    }

    public int getApproachPasses() {
        return approachPasses;
    }

    public void setApproachPasses(int approachPasses) {
        this.approachPasses = approachPasses;
    }

    public void incrementApproachPasses() {
        this.approachPasses++;
    }

    public int getCaptures() {
        return captures;
    }

    public void setCaptures(int captures) {
        this.captures = captures;
    }

    public void incrementCaptures() {
        this.captures++;
    }

    public void resetCaptures() {
        this.captures = 0;
    }



    public void resetToDefault() {
        this.state = new BaseState();
        this.position = POSITION_BASE;
        this.xChoiceDirectionClockwise = true;
        this.combinedBlockDirectionClockwise = true;
        this.breakBlockDirectionClockwise = true;
        this.approachPasses = 0;
        this.captures = 0;
    }

    @Override
    public String toString() {
        return "LudoPiece{" +
                "id='" + id + '\'' +
                ", color=" + color +
                ", position=" + position +
                ", state='" + state.getStateName() + '\'' +
                '}';
    }
}
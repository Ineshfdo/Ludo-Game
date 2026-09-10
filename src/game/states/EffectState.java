package game.states;

import game.LudoBoard;
import game.players.components.LudoPiece;

public abstract class EffectState implements PieceState {
    protected PieceState underlyingState;
    protected int roundsRemaining;

    public EffectState(PieceState underlyingState, int roundsRemaining) {
        this.underlyingState = underlyingState;
        this.roundsRemaining = roundsRemaining;
    }

    @Override public boolean isBase() { return underlyingState.isBase(); }
    @Override public boolean isStandard() { return underlyingState.isStandard(); }
    @Override public boolean isHomeStraight() { return underlyingState.isHomeStraight(); }
    @Override public boolean isHome() { return underlyingState.isHome(); }
    @Override public String getStateName() { return underlyingState.getStateName(); }

    @Override
    public void step(LudoPiece piece, int direction) {
        // Temporarily set the piece's state back to underlying so transitions work cleanly
        piece.setState(underlyingState);
        underlyingState.step(piece, direction);
        
        // If underlying state changed (e.g. Standard -> HomeStraight), capture it
        if (piece.getState() != underlyingState) {
            this.underlyingState = piece.getState();
        }
        
        // Restore the wrapper
        piece.setState(this);
    }

    @Override
    public void removeFromBoard(LudoPiece piece, LudoBoard board) {
        underlyingState.removeFromBoard(piece, board);
    }

    @Override
    public void addToBoard(LudoPiece piece, LudoBoard board) {
        underlyingState.addToBoard(piece, board);
    }
}

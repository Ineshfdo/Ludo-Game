package game.states;

import game.players.components.LudoPiece;

public class BriefingState extends EffectState {

    public BriefingState(PieceState underlyingState, int roundsRemaining) {
        super(underlyingState, roundsRemaining);
    }

    @Override
    public int calculateEffectiveRoll(int roll) {
        return roll;
    }

    @Override
    public int calculateBlockEffectiveRoll(int roll) {
        return roll;
    }

    @Override
    public boolean canMove() {
        System.out.println("  -> Piece cannot move (Frozen for " + roundsRemaining + " more rounds).");
        return false;
    }

    @Override
    public void decrementRoundsPreTurn(LudoPiece piece) {
    }

    @Override
    public void decrementRoundsPostTurn(LudoPiece piece) {
        if (roundsRemaining > 0) {
            roundsRemaining--;
            if (roundsRemaining == 0) {
                System.out.println("  -> " + piece.getId() + " is no longer frozen!");
                piece.setState(underlyingState);
            }
        }
    }
}

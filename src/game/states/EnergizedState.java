package game.states;

import game.players.components.LudoPiece;

public class EnergizedState extends EffectState {

    public EnergizedState(PieceState underlyingState, int roundsRemaining) {
        super(underlyingState, roundsRemaining);
    }

    @Override
    public int calculateEffectiveRoll(int roll) {
        return roll * 2;
    }

    @Override
    public int calculateBlockEffectiveRoll(int roll) {
        return roll * 2;
    }

    @Override
    public boolean canMove() {
        return true;
    }

    @Override
    public void decrementRoundsPreTurn(LudoPiece piece) {
        if (roundsRemaining > 0) {
            roundsRemaining--;
            if (roundsRemaining == 0) {
                System.out.println("  -> " + piece.getId() + "'s Energized effect has worn off.");
                piece.setState(underlyingState);
            }
        }
    }

    @Override
    public void decrementRoundsPostTurn(LudoPiece piece) {
    }
}

package game.strategies.movement;

import game.players.components.LudoPiece;

public class BetaMovementStrategy implements MovementStrategy {
    private int roundsRemaining;

    public BetaMovementStrategy(int roundsRemaining) {
        this.roundsRemaining = roundsRemaining;
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
        System.out.println("  -> Piece cannot move (Beta frozen for " + roundsRemaining + " more rounds).");
        return false;
    }

    @Override
    public void decrementRoundsPreTurn(LudoPiece piece) {
        // Beta effect decrements post-turn
    }

    @Override
    public void decrementRoundsPostTurn(LudoPiece piece) {
        if (roundsRemaining > 0) {
            roundsRemaining--;
            if (roundsRemaining == 0) {
                System.out.println("  -> " + piece.getId() + " is no longer frozen by Beta!");
                piece.setMovementStrategy(new NormalMovementStrategy());
            }
        }
    }

    @Override
    public String getEffectName() {
        return "FROZEN";
    }
}

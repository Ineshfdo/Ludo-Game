package game.strategies.movement;

import players.components.LudoPiece;

public class NormalMovementStrategy implements MovementStrategy {

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
        return true;
    }

    @Override
    public void decrementRoundsPreTurn(LudoPiece piece) {
        // Normal movement has no rounds to decrement
    }

    @Override
    public void decrementRoundsPostTurn(LudoPiece piece) {
        // Normal movement has no rounds to decrement
    }

    @Override
    public String getEffectName() {
        return "NONE";
    }
}

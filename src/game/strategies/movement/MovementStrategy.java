package game.strategies.movement;

import game.players.components.LudoPiece;

public interface MovementStrategy {
    int calculateEffectiveRoll(int roll);

    int calculateBlockEffectiveRoll(int roll);

    boolean canMove();

    void decrementRoundsPreTurn(LudoPiece piece);

    void decrementRoundsPostTurn(LudoPiece piece);

    String getEffectName();
}

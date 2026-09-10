package game.states;

import game.LudoBoard;
import game.players.components.LudoPiece;

public interface PieceState {
    // Identity checks
    boolean isBase();
    boolean isStandard();
    boolean isHomeStraight();
    boolean isHome();
    String getStateName();

    // Behavior Delegation
    void step(LudoPiece piece, int direction);
    void removeFromBoard(LudoPiece piece, LudoBoard board);
    void addToBoard(LudoPiece piece, LudoBoard board);

    // Effect Delegation
    int calculateEffectiveRoll(int roll);
    int calculateBlockEffectiveRoll(int roll);
    boolean canMove();
    
    // Lifecycle Management
    void decrementRoundsPreTurn(LudoPiece piece);
    void decrementRoundsPostTurn(LudoPiece piece);
}

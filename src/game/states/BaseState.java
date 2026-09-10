package game.states;

import game.LudoBoard;
import game.players.components.LudoPiece;

public class BaseState implements PieceState {

    @Override public boolean isBase() { return true; }
    @Override public boolean isStandard() { return false; }
    @Override public boolean isHomeStraight() { return false; }
    @Override public boolean isHome() { return false; }
    @Override public String getStateName() { return "BASE"; }

    @Override
    public void step(LudoPiece piece, int direction) {
    }

    @Override
    public void removeFromBoard(LudoPiece piece, LudoBoard board) {
    }

    @Override
    public void addToBoard(LudoPiece piece, LudoBoard board) {
    }

    @Override public int calculateEffectiveRoll(int roll) { return roll; }
    @Override public int calculateBlockEffectiveRoll(int roll) { return roll; }
    @Override public boolean canMove() { return true; }
    @Override public void decrementRoundsPreTurn(LudoPiece piece) {}
    @Override public void decrementRoundsPostTurn(LudoPiece piece) {}
}

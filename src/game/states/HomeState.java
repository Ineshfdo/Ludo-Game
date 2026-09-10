package game.states;

import game.LudoBoard;
import game.players.components.LudoPiece;

public class HomeState implements PieceState {

    @Override public boolean isBase() { return false; }
    @Override public boolean isStandard() { return false; }
    @Override public boolean isHomeStraight() { return false; }
    @Override public boolean isHome() { return true; }
    @Override public String getStateName() { return "HOME"; }

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
    @Override public boolean canMove() { return false; }
    @Override public void decrementRoundsPreTurn(LudoPiece piece) {}
    @Override public void decrementRoundsPostTurn(LudoPiece piece) {}
}

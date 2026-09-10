package game.states;

import game.LudoBoard;
import game.players.components.LudoPiece;

public class HomeStraightState implements PieceState {

    @Override public boolean isBase() { return false; }
    @Override public boolean isStandard() { return false; }
    @Override public boolean isHomeStraight() { return true; }
    @Override public boolean isHome() { return false; }
    @Override public String getStateName() { return "HOME_STRAIGHT"; }

    @Override
    public void step(LudoPiece piece, int direction) {
        int nextPos = piece.getPosition() + 1;
        if (nextPos <= LudoBoard.HOME_STRAIGHT_LENGTH) {
            piece.setPosition(nextPos);
            if (piece.getPosition() == LudoBoard.HOME_STRAIGHT_LENGTH) {
                piece.setState(new HomeState());
            }
        }
    }

    @Override
    public void removeFromBoard(LudoPiece piece, LudoBoard board) {
        int pos = piece.getPosition();
        if (pos >= 0 && pos < LudoBoard.HOME_STRAIGHT_LENGTH) {
            board.getHomeStraight(piece.getColor())[pos].removePiece(piece);
        }
    }

    @Override
    public void addToBoard(LudoPiece piece, LudoBoard board) {
        int pos = piece.getPosition();
        if (pos >= 0 && pos < LudoBoard.HOME_STRAIGHT_LENGTH) {
            board.getHomeStraight(piece.getColor())[pos].addPiece(piece);
        } else {
            System.out.println("  -> Piece " + piece.getId() + " entered HOME at position " + pos);
        }
    }

    @Override public int calculateEffectiveRoll(int roll) { return roll; }
    @Override public int calculateBlockEffectiveRoll(int roll) { return roll; }
    @Override public boolean canMove() { return true; }
    @Override public void decrementRoundsPreTurn(LudoPiece piece) {}
    @Override public void decrementRoundsPostTurn(LudoPiece piece) {}
}

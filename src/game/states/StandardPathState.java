package game.states;

import game.LudoBoard;
import game.players.components.LudoPiece;
import game.utils.PathUtils;

public class StandardPathState implements PieceState {

    @Override public boolean isBase() { return false; }
    @Override public boolean isStandard() { return true; }
    @Override public boolean isHomeStraight() { return false; }
    @Override public boolean isHome() { return false; }
    @Override public String getStateName() { return "STANDARD"; }

    @Override
    public void step(LudoPiece piece, int direction) {
        boolean readyForHome = false;
        int piecePosition = piece.getPosition();
        int piecePasses = piece.getApproachPasses();

        if (piecePosition == PathUtils.getApproachIndex(piece.getColor()) && piece.getCaptures() >= 1) {
            if (piece.isXChoiceDirectionClockwise() && piecePasses >= 1)
                readyForHome = true;
            if (!piece.isXChoiceDirectionClockwise() && piecePasses >= 2)
                readyForHome = true;
        }

        if (readyForHome) {
            piece.setState(new HomeStraightState());
            piece.setPosition(0);
        } else {
            piecePosition = (piecePosition + direction + LudoBoard.STANDARD_PATH_LENGTH) % LudoBoard.STANDARD_PATH_LENGTH;
            if (piecePosition == PathUtils.getApproachIndex(piece.getColor())) {
                piece.incrementApproachPasses();
            }
            piece.setPosition(piecePosition);
        }
    }

    @Override
    public void removeFromBoard(LudoPiece piece, LudoBoard board) {
        int pos = piece.getPosition();
        if (pos >= 0 && pos < board.getStandardPath().length) {
            board.getStandardPath()[pos].removePiece(piece);
        }
    }

    @Override
    public void addToBoard(LudoPiece piece, LudoBoard board) {
        int pos = piece.getPosition();
        if (pos >= 0 && pos < board.getStandardPath().length) {
            board.getStandardPath()[pos].addPiece(piece);
        }
    }

    @Override public int calculateEffectiveRoll(int roll) { return roll; }
    @Override public int calculateBlockEffectiveRoll(int roll) { return roll; }
    @Override public boolean canMove() { return true; }
    @Override public void decrementRoundsPreTurn(LudoPiece piece) {}
    @Override public void decrementRoundsPostTurn(LudoPiece piece) {}
}

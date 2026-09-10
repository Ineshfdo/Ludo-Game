package game.effects;

import game.LudoBoard;
import game.players.components.LudoPiece;
import game.states.BriefingState;

public class BetaEffect {

    public static void decrementRoundsPostTurn(LudoPiece[] pieces) {
        for (LudoPiece piece : pieces) {
            if (!piece.getState().isBase() && !piece.getState().isHome()) {
                piece.getState().decrementRoundsPostTurn(piece);
            }
        }
    }

    public static void applyIndividual(LudoPiece piece) {
        System.out.println("  * Beta Effect Activated! Piece " + piece.getId() + " frozen for 4 rounds. *");
        piece.setState(new BriefingState(piece.getState(), 4));
    }

    public static void applyBlock(java.util.List<LudoPiece> block) {
        System.out.println("  * Beta Effect Activated for the Block! All pieces frozen for 4 rounds. *");
        for (LudoPiece piece : block) {
            piece.setState(new BriefingState(piece.getState(), 4));
        }
    }

    public static boolean canMove(LudoPiece piece) {
        return piece.getState().canMove();
    }

    public static void applyConsecutiveThreesPenalty(LudoPiece[] pieces, LudoBoard board) {
        for (LudoPiece piece : pieces) {
            // A piece is in BriefingState (frozen) if canMove() returns false
            if (!piece.getState().canMove()) {
                System.out.println("  -> Penalty: Piece " + piece.getId()
                        + " is frozen at Beta and player rolled 3 consecutively! Sent to BASE.");
                board.removePieceFromBoard(piece);
                piece.resetToDefault();
            }
        }
    }
}

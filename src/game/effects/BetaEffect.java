package game.effects;

import game.LudoBoard;
import game.players.components.LudoPiece;
import game.strategies.movement.BetaMovementStrategy;

public class BetaEffect {

    public static void decrementRoundsPostTurn(LudoPiece[] pieces) {
        for (LudoPiece piece : pieces) {
            if (piece.getState().equals("STANDARD") || piece.getState().equals("HOME_STRAIGHT")) {
                piece.getMovementStrategy().decrementRoundsPostTurn(piece);
            }
        }
    }

    public static void applyIndividual(LudoPiece piece) {
        System.out.println("  * Beta Effect Activated! Piece " + piece.getId() + " frozen for 4 rounds. *");
        piece.setMovementStrategy(new BetaMovementStrategy(4));
    }

    public static void applyBlock(java.util.List<LudoPiece> block) {
        System.out.println("  * Beta Effect Activated for the Block! All pieces frozen for 4 rounds. *");
        for (LudoPiece piece : block) {
            piece.setMovementStrategy(new BetaMovementStrategy(4));
        }
    }

    public static boolean canMove(LudoPiece piece) {
        return piece.getMovementStrategy().canMove();
    }

    public static void applyConsecutiveThreesPenalty(LudoPiece[] pieces, LudoBoard board) {
        for (LudoPiece piece : pieces) {
            if (!piece.getMovementStrategy().canMove()
                    && piece.getMovementStrategy().getEffectName().equals("FROZEN")) {
                System.out.println("  -> Penalty: Piece " + piece.getId()
                        + " is frozen at Beta and player rolled 3 consecutively! Sent to BASE.");
                board.removePieceFromBoard(piece);
                piece.resetToDefault();
            }
        }
    }
}

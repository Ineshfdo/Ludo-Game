package game.effects;

import game.LudoBoard;
import players.components.LudoPiece;

public class BetaEffect {

    public static void decrementRounds(LudoPiece[] pieces) {
        for (LudoPiece piece : pieces) {
            if (piece.getBetaFreezeRoundsRemaining() > 0) {
                piece.setBetaFreezeRoundsRemaining(piece.getBetaFreezeRoundsRemaining() - 1);
                if (piece.getBetaFreezeRoundsRemaining() == 0) {
                    System.out.println("  -> " + piece.getId() + " is no longer frozen by Beta!");
                }
            }
        }
    }

    public static void applyIndividual(LudoPiece piece) {
        piece.setBetaFreezeRoundsRemaining(4);
    }

    public static void applyBlock(java.util.List<LudoPiece> block) {
        for (LudoPiece piece : block) {
            piece.setBetaFreezeRoundsRemaining(4);
        }
    }

    public static boolean canMove(LudoPiece piece) {
        if (piece.getBetaFreezeRoundsRemaining() > 0) {
            System.out.println("  -> Piece " + piece.getId() + " cannot move (Beta frozen for "
                    + piece.getBetaFreezeRoundsRemaining() + " more rounds).");
            return false;
        }
        return true;
    }

    public static void applyConsecutiveThreesPenalty(LudoPiece[] pieces, LudoBoard board) {
        for (LudoPiece piece : pieces) {
            if (piece.getBetaFreezeRoundsRemaining() > 0) {
                System.out.println("  -> Penalty: Piece " + piece.getId()
                        + " is frozen at Beta and player rolled 3 consecutively! Sent to BASE.");
                board.removePieceFromBoard(piece);
                piece.resetToDefault();
            }
        }
    }
}

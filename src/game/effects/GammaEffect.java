package game.effects;

import players.LudoPiece;

public class GammaEffect {

    public static int determineTeleportOption(boolean isClockwise) {
        if (isClockwise) {
            System.out.println("  -> Moving Clockwise. Teleporting to Gamma (Cell 46) and changing direction!");
            return 3;
        } else {
            System.out.println("  -> Moving Counter-Clockwise. Teleport hijacked! Sent to Beta (Cell 27) instead.");
            return 2;
        }
    }

    public static void applyIndividual(LudoPiece piece) {
        piece.setXChoiceDirectionClockwise(false);
        piece.setCombinedBlockDirectionClockwise(false);
        piece.setBreakBlockDirectionClockwise(false);
    }

    public static void applyBlock(java.util.List<LudoPiece> block) {
        for (LudoPiece piece : block) {
            piece.setXChoiceDirectionClockwise(false);
            piece.setCombinedBlockDirectionClockwise(false);
            piece.setBreakBlockDirectionClockwise(false);
        }
    }
}

package game.effects;

import java.util.Random;

import game.players.components.LudoPiece;
import game.states.EnergizedState;
import game.states.SickState;

public class AlphaEffect {

    private static final Random random = new Random();

    // The two possible effects
    public static final String[] EFFECTS = { "ENERGIZED", "SICK" };

    // Method to apply effect when landing on mystery cell (individual piece)
    public static void applyMysteryCellEffect(LudoPiece landingPiece, LudoPiece[] allPiecesInBlock) {
        String randomEffect = EFFECTS[random.nextInt(EFFECTS.length)];
        System.out.println("  -> Alpha Effect triggered: " + randomEffect + "!");

        if (allPiecesInBlock != null && allPiecesInBlock.length > 0) {
            // It's a block, apply to all pieces in the block
            for (LudoPiece piece : allPiecesInBlock) {
                if (piece != null) {
                    System.out.println("  -> Applying Block Alpha Effect to " + piece.getId());
                    applyEffectToPiece(piece, randomEffect, 2);
                }
            }
        } else {
            // Apply individually
            System.out.println("  -> Applying Individual Alpha Effect to " + landingPiece.getId());
            applyEffectToPiece(landingPiece, randomEffect, 2);
        }
    }

    private static void applyEffectToPiece(LudoPiece piece, String effect, int rounds) {
        if ("ENERGIZED".equals(effect)) {
            piece.setState(new EnergizedState(piece.getState(), rounds));
        } else if ("SICK".equals(effect)) {
            piece.setState(new SickState(piece.getState(), rounds));
        }
    }

    // Call this at the START of a player's turn to decrement their alpha rounds
    public static void decrementRoundsPreTurn(LudoPiece[] pieces) {
        for (LudoPiece piece : pieces) {
            if (!piece.getState().isBase() && !piece.getState().isHome()) {
                piece.getState().decrementRoundsPreTurn(piece);
            }
        }
    }

    // Method to calculate the effective roll for a piece based on its alpha effects
    public static int calculateEffectiveRoll(LudoPiece piece, int originalRoll) {
        return piece.getState().calculateEffectiveRoll(originalRoll);
    }

    // Method to calculate effective roll for a block
    public static int calculateBlockEffectiveRoll(LudoPiece[] piecesInBlock, int originalRoll) {
        if (piecesInBlock == null || piecesInBlock.length == 0 || piecesInBlock[0] == null) {
            return originalRoll;
        }
        return piecesInBlock[0].getState().calculateBlockEffectiveRoll(originalRoll);
    }
}
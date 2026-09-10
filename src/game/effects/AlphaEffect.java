package game.effects;

import java.util.Random;

import game.players.components.LudoPiece;
import game.strategies.movement.AlphaMovementStrategy;
import game.strategies.movement.MovementStrategy;

public class AlphaEffect {

    private static final Random random = new Random();

    // The two possible effects
    public static final String[] EFFECTS = { "ENERGIZED", "SICK" };

    // Method to apply effect when landing on mystery cell
    public static void applyMysteryCellEffect(LudoPiece landingPiece, LudoPiece[] allPiecesInBlock) {
        String randomEffect = EFFECTS[random.nextInt(EFFECTS.length)];
        System.out.println("  -> Alpha Effect triggered: " + randomEffect + "!");

        if (allPiecesInBlock != null && allPiecesInBlock.length > 0) {
            // It's a block, apply to all pieces in the block
            for (LudoPiece piece : allPiecesInBlock) {
                if (piece != null) {
                    System.out.println("  -> Applying Block Alpha Effect to " + piece.getId());
                    MovementStrategy current = piece.getMovementStrategy();
                    String indEffect = "NONE";
                    int indRounds = 0;
                    if (current instanceof AlphaMovementStrategy) {
                        AlphaMovementStrategy ams = (AlphaMovementStrategy) current;
                        indEffect = ams.getEffectName();
                        indRounds = 2; // Approximate
                    }

                    piece.setMovementStrategy(new AlphaMovementStrategy(indEffect, indRounds, randomEffect, 2));
                }
            }
        } else {
            // Apply individually
            System.out.println("  -> Applying Individual Alpha Effect to " + landingPiece.getId());
            String blkEffect = "NONE";
            int blkRounds = 0;
            landingPiece.setMovementStrategy(new AlphaMovementStrategy(randomEffect, 2, blkEffect, blkRounds));
        }
    }

    // Call this at the START of a player's turn to decrement their alpha rounds
    public static void decrementRoundsPreTurn(LudoPiece[] pieces) {
        for (LudoPiece piece : pieces) {
            if (piece.getState().equals("STANDARD") || piece.getState().equals("HOME_STRAIGHT")) {
                piece.getMovementStrategy().decrementRoundsPreTurn(piece);
            }
        }
    }

    // Method to calculate the effective roll for a piece based on its alpha effects
    public static int calculateEffectiveRoll(LudoPiece piece, int originalRoll) {
        return piece.getMovementStrategy().calculateEffectiveRoll(originalRoll);
    }

    // Method to calculate effective roll for a block
    public static int calculateBlockEffectiveRoll(LudoPiece[] piecesInBlock, int originalRoll) {
        if (piecesInBlock == null || piecesInBlock.length == 0 || piecesInBlock[0] == null) {
            return originalRoll;
        }
        return piecesInBlock[0].getMovementStrategy().calculateBlockEffectiveRoll(originalRoll);
    }
}
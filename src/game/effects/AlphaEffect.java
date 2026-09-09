package game.effects;

import players.LudoPiece;
import utils.CoinFlip;

public class AlphaEffect {

    public static void decrementRounds(LudoPiece[] pieces) {
        for (LudoPiece piece : pieces) {
            if (piece.getIndividualAlphaRoundsRemaining() > 0) {
                piece.setIndividualAlphaRoundsRemaining(piece.getIndividualAlphaRoundsRemaining() - 1);
                if (piece.getIndividualAlphaRoundsRemaining() == 0) {
                    piece.setIndividualAlphaEffect("NONE");
                    System.out.println("  -> " + piece.getId() + "'s Individual Alpha has worn off.");
                }
            }
            if (piece.getBlockAlphaRoundsRemaining() > 0) {
                piece.setBlockAlphaRoundsRemaining(piece.getBlockAlphaRoundsRemaining() - 1);
                if (piece.getBlockAlphaRoundsRemaining() == 0) {
                    piece.setBlockAlphaEffect("NONE");
                    System.out.println("  -> " + piece.getId() + "'s Block Alpha has worn off.");
                }
            }
        }
    }

    public static void applyIndividual(LudoPiece piece) {
        System.out.println("  * Alpha Effect Activated! *");
        boolean isIndividualCoinHeads = CoinFlip.getInstance().flip();
        String individualAlphaStatus = isIndividualCoinHeads ? "ENERGIZED" : "SICK";
        System.out.println("  -> Piece " + piece.getId() + " Individual Alpha Coin Toss: " + (isIndividualCoinHeads ? "Heads (ENERGIZED)" : "Tails (SICK)"));
        piece.setIndividualAlphaEffect(individualAlphaStatus);
        piece.setIndividualAlphaRoundsRemaining(4);
    }

    public static void applyBlock(java.util.List<LudoPiece> block) {
        System.out.println("  * Alpha Effect Activated for the Block! *");
        boolean isBlockCoinHeads = CoinFlip.getInstance().flip();
        String blockAlphaStatus = isBlockCoinHeads ? "ENERGIZED" : "SICK";
        System.out.println("  -> Block Alpha Coin Toss: " + (isBlockCoinHeads ? "Heads (ENERGIZED)" : "Tails (SICK)"));
        
        for (LudoPiece piece : block) {
            boolean isIndividualCoinHeads = CoinFlip.getInstance().flip();
            String individualAlphaStatus = isIndividualCoinHeads ? "ENERGIZED" : "SICK";
            System.out.println("  -> Piece " + piece.getId() + " Individual Alpha Coin Toss: " + (isIndividualCoinHeads ? "Heads (ENERGIZED)" : "Tails (SICK)"));
            
            piece.setIndividualAlphaEffect(individualAlphaStatus);
            piece.setIndividualAlphaRoundsRemaining(4);
            piece.setBlockAlphaEffect(blockAlphaStatus);
            piece.setBlockAlphaRoundsRemaining(4);
        }
    }

    public static int calculateEffectiveRoll(LudoPiece piece, int roll) {
        if (piece.getIndividualAlphaRoundsRemaining() > 0) {
            int effectiveRoll = roll;
            if ("ENERGIZED".equals(piece.getIndividualAlphaEffect())) {
                effectiveRoll = roll * 2;
            } else if ("SICK".equals(piece.getIndividualAlphaEffect())) {
                effectiveRoll = roll / 2;
            }
            System.out.println("  -> Piece " + piece.getId() + " has " + piece.getIndividualAlphaEffect() + " alpha! Effective roll is " + effectiveRoll);
            return effectiveRoll;
        }
        return roll;
    }

    public static int calculateBlockEffectiveRoll(LudoPiece dominant, int roll) {
        if (dominant.getBlockAlphaRoundsRemaining() > 0) {
            if ("ENERGIZED".equals(dominant.getBlockAlphaEffect())) {
                return roll * 2;
            } else if ("SICK".equals(dominant.getBlockAlphaEffect())) {
                return roll / 2;
            }
        }
        return roll;
    }
}
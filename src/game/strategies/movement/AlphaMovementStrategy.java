package game.strategies.movement;

import game.players.components.LudoPiece;

public class AlphaMovementStrategy implements MovementStrategy {
    private String individualEffect;
    private int individualRounds;
    private String blockEffect;
    private int blockRounds;

    public AlphaMovementStrategy(String individualEffect, int individualRounds, String blockEffect, int blockRounds) {
        this.individualEffect = individualEffect;
        this.individualRounds = individualRounds;
        this.blockEffect = blockEffect;
        this.blockRounds = blockRounds;
    }

    @Override
    public int calculateEffectiveRoll(int roll) {
        if (individualRounds > 0) {
            int effectiveRoll = roll;
            if ("ENERGIZED".equals(individualEffect)) {
                effectiveRoll = roll * 2;
            } else if ("SICK".equals(individualEffect)) {
                effectiveRoll = roll / 2;
            }
            return effectiveRoll;
        }
        return roll;
    }

    @Override
    public int calculateBlockEffectiveRoll(int roll) {
        if (blockRounds > 0) {
            if ("ENERGIZED".equals(blockEffect)) {
                return roll * 2;
            } else if ("SICK".equals(blockEffect)) {
                return roll / 2;
            }
        }
        return roll;
    }

    @Override
    public boolean canMove() {
        return true;
    }

    @Override
    public void decrementRoundsPreTurn(LudoPiece piece) {
        boolean effectActive = false;
        if (individualRounds > 0) {
            individualRounds--;
            if (individualRounds == 0) {
                System.out.println(
                        "  -> " + piece.getId() + "'s Individual Alpha (" + individualEffect + ") has worn off.");
                individualEffect = "NONE";
            } else {
                effectActive = true;
            }
        }
        if (blockRounds > 0) {
            blockRounds--;
            if (blockRounds == 0) {
                System.out.println("  -> " + piece.getId() + "'s Block Alpha (" + blockEffect + ") has worn off.");
                blockEffect = "NONE";
            } else {
                effectActive = true;
            }
        }

        if (!effectActive) {
            piece.setMovementStrategy(new NormalMovementStrategy());
        }
    }

    @Override
    public void decrementRoundsPostTurn(LudoPiece piece) {
        // Alpha effect decrements pre-turn
    }

    @Override
    public String getEffectName() {
        return individualEffect;
    }
}

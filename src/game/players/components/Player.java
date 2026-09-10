package game.players.components;

import game.Dice;
import game.LudoBoard;
import game.effects.AlphaEffect;
import game.effects.BetaEffect;
import game.players.strategies.Strategy;
import game.utils.MovementManager;

// Uses an abstract class to promote code reuse (holding pieces and color)

public abstract class Player {

    protected PlayerColor color;
    protected LudoPiece[] pieces;
    protected int consecutiveThrees = 0;
    protected Strategy strategy;

    // CONSTRUCTOR
    public Player(PlayerColor color, Strategy strategy) {
        this.color = color;
        this.strategy = strategy;
        this.pieces = new LudoPiece[4];

        // Dynamically generate the Piece IDs based on color prefix (e.g -> R1,R2,R3,R4)
        String prefix = color.name().substring(0, 1);
        for (int i = 0; i < 4; i++) {
            pieces[i] = new LudoPiece(prefix + (i + 1), color);
        }
    }

    // GETTERS & STATUS METHODS
    public PlayerColor getColor() {
        return color;
    }

    public LudoPiece[] getPieces() {
        return pieces;
    }

    // Helper method to find a piece that is currently sitting in the base
    public LudoPiece getPieceInBase() {
        for (LudoPiece piece : pieces) {
            if (piece.getState().isBase()) {
                return piece;
            }
        }
        return null;
    }

    // Helper method to find a piece that is currently active on the board
    public LudoPiece getPieceOnBoard() {
        for (LudoPiece piece : pieces) {
            if (piece.getState().isStandard() || piece.getState().isHomeStraight()) {
                return piece;
            }
        }
        return null;
    }

    public boolean hasFinished() {
        for (LudoPiece piece : pieces) {
            if (!piece.getState().isHome()) {
                return false;
            }
        }
        return true;
    }

    // CORE TURN LOGIC
    public void executeTurn(Dice dice, LudoBoard board) {

        // Decrement the Alpha Rounds
        AlphaEffect.decrementRoundsPreTurn(pieces);

        int consecutiveSixes = 0;
        boolean turnContinues = true;

        while (turnContinues) {
            int roll = dice.roll();
            String colorName = getColor().toString().substring(0, 1).toUpperCase()
                    + getColor().toString().substring(1).toLowerCase();
            System.out.println(colorName + " player rolled " + roll + ".");

            handleConsecutiveThrees(roll, board);

            if (roll == 6) {
                consecutiveSixes++;
                if (consecutiveSixes == 3) {
                    applyConsecutiveSixesPenalty(board);
                    break;
                }
            } else {
                turnContinues = false; // Turn ends after this roll
            }

            boolean captured = processMovement(roll, board);

            if (captured) {
                turnContinues = true;
            }
        }

        // Decrement Beta Freeze Rounds
        BetaEffect.decrementRoundsPostTurn(pieces);
    }

    // PRIVATE HELPER METHODS FOR TURN LOGIC

    private void handleConsecutiveThrees(int roll, LudoBoard board) {
        if (roll == 3) {
            consecutiveThrees++;
            if (consecutiveThrees >= 2) {
                BetaEffect.applyConsecutiveThreesPenalty(pieces, board);
            }
        } else {
            consecutiveThrees = 0;
        }
    }

    private void applyConsecutiveSixesPenalty(LudoBoard board) {
        MovementManager.tryBreakBlockadeForConsecutiveSixes(this);
    }

    protected boolean processMovement(int roll, LudoBoard board) {
        return strategy.processMovement(pieces, roll, board);
    }
}

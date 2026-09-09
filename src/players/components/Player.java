package players.components;

import game.Dice;
import game.LudoBoard;
import game.effects.AlphaEffect;
import game.effects.BetaEffect;
import game.utils.MovementManager;

// Uses an abstract class to promote code reuse (holding pieces and color)

public abstract class Player {

    protected PlayerColor color;
    protected LudoPiece[] pieces;
    protected int consecutiveThrees = 0;

    // CONSTRUCTOR
    public Player(PlayerColor color) {
        this.color = color;
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
            if (piece.getState().equals("BASE")) {
                return piece;
            }
        }
        return null;
    }

    // Helper method to find a piece that is currently active on the board
    public LudoPiece getPieceOnBoard() {
        for (LudoPiece piece : pieces) {
            if (piece.getState().equals("STANDARD") || piece.getState().equals("HOME_STRAIGHT")) {
                return piece;
            }
        }
        return null;
    }

    public boolean hasFinished() {
        for (LudoPiece piece : pieces) {
            if (!piece.getState().equals("HOME")) {
                return false;
            }
        }
        return true;
    }

    // CORE TURN LOGIC
    public void executeTurn(Dice dice, LudoBoard board) {

        // Decrement the Alpha Rounds
        AlphaEffect.decrementRounds(pieces);

        int consecutiveSixes = 0;
        boolean turnContinues = true;

        while (turnContinues) {
            int roll = dice.roll();
            System.out.println("  -> Rolled a " + roll);

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
                System.out.println("  -> " + getColor() + " gets a bonus roll for capturing an opponent!");
                turnContinues = true;
            }
        }

        // Decrement Beta Freeze Rounds
        BetaEffect.decrementRounds(pieces);
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
        System.out.println("  -> Rolled a 6 for the third time!");
        boolean brokeBlockade = MovementManager.tryBreakBlockadeForConsecutiveSixes(this);
        if (brokeBlockade) {
            System.out.println("  -> Penalty: Blockade forcibly broken!");
        } else {
            System.out.println("  -> No blockades to break. Turn skipped.");
        }
    }

    private boolean processMovement(int roll, LudoBoard board) {
        boolean captured = false;

        // 1. Try to move a piece out of BASE
        if (roll == 6) {
            LudoPiece pieceInBase = getPieceInBase();
            if (pieceInBase != null) {
                System.out.println(
                        "  -> Rolled a 6! Attempting to move piece " + pieceInBase.getId() + " out of BASE...");
                captured = MovementManager.movePiece(pieceInBase, roll, true);
                if (!pieceInBase.getState().equals("BASE")) {
                    return captured; // Move was successful
                }
            }
        }

        // 2. Try to move ANY piece on the board normally (keeping block together)
        for (LudoPiece piece : pieces) {
            if (!BetaEffect.canMove(piece)) {
                continue;
            }

            if (piece.getState().equals("STANDARD") || piece.getState().equals("HOME_STRAIGHT")) {
                int oldPos = piece.getPosition();
                String oldState = piece.getState();

                captured = MovementManager.movePiece(piece, roll, true);

                if (!piece.getState().equals(oldState) || piece.getPosition() != oldPos) {
                    System.out.println("  -> Moved piece " + piece.getId() + " from " + oldState + " [" + oldPos
                            + "] to " + piece.getState() + " [" + piece.getPosition() + "]");
                    return captured; // Move was successful
                }
            }
        }

        // 3. FALLBACK: Try breaking the block
        for (LudoPiece piece : pieces) {
            if (!BetaEffect.canMove(piece)) {
                continue;
            }

            if (piece.getState().equals("STANDARD") || piece.getState().equals("HOME_STRAIGHT")) {
                int oldPos = piece.getPosition();
                String oldState = piece.getState();

                captured = MovementManager.movePiece(piece, roll, false);

                if (!piece.getState().equals(oldState) || piece.getPosition() != oldPos) {
                    System.out.println("  -> Piece " + piece.getId() + " broke away from its block from " + oldState
                            + " [" + oldPos + "] to " + piece.getState() + " [" + piece.getPosition() + "]");
                    return captured; // Move was successful
                }
            }
        }

        // 4. No pieces could be moved
        if (roll != 6) {
            System.out.println("  -> No pieces on the board could be moved.");
        } else {
            System.out.println("  -> Rolled a 6 but no pieces could be moved.");
        }

        return false;
    }
}

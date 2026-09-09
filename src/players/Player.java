package players;

import core.Dice;
import core.LudoBoard;


// Uses an abstract class to promote code reuse (holding pieces and color)
// while forcing specific color subclasses to implement their own distinct AI behavior.

public abstract class Player {
    protected PlayerColor color;
    protected LudoPiece[] pieces;
    protected int consecutiveThrees = 0;

    public Player(PlayerColor color) {
        this.color = color;
        this.pieces = new LudoPiece[4];

        // Dynamically generate the Piece IDs based on color prefix (e.g., Red becomes R1, R2, R3, R4)
        String prefix = color.name().substring(0, 1);
        for (int i = 0; i < 4; i++) {
            pieces[i] = new LudoPiece(prefix + (i + 1), color);
        }
    }

    // Executes the player's turn logic.
    public void executeTurn(Dice dice, LudoBoard board) {
        // Decrement Aura rounds
        for (LudoPiece p : pieces) {
            if (p.getIndividualAuraRoundsRemaining() > 0) {
                p.setIndividualAuraRoundsRemaining(p.getIndividualAuraRoundsRemaining() - 1);
                if (p.getIndividualAuraRoundsRemaining() == 0) {
                    p.setIndividualAuraEffect("NONE");
                    System.out.println("  -> " + p.getId() + "'s Individual Aura has worn off.");
                }
            }
            if (p.getBlockAuraRoundsRemaining() > 0) {
                p.setBlockAuraRoundsRemaining(p.getBlockAuraRoundsRemaining() - 1);
                if (p.getBlockAuraRoundsRemaining() == 0) {
                    p.setBlockAuraEffect("NONE");
                    System.out.println("  -> " + p.getId() + "'s Block Aura has worn off.");
                }
            }
        }

        int consecutiveSixes = 0;
        boolean turnContinues = true;

        while (turnContinues) {
            int roll = dice.roll();
            System.out.println("  -> Rolled a " + roll);
            
            if (roll == 3) {
                consecutiveThrees++;
                if (consecutiveThrees >= 2) {
                    for (LudoPiece p : pieces) {
                        if (p.getBetaFreezeRoundsRemaining() > 0) {
                            System.out.println("  -> Penalty: Piece " + p.getId() + " is frozen at Beta and player rolled 3 consecutively! Sent to BASE.");
                            board.removePieceFromBoard(p);
                            p.resetToDefault();
                        }
                    }
                }
            } else {
                consecutiveThrees = 0;
            }
            

            if (roll == 6) {
                consecutiveSixes++;
                if (consecutiveSixes == 3) {
                    System.out.println("  -> Rolled a 6 for the third time!");
                    boolean brokeBlockade = board.tryBreakBlockadeForConsecutiveSixes(this);
                    if (brokeBlockade) {
                        System.out.println("  -> Penalty: Blockade forcibly broken!");
                    } else {
                        System.out.println("  -> No blockades to break. Turn skipped.");
                    }
                    break;
                }
            } else {
                turnContinues = false; // Turn ends after this roll
            }

            boolean moveMade = false;
            boolean captured = false;

            if (roll == 6) {
                LudoPiece pieceInBase = getPieceInBase();
                if (pieceInBase != null) {
                    System.out.println("  -> Rolled a 6! Attempting to move piece " + pieceInBase.getId() + " out of BASE...");
                    captured = board.movePiece(pieceInBase, roll, true);
                    if (!pieceInBase.getState().equals("BASE")) {
                        moveMade = true;
                    }
                }
            }
            
            if (!moveMade) {
                // Try to find ANY piece on the board that can successfully move
                boolean anyPieceMoved = false;
                
                for (LudoPiece p : pieces) {
                    if (p.getBetaFreezeRoundsRemaining() > 0) {
                        System.out.println("  -> Piece " + p.getId() + " cannot move (Beta frozen for " + p.getBetaFreezeRoundsRemaining() + " more rounds).");
                        continue;
                    }
                    if (p.getState().equals("STANDARD") || p.getState().equals("HOME_STRAIGHT")) {
                        int oldPos = p.getPosition();
                        String oldState = p.getState();
                        
                        captured = board.movePiece(p, roll, true);
                        
                        if (!p.getState().equals(oldState) || p.getPosition() != oldPos) {
                            System.out.println("  -> Moved piece " + p.getId() + " from " + oldState + " [" + oldPos + "] to " + p.getState() + " [" + p.getPosition() + "]");
                            anyPieceMoved = true;
                            break; // Success! Stop trying other pieces.
                        }
                    }
                }
                
                if (!anyPieceMoved) {
                    // FALLBACK: Try breaking the block
                    for (LudoPiece p : pieces) {
                        if (p.getBetaFreezeRoundsRemaining() > 0) continue;
                        if (p.getState().equals("STANDARD") || p.getState().equals("HOME_STRAIGHT")) {
                            int oldPos = p.getPosition();
                            String oldState = p.getState();
                            
                            captured = board.movePiece(p, roll, false);
                            
                            if (!p.getState().equals(oldState) || p.getPosition() != oldPos) {
                                System.out.println("  -> Piece " + p.getId() + " broke away from its block from " + oldState + " [" + oldPos + "] to " + p.getState() + " [" + p.getPosition() + "]");
                                anyPieceMoved = true;
                                break; 
                            }
                        }
                    }
                }
                
                if (!anyPieceMoved) {
                    if (roll != 6) {
                        System.out.println("  -> No pieces on the board could be moved.");
                    } else {
                        System.out.println("  -> Rolled a 6 but no pieces could be moved.");
                    }
                }
            }

            if (captured) {
                System.out.println("  -> " + getColor() + " gets a bonus roll for capturing an opponent!");
                turnContinues = true;
            }
        }
        
        // Decrement Beta Freeze rounds at the end of the turn
        for (LudoPiece p : pieces) {
            if (p.getBetaFreezeRoundsRemaining() > 0) {
                p.setBetaFreezeRoundsRemaining(p.getBetaFreezeRoundsRemaining() - 1);
                if (p.getBetaFreezeRoundsRemaining() == 0) {
                    System.out.println("  -> " + p.getId() + " is no longer frozen by Beta!");
                }
            }
        }
    }

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
        return null; // No pieces left in the base
    }

    // Helper method to find a piece that is currently active on the board
    public LudoPiece getPieceOnBoard() {
        for (LudoPiece piece : pieces) {
            if (piece.getState().equals("STANDARD") || piece.getState().equals("HOME_STRAIGHT")) {
                return piece;
            }
        }
        return null; // No pieces on the board
    }

    public boolean hasFinished() {
        for (LudoPiece p : pieces) {
            if (!p.getState().equals("HOME")) {
                return false;
            }
        }
        return true;
    }
}


package game;

import players.LudoPiece;
import players.PlayerColor;
import game.effects.AlphaEffect;
import game.effects.BetaEffect;
import game.effects.GammaEffect;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

// Uses the Singleton pattern to ensure only one board instance exists globally.

public class LudoBoard {
    private static LudoBoard instance;

    public static final int STANDARD_PATH_LENGTH = 52;
    public static final int HOME_STRAIGHT_LENGTH = 5;

    // Main path where all pieces travel.
    private Cell[] standardPath;

    private Cell[] redHomeStraight;
    private Cell[] greenHomeStraight;
    private Cell[] yellowHomeStraight;
    private Cell[] blueHomeStraight;

    // Mystery Cell Tracking
    private int currentMysteryCellPosition = -1;
    private int previousMysteryCellPosition = -1;

    private LudoBoard() {
        standardPath = new Cell[STANDARD_PATH_LENGTH];
        
        // 52 standard cells Including X and Approach cells
        for (int cellIndex = 0; cellIndex < STANDARD_PATH_LENGTH; cellIndex++) {
            boolean isStart = (cellIndex == 2 || cellIndex == 15 || cellIndex == 28 || cellIndex == 41);
            boolean isApproach = (cellIndex == 0 || cellIndex == 13 || cellIndex == 26 || cellIndex == 39);
            String cellId = String.valueOf(cellIndex);
            standardPath[cellIndex] = new Cell(cellId, cellIndex, (PlayerColor) null, isStart, isApproach);
        }

        // 20 color specific cells (5 each) connecting to Home called Home Straight
        redHomeStraight = initializeHomeStraight(PlayerColor.RED);
        greenHomeStraight = initializeHomeStraight(PlayerColor.GREEN);
        yellowHomeStraight = initializeHomeStraight(PlayerColor.YELLOW);
        blueHomeStraight = initializeHomeStraight(PlayerColor.BLUE);
    }
    
    private Cell[] initializeHomeStraight(PlayerColor color) {
        Cell[] straight = new Cell[HOME_STRAIGHT_LENGTH];
        String colorPrefix = color.name().substring(0, 1).toUpperCase() + color.name().substring(1).toLowerCase();
        
        for (int cellIndex = 0; cellIndex < HOME_STRAIGHT_LENGTH; cellIndex++) {
            String cellId = colorPrefix + "home path" + cellIndex;
            straight[cellIndex] = new Cell(cellId, cellIndex, color, false, false);
        }
        return straight;
    }

    public static LudoBoard getInstance() {
        if (instance == null) {
            instance = new LudoBoard();
        }
        return instance;
    }

    public Cell[] getStandardPath() {
        return standardPath;
    }

    // Helper to remove a piece from its current cell before it moves
    public void removePieceFromBoard(LudoPiece piece) {
        if (piece.getState().equals("STANDARD")) {
            standardPath[piece.getPosition()].removePiece(piece);
        } else if (piece.getState().equals("HOME_STRAIGHT")) {
            Cell[] straight = getHomeStraight(piece.getColor());
            straight[piece.getPosition()].removePiece(piece);
        }
    }

    private void removeFromCurrentCell(LudoPiece piece) {
        removePieceFromBoard(piece);
    }

    // Helper to get the correct home straight array for a color
    private Cell[] getHomeStraight(PlayerColor color) {
        switch (color) {
            case RED: return redHomeStraight;
            case GREEN: return greenHomeStraight;
            case YELLOW: return yellowHomeStraight;
            case BLUE: return blueHomeStraight;
            default: return null;
        }
    }

    // Handles landing on a standard cell and capturing opponentPieces
    private boolean handleStandardCellLanding(LudoPiece piece, int newPos) {
        return handleStandardCellLanding(piece, newPos, false);
    }

    private boolean handleStandardCellLanding(LudoPiece piece, int newPos, boolean ignoreMysteryCell) {
        if (!ignoreMysteryCell && newPos == currentMysteryCellPosition && currentMysteryCellPosition != -1) {
            System.out.println("  -> Piece " + piece.getId() + " landed on the Mystery Cell!");
            return triggerMysteryCellEffect(piece);
        }

        boolean captured = false;
        Cell destinationCell = standardPath[newPos];
        
        // Use a copy of the list to avoid ConcurrentModificationException
        List<LudoPiece> piecesOnCell = new ArrayList<>(destinationCell.getPieces());
        for (LudoPiece otherPiece : piecesOnCell) {
            if (otherPiece.getColor() != piece.getColor()) {
                // Capture!
                otherPiece.resetToDefault();
                destinationCell.removePiece(otherPiece);
                System.out.println("  -> CAPTURE! " + piece.getId() + " captured " + otherPiece.getId() + "!");
                captured = true;
                piece.incrementCaptures(); // Increment captures for the attacking piece
            }
        }
        
        destinationCell.addPiece(piece);
        return captured;
    }

    private boolean isPathBlockedByOpponent(Cell cell, PlayerColor movingColor, int movingSize) {
        Map<PlayerColor, Integer> colorCounts = new HashMap<>();
        for (LudoPiece currentPiece : cell.getPieces()) {
            if (currentPiece.getColor() != movingColor && currentPiece.getState().equals("STANDARD")) {
                colorCounts.put(currentPiece.getColor(), colorCounts.getOrDefault(currentPiece.getColor(), 0) + 1);
            }
        }
        for (int count : colorCounts.values()) {
            if (count > movingSize) {
                return true;
            }
        }
        return false;
    }

    // When a player can move a piece from the base, it will be moved to the cell marked with an "X".
    public boolean moveFromBaseToStart(LudoPiece piece) {
        if (piece.getState().equals("BASE")) {
            int startPos = getStartIndex(piece.getColor());
            if (isPathBlockedByOpponent(standardPath[startPos], piece.getColor(), 1)) {
                System.out.println("  -> Cannot move out of BASE! Start cell " + startPos + " is BLOCKED by an opponentPiece block.");
                return false;
            }
            piece.setState("STANDARD");
            piece.setPosition(startPos);
            piece.setApproachPasses(0);
            piece.setCaptures(0); // Ensure captures are reset when leaving base
            
            boolean heads = utils.CoinFlip.getInstance().flip();
            piece.setXChoiceDirectionClockwise(heads);
            String directionStr = heads ? "Clockwise (Heads)" : "Counter-Clockwise (Tails)";
            System.out.println("  -> Coin Toss! " + directionStr + " for piece " + piece.getId());
            
            return handleStandardCellLanding(piece, startPos);
        }
        return false;
    }

    public int getDistanceToHome(LudoPiece piece) {
        if (piece.getState().equals("HOME")) return 0;
        if (piece.getState().equals("BASE")) return 1000;
        
        int temporaryPosition = piece.getPosition();
        String temporaryState = piece.getState();
        int temporaryPasses = piece.getApproachPasses();
        int approachIndex = getApproachIndex(piece.getColor());
        int direction = piece.isXChoiceDirectionClockwise() ? 1 : -1;
        
        int steps = 0;
        while (!temporaryState.equals("HOME")) {
            if (steps > 100) return 100; // Failsafe
            if (temporaryState.equals("STANDARD")) {
                boolean readyForHome = false;
                // Ignore capture rule here to avoid infinite loop
                if (temporaryPosition == approachIndex) {
                    if (piece.isXChoiceDirectionClockwise() && temporaryPasses >= 1) readyForHome = true;
                    if (!piece.isXChoiceDirectionClockwise() && temporaryPasses >= 2) readyForHome = true;
                }
                
                if (readyForHome) {
                    temporaryState = "HOME_STRAIGHT";
                    temporaryPosition = 0;
                } else {
                    temporaryPosition = (temporaryPosition + direction + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
                    if (temporaryPosition == approachIndex) {
                        temporaryPasses++;
                    }
                }
            } else if (temporaryState.equals("HOME_STRAIGHT")) {
                temporaryPosition++;
                if (temporaryPosition >= HOME_STRAIGHT_LENGTH) {
                    temporaryState = "HOME";
                }
            }
            steps++;
        }
        return steps;
    }

    private boolean moveOppositeBlock(List<LudoPiece> block, int roll) {
        LudoPiece dominant = block.get(0);
        int baseStepsToMove = roll / block.size();
        int stepsToMove = AlphaEffect.calculateBlockEffectiveRoll(dominant, baseStepsToMove);
        
        System.out.println("  -> Opposite Block tried to move with roll " + roll + ". Base division results in " + baseStepsToMove + ". Effective steps: " + stepsToMove);
        if (stepsToMove == 0) {return false;
        }
        
        // Determine longest distance from home
        int maxDistance = -1;
        LudoPiece dominantPiece = null;
        for (LudoPiece currentPiece : block) {
            int distanceToHome = getDistanceToHome(currentPiece);
            if (distanceToHome > maxDistance) {
                maxDistance = distanceToHome;
                dominantPiece = currentPiece;
            }
        }
        
        System.out.println("  -> Opposite Block detected! Moving together. Roll: " + roll + " / " + block.size() + " pieces = " + stepsToMove + " steps.");
        System.out.println("  -> Dominant direction determined by piece " + dominantPiece.getId() + " (Distance to home: " + maxDistance + ").");
        
        int combinedBlockDirection = dominantPiece.isXChoiceDirectionClockwise() ? 1 : -1;
        
        // Update all pieces in the block to visually adopt the combined direction
        for (LudoPiece currentPiece : block) {
            currentPiece.setCombinedBlockDirectionClockwise(combinedBlockDirection == 1);
        }
        
        // Simulate block movement to check for blockages
        int temporaryPosition = block.get(0).getPosition();
        for (int stepIndex = 1; stepIndex <= stepsToMove; stepIndex++) {
            temporaryPosition = (temporaryPosition + combinedBlockDirection + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
            if (isPathBlockedByOpponent(standardPath[temporaryPosition], block.get(0).getColor(), block.size())) {
                System.out.println("  -> Block move completely blocked by an opponentPiece block at cell " + temporaryPosition + "!");
                return false;
            }
        }
        
        int finalPos = temporaryPosition;
        Cell startingCell = standardPath[block.get(0).getPosition()];
        boolean captured = false;
        
        // Handle captures ONCE for the entire block so everyone gets the credit
        Cell destinationCell = standardPath[finalPos];
        List<LudoPiece> opponentPieces = new ArrayList<>();
        for (LudoPiece currentPiece : destinationCell.getPieces()) {
            if (currentPiece.getColor() != block.get(0).getColor()) {
                opponentPieces.add(currentPiece);
            }
        }
        
        if (!opponentPieces.isEmpty()) {
            captured = true;
            for (LudoPiece opponentPiece : opponentPieces) {
                opponentPiece.resetToDefault();
                destinationCell.removePiece(opponentPiece);
                System.out.println("  -> CAPTURE! Block captured " + opponentPiece.getId() + "!");
            }
            
            // Increment captures for EVERY piece participating in this block!
            for (LudoPiece currentPiece : block) {
                currentPiece.incrementCaptures();
            }
        }
        
        // Remove pieces from starting cell and update their internal state
        for (LudoPiece currentPiece : block) {
            startingCell.removePiece(currentPiece);
            
            int piecePosition = currentPiece.getPosition();
            String pieceState = currentPiece.getState();
            int piecePasses = currentPiece.getApproachPasses();
            
            for (int stepIndex = 1; stepIndex <= stepsToMove; stepIndex++) {
                if (pieceState.equals("STANDARD")) {
                    boolean readyForHome = false;
                    if (piecePosition == getApproachIndex(currentPiece.getColor()) && currentPiece.getCaptures() >= 1) {
                        if (currentPiece.isXChoiceDirectionClockwise() && piecePasses >= 1) readyForHome = true;
                        if (!currentPiece.isXChoiceDirectionClockwise() && piecePasses >= 2) readyForHome = true;
                    }
                    
                    if (readyForHome) {
                        pieceState = "HOME_STRAIGHT";
                        piecePosition = 0;
                    } else {
                        piecePosition = (piecePosition + combinedBlockDirection + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
                        if (piecePosition == getApproachIndex(currentPiece.getColor())) {
                            if ((currentPiece.isXChoiceDirectionClockwise() && combinedBlockDirection == 1) || (!currentPiece.isXChoiceDirectionClockwise() && combinedBlockDirection == -1)) {
                                piecePasses++;
                            } else {
                                piecePasses = Math.max(0, piecePasses - 1);
                            }
                        }
                    }
                } else if (pieceState.equals("HOME_STRAIGHT")) {
                    int nextPos = piecePosition + 1;
                    if (nextPos <= HOME_STRAIGHT_LENGTH) {
                        piecePosition = nextPos;
                        if (piecePosition == HOME_STRAIGHT_LENGTH) {
                            pieceState = "HOME";
                        }
                    }
                }
            }
            
            currentPiece.setPosition(piecePosition);
            currentPiece.setState(pieceState);
            currentPiece.setApproachPasses(piecePasses);
            
            if (pieceState.equals("STANDARD")) {
                standardPath[finalPos].addPiece(currentPiece);
            } else {
                System.out.println("  -> Piece " + currentPiece.getId() + " broke off from the block and entered " + pieceState + " at position " + piecePosition);
            }
        }
        
        // TRIGGER MYSTERY CELL FOR OPPOSITE BLOCK
        if (finalPos == currentMysteryCellPosition && currentMysteryCellPosition != -1) {
            triggerMysteryCellBlockTeleport(block);
        }
        
        return captured;
    }

    private boolean moveSameWayBlock(List<LudoPiece> block, int roll) {
        LudoPiece dominant = block.get(0);
        int stepsToMove = AlphaEffect.calculateBlockEffectiveRoll(dominant, roll);
        
        System.out.println("  -> Same-Way Block moving with roll " + roll + ". Effective steps: " + stepsToMove);
        if (stepsToMove == 0) {
            System.out.println("  -> Block stays still.");
            return false;
        }

        int direction = dominant.isXChoiceDirectionClockwise() ? 1 : -1;

        int temporaryPosition = dominant.getPosition();
        for (int stepIndex = 1; stepIndex <= stepsToMove; stepIndex++) {
            temporaryPosition = (temporaryPosition + direction + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
            if (isPathBlockedByOpponent(standardPath[temporaryPosition], dominant.getColor(), block.size())) {
                System.out.println("  -> Block move completely blocked by an opponentPiece block at cell " + temporaryPosition + "!");
                return false;
            }
        }

        int finalPos = temporaryPosition;
        Cell startingCell = standardPath[dominant.getPosition()];
        boolean captured = false;
        
        Cell destinationCell = standardPath[finalPos];
        List<LudoPiece> opponentPieces = new ArrayList<>();
        for (LudoPiece currentPiece : destinationCell.getPieces()) {
            if (currentPiece.getColor() != dominant.getColor()) {
                opponentPieces.add(currentPiece);
            }
        }
        
        if (!opponentPieces.isEmpty()) {
            captured = true;
            for (LudoPiece opponentPiece : opponentPieces) {
                opponentPiece.resetToDefault();
                destinationCell.removePiece(opponentPiece);
                System.out.println("  -> CAPTURE! Block captured " + opponentPiece.getId() + "!");
            }
            for (LudoPiece currentPiece : block) {
                currentPiece.incrementCaptures();
            }
        }
        
        for (LudoPiece currentPiece : block) {
            startingCell.removePiece(currentPiece);
            int piecePosition = currentPiece.getPosition();
            String pieceState = currentPiece.getState();
            int piecePasses = currentPiece.getApproachPasses();
            
            for (int stepIndex = 1; stepIndex <= stepsToMove; stepIndex++) {
                if (pieceState.equals("STANDARD")) {
                    boolean readyForHome = false;
                    if (piecePosition == getApproachIndex(currentPiece.getColor()) && currentPiece.getCaptures() >= 1) {
                        if (currentPiece.isXChoiceDirectionClockwise() && piecePasses >= 1) readyForHome = true;
                        if (!currentPiece.isXChoiceDirectionClockwise() && piecePasses >= 2) readyForHome = true;
                    }
                    
                    if (readyForHome) {
                        pieceState = "HOME_STRAIGHT";
                        piecePosition = 0;
                    } else {
                        piecePosition = (piecePosition + direction + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
                        if (piecePosition == getApproachIndex(currentPiece.getColor())) {
                            piecePasses++;
                        }
                    }
                } else if (pieceState.equals("HOME_STRAIGHT")) {
                    int nextPos = piecePosition + 1;
                    if (nextPos <= HOME_STRAIGHT_LENGTH) {
                        piecePosition = nextPos;
                        if (piecePosition == HOME_STRAIGHT_LENGTH) {
                            pieceState = "HOME";
                        }
                    }
                }
            }
            
            currentPiece.setPosition(piecePosition);
            currentPiece.setState(pieceState);
            currentPiece.setApproachPasses(piecePasses);
            
            if (pieceState.equals("STANDARD")) {
                standardPath[finalPos].addPiece(currentPiece);
            } else {
                System.out.println("  -> Piece " + currentPiece.getId() + " broke off from the block and entered " + pieceState + " at position " + piecePosition);
            }
        }
        
        // TRIGGER MYSTERY CELL FOR SAME-WAY BLOCK
        if (finalPos == currentMysteryCellPosition && currentMysteryCellPosition != -1) {
            triggerMysteryCellBlockTeleport(block);
        }
        
        return captured;
    }

    // Moves a piece on the board by the exact dice roll amount. Returns true if an opponentPiece was captured.
    public boolean movePiece(LudoPiece piece, int roll, boolean tryMoveAsBlock) {
        if (piece.getState().equals("BASE") && roll == 6) {
            return moveFromBaseToStart(piece);
        } 
        else if (!piece.getState().equals("BASE")) {
            if (piece.getState().equals("STANDARD") && tryMoveAsBlock) {
                Cell currentCell = standardPath[piece.getPosition()];
                List<LudoPiece> piecesOnCell = currentCell.getPieces();
                
                boolean hasClockwisePiece = false;
                boolean hasCounterClockwisePiece = false;
                List<LudoPiece> blockPieces = new ArrayList<>();
                
                for (LudoPiece currentPiece : piecesOnCell) {
                    if (currentPiece.getColor() == piece.getColor() && currentPiece.getState().equals("STANDARD")) {
                        blockPieces.add(currentPiece);
                        if (currentPiece.isXChoiceDirectionClockwise()) hasClockwisePiece = true;
                        else hasCounterClockwisePiece = true;
                    }
                }
                
                if (blockPieces.size() >= 2) {
                    if (hasClockwisePiece && hasCounterClockwisePiece) {
                        return moveOppositeBlock(blockPieces, roll);
                    } else {
                        return moveSameWayBlock(blockPieces, roll);
                    }
                }
            }

            int temporaryPosition = piece.getPosition();
            String temporaryState = piece.getState();
            int temporaryPasses = piece.getApproachPasses();
            int approachIndex = getApproachIndex(piece.getColor());
            int direction = piece.isXChoiceDirectionClockwise() ? 1 : -1;
            
            int actualSteps = 0;
            
            int effectiveRoll = AlphaEffect.calculateEffectiveRoll(piece, roll);
            
            // Step-by-step path simulator
            for (int stepIndex = 1; stepIndex <= effectiveRoll; stepIndex++) {
                if (temporaryState.equals("STANDARD")) {
                    boolean readyForHome = false;
                    // A piece MUST have captured at least 1 opponentPiece to enter the Home Straight
                    if (temporaryPosition == approachIndex && piece.getCaptures() >= 1) {
                        if (piece.isXChoiceDirectionClockwise() && temporaryPasses >= 1) readyForHome = true;
                        if (!piece.isXChoiceDirectionClockwise() && temporaryPasses >= 2) readyForHome = true;
                    }
                    
                    if (readyForHome) {
                        temporaryState = "HOME_STRAIGHT";
                        temporaryPosition = 0;
                        actualSteps++;
                    } else {
                        int nextIndex = (temporaryPosition + direction + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
                        if (isPathBlockedByOpponent(standardPath[nextIndex], piece.getColor(), 1)) {
                            System.out.println("  -> Path blocked at cell " + nextIndex + "!");
                            break; // Stop simulating, but keep valid steps
                        }
                        temporaryPosition = nextIndex;
                        if (temporaryPosition == approachIndex) {
                            temporaryPasses++; // Record that we've passed the approach cell
                        }
                        actualSteps++;
                    }
                } else if (temporaryState.equals("HOME_STRAIGHT")) {
                    int nextPos = temporaryPosition + 1;
                    if (nextPos <= HOME_STRAIGHT_LENGTH) {
                        temporaryPosition = nextPos;
                        actualSteps++;
                    } else {
                        System.out.println("  -> Move invalid: Overshoots home.");
                        return false; // Abort the entire move
                    }
                }
            }

            if (actualSteps == 0) {
                System.out.println("  -> Move completely blocked! Piece " + piece.getId() + " cannot move.");
                return false;
            }
            
            if (actualSteps < effectiveRoll) {
                System.out.println("  -> Piece " + piece.getId() + " can only move " + actualSteps + " steps due to block.");
            }

            // Commit the successful move
            removeFromCurrentCell(piece);
            piece.setState(temporaryState);
            piece.setPosition(temporaryPosition);
            piece.setApproachPasses(temporaryPasses);

            if (temporaryState.equals("HOME_STRAIGHT")) {
                if (temporaryPosition == HOME_STRAIGHT_LENGTH) {
                    reachHome(piece);
                } else {
                    getHomeStraight(piece.getColor())[temporaryPosition].addPiece(piece);
                }
                return false; // No captures inside home straight
            } else {
                return handleStandardCellLanding(piece, temporaryPosition);
            }
        }
        return false;
    }

    // Approach cell says each color can move from standard cell to Home Straight.
    public void moveToHomeStraight(LudoPiece piece) {
        if (piece.getState().equals("STANDARD") && piece.getPosition() == getApproachIndex(piece.getColor())) {
            piece.setState("HOME_STRAIGHT");
            piece.setPosition(0);
        }
    }

    // After a piece reaches home it will be removed from the game.
    public void reachHome(LudoPiece piece) {
        if (piece.getState().equals("HOME_STRAIGHT") && piece.getPosition() == HOME_STRAIGHT_LENGTH) {
            piece.setState("HOME");
            piece.setPosition(LudoPiece.POSITION_REMOVED);
        }
    }
    
    // Helper to find the "X" start cell index based on the 13 cell spacing
    private int getStartIndex(PlayerColor color) {
        switch (color) {
            case YELLOW: return 2;
            case BLUE: return 15;
            case RED: return 28;
            case GREEN: return 41;
            default: return 2;
        }
    }
    
    // Helper to find the "Approach" circle index (cell right before entering home)
    private int getApproachIndex(PlayerColor color) {
        switch (color) {
            case YELLOW: return 0;
            case BLUE: return 13;
            case RED: return 26;
            case GREEN: return 39;
            default: return 0;
        }
    }

    public boolean tryBreakBlockadeForConsecutiveSixes(players.Player player) {
        for (int cellIndex = 0; cellIndex < STANDARD_PATH_LENGTH; cellIndex++) {
            Cell cell = standardPath[cellIndex];
            List<LudoPiece> playerPieces = new ArrayList<>();
            for (LudoPiece currentPiece : cell.getPieces()) {
                if (currentPiece.getColor() == player.getColor() && currentPiece.getState().equals("STANDARD")) {
                    playerPieces.add(currentPiece);
                }
            }
            
            if (playerPieces.size() >= 2) {
                // Found a blockade! Break it.
                System.out.println("  -> Breaking blockade at cell " + cellIndex);
                
                // Keep the first piece at the current cell.
                // Move the rest exactly 6 cells in their original direction.
                for (int pieceIndex = 1; pieceIndex < playerPieces.size(); pieceIndex++) {
                    LudoPiece movingPiece = playerPieces.get(pieceIndex);
                    // Use a special move that ignores path blockages!
                    forceMovePiece(movingPiece, 6);
                }
                return true;
            }
        }
        return false;
    }

    private void forceMovePiece(LudoPiece piece, int roll) {
        int temporaryPosition = piece.getPosition();
        String temporaryState = piece.getState();
        int temporaryPasses = piece.getApproachPasses();
        int direction = piece.isXChoiceDirectionClockwise() ? 1 : -1;
        int approachIndex = getApproachIndex(piece.getColor());
        
        System.out.println("  -> Forcing piece " + piece.getId() + " to move " + roll + " cells " + (direction == 1 ? "Clockwise" : "Counter-Clockwise") + "...");
        
        for (int stepIndex = 1; stepIndex <= roll; stepIndex++) {
            if (temporaryState.equals("STANDARD")) {
                boolean readyForHome = false;
                if (temporaryPosition == approachIndex && piece.getCaptures() >= 1) {
                    if (piece.isXChoiceDirectionClockwise() && temporaryPasses >= 1) readyForHome = true;
                    if (!piece.isXChoiceDirectionClockwise() && temporaryPasses >= 2) readyForHome = true;
                }
                
                if (readyForHome) {
                    temporaryState = "HOME_STRAIGHT";
                    temporaryPosition = 0;
                } else {
                    temporaryPosition = (temporaryPosition + direction + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
                    if (temporaryPosition == approachIndex) {
                        temporaryPasses++;
                    }
                }
            } else if (temporaryState.equals("HOME_STRAIGHT")) {
                int nextPos = temporaryPosition + 1;
                if (nextPos <= HOME_STRAIGHT_LENGTH) {
                    temporaryPosition = nextPos;
                    if (temporaryPosition == HOME_STRAIGHT_LENGTH) {
                        temporaryState = "HOME";
                    }
                }
            }
        }
        
        // Remove from old position BEFORE updating
        if (piece.getState().equals("STANDARD")) {
            standardPath[piece.getPosition()].removePiece(piece);
        }
        
        piece.setPosition(temporaryPosition);
        piece.setState(temporaryState);
        piece.setApproachPasses(temporaryPasses);
        
        if (temporaryState.equals("STANDARD")) {
            handleStandardCellLanding(piece, temporaryPosition);
        } else {
            System.out.println("  -> Piece " + piece.getId() + " forcibly moved into " + temporaryState + " at position " + temporaryPosition);
        }
    }

    public boolean hasPiecesOnStandardPath() {
        for (Cell cell : standardPath) {
            if (!cell.getPieces().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void spawnMysteryCell() {
        List<Integer> emptyCells = new ArrayList<>();
        for (int cellIndex = 0; cellIndex < STANDARD_PATH_LENGTH; cellIndex++) {
            if (standardPath[cellIndex].getPieces().isEmpty() && cellIndex != previousMysteryCellPosition) {
                emptyCells.add(cellIndex);
            }
        }
        
        if (!emptyCells.isEmpty()) {
            int index = new Random().nextInt(emptyCells.size());
            currentMysteryCellPosition = emptyCells.get(index);
            previousMysteryCellPosition = currentMysteryCellPosition;
            System.out.println("\n*** A Mystery Cell has appeared at Cell(" + currentMysteryCellPosition + ")! ***\n");
        }
    }

    public int getMysteryCellPosition() {
        return currentMysteryCellPosition;
    }

    private boolean triggerMysteryCellEffect(LudoPiece piece) {
        int option = new Random().nextInt(6) + 1;
        System.out.println("  -> Mystery Cell activates Option " + option + "!");
        
        switch (option) {
            case 1:
                System.out.println("  -> Teleporting to Alpha (Cell 9)");
                piece.setPosition(9);
                boolean didCapture = handleStandardCellLanding(piece, 9, true);
                AlphaEffect.applyIndividual(piece);
                return didCapture;
            case 2:
                System.out.println("  -> Teleporting to Beta (Cell 27)");
                piece.setPosition(27);
                BetaEffect.applyIndividual(piece);
                return handleStandardCellLanding(piece, 27, true);
            case 3:
                int optionChoice = GammaEffect.determineTeleportOption(piece.isXChoiceDirectionClockwise());
                if (optionChoice == 3) {
                    piece.setPosition(46);
                    GammaEffect.applyIndividual(piece);
                    return handleStandardCellLanding(piece, 46, true);
                } else {
                    piece.setPosition(27);
                    BetaEffect.applyIndividual(piece);
                    return handleStandardCellLanding(piece, 27, true);
                }
            case 4:
                System.out.println("  -> Teleporting to BASE!");
                piece.resetToDefault();
                return false;
            case 5:
                int startCellIndex = getStartIndex(piece.getColor());
                System.out.println("  -> Teleporting to X (" + piece.getColor() + " start cell " + startCellIndex + ")");
                piece.setPosition(startCellIndex);
                piece.setApproachPasses(0);
                return handleStandardCellLanding(piece, startCellIndex, true);
            case 6:
                int approachCellIndex = getApproachIndex(piece.getColor());
                System.out.println("  -> Teleporting to Approach (" + piece.getColor() + " approach cell " + approachCellIndex + ")");
                piece.setPosition(approachCellIndex);
                return handleStandardCellLanding(piece, approachCellIndex, true);
            default:
                return false;
        }
    }

    private void triggerMysteryCellBlockTeleport(List<LudoPiece> block) {
        System.out.println("  -> The Block landed on the Mystery Cell!");
        int option = new Random().nextInt(6) + 1;
        System.out.println("  -> Mystery Cell activates Option " + option + " for the entire block!");

        if (option == 4) { 
            System.out.println("  -> Teleporting Block to BASE!");
            for (LudoPiece currentPiece : block) {
                if (currentPiece.getState().equals("STANDARD")) {
                    standardPath[currentPiece.getPosition()].removePiece(currentPiece);
                    currentPiece.resetToDefault();
                }
            }
            return;
        }

        LudoPiece dominantPiece = block.get(0);
        int minDistance = Integer.MAX_VALUE;
        for (LudoPiece currentPiece : block) {
            int distanceToHome = getDistanceToHome(currentPiece);
            if (distanceToHome < minDistance) {
                minDistance = distanceToHome;
                dominantPiece = currentPiece;
            }
        }
        boolean isBlockClockwise = dominantPiece.isXChoiceDirectionClockwise();

        if (option == 3) {
            option = GammaEffect.determineTeleportOption(isBlockClockwise);
            if (option == 3) {
                GammaEffect.applyBlock(block);
            }
        }

        int destinationPosition = -1;
        switch (option) {
            case 1: destinationPosition = 9; break; 
            case 2: destinationPosition = 27; break; 
            case 3: destinationPosition = 46; break; 
            case 5: destinationPosition = getStartIndex(block.get(0).getColor()); break; 
            case 6: destinationPosition = getApproachIndex(block.get(0).getColor()); break; 
        }

        System.out.println("  -> Teleporting Block to Cell " + destinationPosition);

        for (LudoPiece currentPiece : block) {
            if (currentPiece.getState().equals("STANDARD")) {
                standardPath[currentPiece.getPosition()].removePiece(currentPiece);
                currentPiece.setPosition(destinationPosition);
                if (option == 5) currentPiece.setApproachPasses(0);
                standardPath[destinationPosition].addPiece(currentPiece);
            }
        }
        
        if (option == 1) {
            AlphaEffect.applyBlock(block);
        } else if (option == 2) {
            BetaEffect.applyBlock(block);
        }

        Cell destinationCell = standardPath[destinationPosition];
        List<LudoPiece> opponentPieces = new ArrayList<>();
        for (LudoPiece currentPiece : destinationCell.getPieces()) {
            if (currentPiece.getColor() != block.get(0).getColor()) {
                opponentPieces.add(currentPiece);
            }
        }
        if (!opponentPieces.isEmpty()) {
            for (LudoPiece opponentPiece : opponentPieces) {
                opponentPiece.resetToDefault();
                destinationCell.removePiece(opponentPiece);
                System.out.println("  -> CAPTURE! Teleported Block captured " + opponentPiece.getId() + "!");
            }
            for (LudoPiece currentPiece : block) {
                currentPiece.incrementCaptures();
            }
        }


    }

}
package core;

import players.LudoPiece;
import players.PlayerColor;

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
        for (int i = 0; i < STANDARD_PATH_LENGTH; i++) {
            boolean isStart = (i == 2 || i == 15 || i == 28 || i == 41);
            boolean isApproach = (i == 0 || i == 13 || i == 26 || i == 39);
            String cellId = String.valueOf(i);
            standardPath[i] = new Cell(cellId, i, (PlayerColor) null, isStart, isApproach);
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
        
        for (int i = 0; i < HOME_STRAIGHT_LENGTH; i++) {
            String cellId = colorPrefix + "home path" + i;
            straight[i] = new Cell(cellId, i, color, false, false);
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

    // Handles landing on a standard cell and capturing opponents
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
        java.util.List<LudoPiece> piecesOnCell = new java.util.ArrayList<>(destinationCell.getPieces());
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
        java.util.Map<PlayerColor, Integer> colorCounts = new java.util.HashMap<>();
        for (LudoPiece p : cell.getPieces()) {
            if (p.getColor() != movingColor && p.getState().equals("STANDARD")) {
                colorCounts.put(p.getColor(), colorCounts.getOrDefault(p.getColor(), 0) + 1);
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
                System.out.println("  -> Cannot move out of BASE! Start cell " + startPos + " is BLOCKED by an opponent block.");
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
        
        int tempPos = piece.getPosition();
        String tempState = piece.getState();
        int tempPasses = piece.getApproachPasses();
        int approachIndex = getApproachIndex(piece.getColor());
        int direction = piece.isXChoiceDirectionClockwise() ? 1 : -1;
        
        int steps = 0;
        while (!tempState.equals("HOME")) {
            if (steps > 100) return 100; // Failsafe
            if (tempState.equals("STANDARD")) {
                boolean readyForHome = false;
                // Ignore capture rule here to avoid infinite loop
                if (tempPos == approachIndex) {
                    if (piece.isXChoiceDirectionClockwise() && tempPasses >= 1) readyForHome = true;
                    if (!piece.isXChoiceDirectionClockwise() && tempPasses >= 2) readyForHome = true;
                }
                
                if (readyForHome) {
                    tempState = "HOME_STRAIGHT";
                    tempPos = 0;
                } else {
                    tempPos = (tempPos + direction + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
                    if (tempPos == approachIndex) {
                        tempPasses++;
                    }
                }
            } else if (tempState.equals("HOME_STRAIGHT")) {
                tempPos++;
                if (tempPos >= HOME_STRAIGHT_LENGTH) {
                    tempState = "HOME";
                }
            }
            steps++;
        }
        return steps;
    }

    private boolean moveOppositeBlock(java.util.List<LudoPiece> block, int roll) {
        LudoPiece dominant = block.get(0);
        int baseStepsToMove = roll / block.size();
        int stepsToMove = baseStepsToMove;
        
        if (dominant.getBlockAuraRoundsRemaining() > 0) {
            if ("ENERGIZED".equals(dominant.getBlockAuraEffect())) {
                stepsToMove = baseStepsToMove * 2;
            } else if ("SICK".equals(dominant.getBlockAuraEffect())) {
                stepsToMove = baseStepsToMove / 2;
            }
        }
        
        System.out.println("  -> Opposite Block tried to move with roll " + roll + ". Base division results in " + baseStepsToMove + ". Effective steps: " + stepsToMove);
        if (stepsToMove == 0) {return false;
        }
        
        // Determine longest distance from home
        int maxDistance = -1;
        LudoPiece dominantPiece = null;
        for (LudoPiece p : block) {
            int dist = getDistanceToHome(p);
            if (dist > maxDistance) {
                maxDistance = dist;
                dominantPiece = p;
            }
        }
        
        System.out.println("  -> Opposite Block detected! Moving together. Roll: " + roll + " / " + block.size() + " pieces = " + stepsToMove + " steps.");
        System.out.println("  -> Dominant direction determined by piece " + dominantPiece.getId() + " (Distance to home: " + maxDistance + ").");
        
        int combinedBlockDirection = dominantPiece.isXChoiceDirectionClockwise() ? 1 : -1;
        
        // Update all pieces in the block to visually adopt the combined direction
        for (LudoPiece p : block) {
            p.setCombinedBlockDirectionClockwise(combinedBlockDirection == 1);
        }
        
        // Simulate block movement to check for blockages
        int tempPos = block.get(0).getPosition();
        for (int i = 1; i <= stepsToMove; i++) {
            tempPos = (tempPos + combinedBlockDirection + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
            if (isPathBlockedByOpponent(standardPath[tempPos], block.get(0).getColor(), block.size())) {
                System.out.println("  -> Block move completely blocked by an opponent block at cell " + tempPos + "!");
                return false;
            }
        }
        
        int finalPos = tempPos;
        Cell startingCell = standardPath[block.get(0).getPosition()];
        boolean captured = false;
        
        // Handle captures ONCE for the entire block so everyone gets the credit
        Cell destCell = standardPath[finalPos];
        java.util.List<LudoPiece> opponents = new java.util.ArrayList<>();
        for (LudoPiece p : destCell.getPieces()) {
            if (p.getColor() != block.get(0).getColor()) {
                opponents.add(p);
            }
        }
        
        if (!opponents.isEmpty()) {
            captured = true;
            for (LudoPiece opponent : opponents) {
                opponent.resetToDefault();
                destCell.removePiece(opponent);
                System.out.println("  -> CAPTURE! Block captured " + opponent.getId() + "!");
            }
            
            // Increment captures for EVERY piece participating in this block!
            for (LudoPiece p : block) {
                p.incrementCaptures();
            }
        }
        
        // Remove pieces from starting cell and update their internal state
        for (LudoPiece p : block) {
            startingCell.removePiece(p);
            
            int pPos = p.getPosition();
            String pState = p.getState();
            int pPasses = p.getApproachPasses();
            
            for (int i = 1; i <= stepsToMove; i++) {
                if (pState.equals("STANDARD")) {
                    boolean readyForHome = false;
                    if (pPos == getApproachIndex(p.getColor()) && p.getCaptures() >= 1) {
                        if (p.isXChoiceDirectionClockwise() && pPasses >= 1) readyForHome = true;
                        if (!p.isXChoiceDirectionClockwise() && pPasses >= 2) readyForHome = true;
                    }
                    
                    if (readyForHome) {
                        pState = "HOME_STRAIGHT";
                        pPos = 0;
                    } else {
                        pPos = (pPos + combinedBlockDirection + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
                        if (pPos == getApproachIndex(p.getColor())) {
                            if ((p.isXChoiceDirectionClockwise() && combinedBlockDirection == 1) || (!p.isXChoiceDirectionClockwise() && combinedBlockDirection == -1)) {
                                pPasses++;
                            } else {
                                pPasses = Math.max(0, pPasses - 1);
                            }
                        }
                    }
                } else if (pState.equals("HOME_STRAIGHT")) {
                    int nextPos = pPos + 1;
                    if (nextPos <= HOME_STRAIGHT_LENGTH) {
                        pPos = nextPos;
                        if (pPos == HOME_STRAIGHT_LENGTH) {
                            pState = "HOME";
                        }
                    }
                }
            }
            
            p.setPosition(pPos);
            p.setState(pState);
            p.setApproachPasses(pPasses);
            
            if (pState.equals("STANDARD")) {
                standardPath[finalPos].addPiece(p);
            } else {
                System.out.println("  -> Piece " + p.getId() + " broke off from the block and entered " + pState + " at position " + pPos);
            }
        }
        
        // TRIGGER MYSTERY CELL FOR OPPOSITE BLOCK
        if (finalPos == currentMysteryCellPosition && currentMysteryCellPosition != -1) {
            triggerMysteryCellBlockTeleport(block);
        }
        
        return captured;
    }

    private boolean moveSameWayBlock(java.util.List<LudoPiece> block, int roll) {
        LudoPiece dominant = block.get(0);
        int stepsToMove = roll;
        
        if (dominant.getBlockAuraRoundsRemaining() > 0) {
            if ("ENERGIZED".equals(dominant.getBlockAuraEffect())) {
                stepsToMove = roll * 2;
            } else if ("SICK".equals(dominant.getBlockAuraEffect())) {
                stepsToMove = roll / 2;
            }
        }
        
        System.out.println("  -> Same-Way Block moving with roll " + roll + ". Effective steps: " + stepsToMove);
        if (stepsToMove == 0) {
            System.out.println("  -> Block stays still.");
            return false;
        }

        int direction = dominant.isXChoiceDirectionClockwise() ? 1 : -1;

        int tempPos = dominant.getPosition();
        for (int i = 1; i <= stepsToMove; i++) {
            tempPos = (tempPos + direction + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
            if (isPathBlockedByOpponent(standardPath[tempPos], dominant.getColor(), block.size())) {
                System.out.println("  -> Block move completely blocked by an opponent block at cell " + tempPos + "!");
                return false;
            }
        }

        int finalPos = tempPos;
        Cell startingCell = standardPath[dominant.getPosition()];
        boolean captured = false;
        
        Cell destCell = standardPath[finalPos];
        java.util.List<LudoPiece> opponents = new java.util.ArrayList<>();
        for (LudoPiece p : destCell.getPieces()) {
            if (p.getColor() != dominant.getColor()) {
                opponents.add(p);
            }
        }
        
        if (!opponents.isEmpty()) {
            captured = true;
            for (LudoPiece opponent : opponents) {
                opponent.resetToDefault();
                destCell.removePiece(opponent);
                System.out.println("  -> CAPTURE! Block captured " + opponent.getId() + "!");
            }
            for (LudoPiece p : block) {
                p.incrementCaptures();
            }
        }
        
        for (LudoPiece p : block) {
            startingCell.removePiece(p);
            int pPos = p.getPosition();
            String pState = p.getState();
            int pPasses = p.getApproachPasses();
            
            for (int i = 1; i <= stepsToMove; i++) {
                if (pState.equals("STANDARD")) {
                    boolean readyForHome = false;
                    if (pPos == getApproachIndex(p.getColor()) && p.getCaptures() >= 1) {
                        if (p.isXChoiceDirectionClockwise() && pPasses >= 1) readyForHome = true;
                        if (!p.isXChoiceDirectionClockwise() && pPasses >= 2) readyForHome = true;
                    }
                    
                    if (readyForHome) {
                        pState = "HOME_STRAIGHT";
                        pPos = 0;
                    } else {
                        pPos = (pPos + direction + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
                        if (pPos == getApproachIndex(p.getColor())) {
                            pPasses++;
                        }
                    }
                } else if (pState.equals("HOME_STRAIGHT")) {
                    int nextPos = pPos + 1;
                    if (nextPos <= HOME_STRAIGHT_LENGTH) {
                        pPos = nextPos;
                        if (pPos == HOME_STRAIGHT_LENGTH) {
                            pState = "HOME";
                        }
                    }
                }
            }
            
            p.setPosition(pPos);
            p.setState(pState);
            p.setApproachPasses(pPasses);
            
            if (pState.equals("STANDARD")) {
                standardPath[finalPos].addPiece(p);
            } else {
                System.out.println("  -> Piece " + p.getId() + " broke off from the block and entered " + pState + " at position " + pPos);
            }
        }
        
        // TRIGGER MYSTERY CELL FOR SAME-WAY BLOCK
        if (finalPos == currentMysteryCellPosition && currentMysteryCellPosition != -1) {
            triggerMysteryCellBlockTeleport(block);
        }
        
        return captured;
    }

    // Moves a piece on the board by the exact dice roll amount. Returns true if an opponent was captured.
    public boolean movePiece(LudoPiece piece, int roll, boolean tryMoveAsBlock) {
        if (piece.getState().equals("BASE") && roll == 6) {
            return moveFromBaseToStart(piece);
        } 
        else if (!piece.getState().equals("BASE")) {
            if (piece.getState().equals("STANDARD") && tryMoveAsBlock) {
                Cell currentCell = standardPath[piece.getPosition()];
                java.util.List<LudoPiece> piecesOnCell = currentCell.getPieces();
                
                boolean hasClockwise = false;
                boolean hasCounter = false;
                java.util.List<LudoPiece> blockPieces = new java.util.ArrayList<>();
                
                for (LudoPiece p : piecesOnCell) {
                    if (p.getColor() == piece.getColor() && p.getState().equals("STANDARD")) {
                        blockPieces.add(p);
                        if (p.isXChoiceDirectionClockwise()) hasClockwise = true;
                        else hasCounter = true;
                    }
                }
                
                if (blockPieces.size() >= 2) {
                    if (hasClockwise && hasCounter) {
                        return moveOppositeBlock(blockPieces, roll);
                    } else {
                        return moveSameWayBlock(blockPieces, roll);
                    }
                }
            }

            int tempPos = piece.getPosition();
            String tempState = piece.getState();
            int tempPasses = piece.getApproachPasses();
            int approachIndex = getApproachIndex(piece.getColor());
            int direction = piece.isXChoiceDirectionClockwise() ? 1 : -1;
            
            int actualSteps = 0;
            
            int effectiveRoll = roll;
            if (piece.getIndividualAuraRoundsRemaining() > 0) {
                if ("ENERGIZED".equals(piece.getIndividualAuraEffect())) {
                    effectiveRoll = roll * 2;
                } else if ("SICK".equals(piece.getIndividualAuraEffect())) {
                    effectiveRoll = roll / 2;
                }
                System.out.println("  -> Piece " + piece.getId() + " has " + piece.getIndividualAuraEffect() + " aura! Effective roll is " + effectiveRoll);
            }
            
            // Step-by-step path simulator
            for (int i = 1; i <= effectiveRoll; i++) {
                if (tempState.equals("STANDARD")) {
                    boolean readyForHome = false;
                    // A piece MUST have captured at least 1 opponent to enter the Home Straight
                    if (tempPos == approachIndex && piece.getCaptures() >= 1) {
                        if (piece.isXChoiceDirectionClockwise() && tempPasses >= 1) readyForHome = true;
                        if (!piece.isXChoiceDirectionClockwise() && tempPasses >= 2) readyForHome = true;
                    }
                    
                    if (readyForHome) {
                        tempState = "HOME_STRAIGHT";
                        tempPos = 0;
                        actualSteps++;
                    } else {
                        int nextIndex = (tempPos + direction + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
                        if (isPathBlockedByOpponent(standardPath[nextIndex], piece.getColor(), 1)) {
                            System.out.println("  -> Path blocked at cell " + nextIndex + "!");
                            break; // Stop simulating, but keep valid steps
                        }
                        tempPos = nextIndex;
                        if (tempPos == approachIndex) {
                            tempPasses++; // Record that we've passed the approach cell
                        }
                        actualSteps++;
                    }
                } else if (tempState.equals("HOME_STRAIGHT")) {
                    int nextPos = tempPos + 1;
                    if (nextPos <= HOME_STRAIGHT_LENGTH) {
                        tempPos = nextPos;
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
            piece.setState(tempState);
            piece.setPosition(tempPos);
            piece.setApproachPasses(tempPasses);

            if (tempState.equals("HOME_STRAIGHT")) {
                if (tempPos == HOME_STRAIGHT_LENGTH) {
                    reachHome(piece);
                } else {
                    getHomeStraight(piece.getColor())[tempPos].addPiece(piece);
                }
                return false; // No captures inside home straight
            } else {
                return handleStandardCellLanding(piece, tempPos);
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
        for (int i = 0; i < STANDARD_PATH_LENGTH; i++) {
            Cell cell = standardPath[i];
            java.util.List<LudoPiece> myPieces = new java.util.ArrayList<>();
            for (LudoPiece p : cell.getPieces()) {
                if (p.getColor() == player.getColor() && p.getState().equals("STANDARD")) {
                    myPieces.add(p);
                }
            }
            
            if (myPieces.size() >= 2) {
                // Found a blockade! Break it.
                System.out.println("  -> Breaking blockade at cell " + i);
                
                // Keep the first piece at the current cell.
                // Move the rest exactly 6 cells in their original direction.
                for (int j = 1; j < myPieces.size(); j++) {
                    LudoPiece movingPiece = myPieces.get(j);
                    // Use a special move that ignores path blockages!
                    forceMovePiece(movingPiece, 6);
                }
                return true;
            }
        }
        return false;
    }

    private void forceMovePiece(LudoPiece piece, int roll) {
        int tempPos = piece.getPosition();
        String tempState = piece.getState();
        int tempPasses = piece.getApproachPasses();
        int direction = piece.isXChoiceDirectionClockwise() ? 1 : -1;
        int approachIndex = getApproachIndex(piece.getColor());
        
        System.out.println("  -> Forcing piece " + piece.getId() + " to move " + roll + " cells " + (direction == 1 ? "Clockwise" : "Counter-Clockwise") + "...");
        
        for (int i = 1; i <= roll; i++) {
            if (tempState.equals("STANDARD")) {
                boolean readyForHome = false;
                if (tempPos == approachIndex && piece.getCaptures() >= 1) {
                    if (piece.isXChoiceDirectionClockwise() && tempPasses >= 1) readyForHome = true;
                    if (!piece.isXChoiceDirectionClockwise() && tempPasses >= 2) readyForHome = true;
                }
                
                if (readyForHome) {
                    tempState = "HOME_STRAIGHT";
                    tempPos = 0;
                } else {
                    tempPos = (tempPos + direction + STANDARD_PATH_LENGTH) % STANDARD_PATH_LENGTH;
                    if (tempPos == approachIndex) {
                        tempPasses++;
                    }
                }
            } else if (tempState.equals("HOME_STRAIGHT")) {
                int nextPos = tempPos + 1;
                if (nextPos <= HOME_STRAIGHT_LENGTH) {
                    tempPos = nextPos;
                    if (tempPos == HOME_STRAIGHT_LENGTH) {
                        tempState = "HOME";
                    }
                }
            }
        }
        
        // Remove from old position BEFORE updating
        if (piece.getState().equals("STANDARD")) {
            standardPath[piece.getPosition()].removePiece(piece);
        }
        
        piece.setPosition(tempPos);
        piece.setState(tempState);
        piece.setApproachPasses(tempPasses);
        
        if (tempState.equals("STANDARD")) {
            handleStandardCellLanding(piece, tempPos);
        } else {
            System.out.println("  -> Piece " + piece.getId() + " forcibly moved into " + tempState + " at position " + tempPos);
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
        java.util.List<Integer> emptyCells = new java.util.ArrayList<>();
        for (int i = 0; i < STANDARD_PATH_LENGTH; i++) {
            if (standardPath[i].getPieces().isEmpty() && i != previousMysteryCellPosition) {
                emptyCells.add(i);
            }
        }
        
        if (!emptyCells.isEmpty()) {
            int index = new java.util.Random().nextInt(emptyCells.size());
            currentMysteryCellPosition = emptyCells.get(index);
            previousMysteryCellPosition = currentMysteryCellPosition;
            System.out.println("\n*** A Mystery Cell has appeared at Cell(" + currentMysteryCellPosition + ")! ***\n");
        }
    }

    public int getMysteryCellPosition() {
        return currentMysteryCellPosition;
    }

    private boolean triggerMysteryCellEffect(LudoPiece piece) {
        int option = new java.util.Random().nextInt(6) + 1;
        System.out.println("  -> Mystery Cell activates Option " + option + "!");
        
        switch (option) {
            case 1:
                System.out.println("  -> Teleporting to Alpha (Cell 9)");
                piece.setPosition(9);
                boolean cap1 = handleStandardCellLanding(piece, 9, true);
                System.out.println("  *** Alpha Aura Effect Activated! ***");
                boolean indCoin = utils.CoinFlip.getInstance().flip();
                String indAura = indCoin ? "ENERGIZED" : "SICK";
                System.out.println("  -> Piece " + piece.getId() + " Individual Aura Coin Toss: " + (indCoin ? "Heads (ENERGIZED)" : "Tails (SICK)"));
                piece.setIndividualAuraEffect(indAura);
                piece.setIndividualAuraRoundsRemaining(4);
                return cap1;
            case 2:
                System.out.println("  -> Teleporting to Beta (Cell 27)");
                piece.setPosition(27);
                piece.setBetaFreezeRoundsRemaining(4);
                return handleStandardCellLanding(piece, 27, true);
            case 3:
                if (piece.isXChoiceDirectionClockwise()) {
                    System.out.println("  -> Piece was moving Clockwise. Teleporting to Gamma (Cell 46) and changing direction!");
                    piece.setPosition(46);
                    piece.setXChoiceDirectionClockwise(false);
                    piece.setCombinedBlockDirectionClockwise(false);
                    piece.setBreakBlockDirectionClockwise(false);
                    return handleStandardCellLanding(piece, 46, true);
                } else {
                    System.out.println("  -> Piece was moving Counter-Clockwise. Teleport hijacked! Sent to Beta (Cell 27) instead.");
                    piece.setPosition(27);
                    piece.setBetaFreezeRoundsRemaining(4);
                    return handleStandardCellLanding(piece, 27, true);
                }
            case 4:
                System.out.println("  -> Teleporting to BASE!");
                piece.resetToDefault();
                return false;
            case 5:
                int xIndex = getStartIndex(piece.getColor());
                System.out.println("  -> Teleporting to X (" + piece.getColor() + " start cell " + xIndex + ")");
                piece.setPosition(xIndex);
                piece.setApproachPasses(0);
                return handleStandardCellLanding(piece, xIndex, true);
            case 6:
                int appIndex = getApproachIndex(piece.getColor());
                System.out.println("  -> Teleporting to Approach (" + piece.getColor() + " approach cell " + appIndex + ")");
                piece.setPosition(appIndex);
                return handleStandardCellLanding(piece, appIndex, true);
            default:
                return false;
        }
    }

    private void triggerMysteryCellBlockTeleport(java.util.List<LudoPiece> block) {
        System.out.println("  -> The Block landed on the Mystery Cell!");
        int option = new java.util.Random().nextInt(6) + 1;
        System.out.println("  -> Mystery Cell activates Option " + option + " for the entire block!");

        if (option == 4) { 
            System.out.println("  -> Teleporting Block to BASE!");
            for (LudoPiece p : block) {
                if (p.getState().equals("STANDARD")) {
                    standardPath[p.getPosition()].removePiece(p);
                    p.resetToDefault();
                }
            }
            return;
        }

        LudoPiece dominantPiece = block.get(0);
        int minDistance = Integer.MAX_VALUE;
        for (LudoPiece p : block) {
            int dist = getDistanceToHome(p);
            if (dist < minDistance) {
                minDistance = dist;
                dominantPiece = p;
            }
        }
        boolean blockIsClockwise = dominantPiece.isXChoiceDirectionClockwise();

        if (option == 3) {
            if (blockIsClockwise) {
                System.out.println("  -> Block was moving Clockwise. Teleporting to Gamma (Cell 46) and changing direction!");
                for (LudoPiece p : block) {
                    p.setXChoiceDirectionClockwise(false);
                    p.setCombinedBlockDirectionClockwise(false);
                    p.setBreakBlockDirectionClockwise(false);
                }
            } else {
                System.out.println("  -> Block was moving Counter-Clockwise. Teleport hijacked! Sent to Beta (Cell 27) instead.");
                option = 2; // Hijack to Beta
            }
        }

        int destPos = -1;
        switch (option) {
            case 1: destPos = 9; break; 
            case 2: destPos = 27; break; 
            case 3: destPos = 46; break; 
            case 5: destPos = getStartIndex(block.get(0).getColor()); break; 
            case 6: destPos = getApproachIndex(block.get(0).getColor()); break; 
        }

        System.out.println("  -> Teleporting Block to Cell " + destPos);

        for (LudoPiece p : block) {
            if (p.getState().equals("STANDARD")) {
                standardPath[p.getPosition()].removePiece(p);
                p.setPosition(destPos);
                if (option == 5) p.setApproachPasses(0);
                if (option == 2) p.setBetaFreezeRoundsRemaining(4);
                standardPath[destPos].addPiece(p);
            }
        }

        Cell destCell = standardPath[destPos];
        java.util.List<LudoPiece> opponents = new java.util.ArrayList<>();
        for (LudoPiece p : destCell.getPieces()) {
            if (p.getColor() != block.get(0).getColor()) {
                opponents.add(p);
            }
        }
        if (!opponents.isEmpty()) {
            for (LudoPiece opponent : opponents) {
                opponent.resetToDefault();
                destCell.removePiece(opponent);
                System.out.println("  -> CAPTURE! Teleported Block captured " + opponent.getId() + "!");
            }
            for (LudoPiece p : block) {
                p.incrementCaptures();
            }
        }

        if (option == 1) {
            System.out.println("  *** Alpha Aura Effect Activated! ***");
            boolean blockCoin = utils.CoinFlip.getInstance().flip();
            String blockAura = blockCoin ? "ENERGIZED" : "SICK";
            System.out.println("  -> Block Aura Coin Toss: " + (blockCoin ? "Heads (ENERGIZED)" : "Tails (SICK)"));

            for (LudoPiece p : block) {
                boolean indCoin = utils.CoinFlip.getInstance().flip();
                String indAura = indCoin ? "ENERGIZED" : "SICK";
                System.out.println("  -> Piece " + p.getId() + " Individual Aura Coin Toss: " + (indCoin ? "Heads (ENERGIZED)" : "Tails (SICK)"));
                
                p.setIndividualAuraEffect(indAura);
                p.setIndividualAuraRoundsRemaining(4);
                p.setBlockAuraEffect(blockAura);
                p.setBlockAuraRoundsRemaining(4);
            }
        }
    }

}
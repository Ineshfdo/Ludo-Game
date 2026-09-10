package game.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Cell;
import game.LudoBoard;
import game.effects.AlphaEffect;
import players.components.LudoPiece;
import players.components.Player;
import players.components.PlayerColor;

public class MovementManager {
    public static boolean isPathBlockedByOpponent(Cell cell, PlayerColor movingColor, int movingSize) {
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

    private static String getBlockingPiecesString(Cell cell, PlayerColor movingColor) {
        StringBuilder sb = new StringBuilder();
        Map<PlayerColor, List<String>> blockers = new HashMap<>();
        for (LudoPiece p : cell.getPieces()) {
            if (p.getColor() != movingColor && p.getState().equals("STANDARD")) {
                blockers.putIfAbsent(p.getColor(), new ArrayList<>());
                blockers.get(p.getColor()).add(p.getId());
            }
        }
        boolean first = true;
        for (Map.Entry<PlayerColor, List<String>> entry : blockers.entrySet()) {
            if (!first) sb.append(" / ");
            String cName = entry.getKey().toString().substring(0, 1).toUpperCase() + entry.getKey().toString().substring(1).toLowerCase();
            sb.append(cName).append(" pieces [").append(String.join(", ", entry.getValue())).append("]");
            first = false;
        }
        return sb.toString();
    }

    public static boolean moveFromBaseToStart(LudoPiece piece) {
        if (piece.getState().equals("BASE")) {
            int startPos = PathUtils.getStartIndex(piece.getColor());
            if (isPathBlockedByOpponent(LudoBoard.getInstance().getStandardPath()[startPos], piece.getColor(), 1)) {
                System.out.println("  -> Cannot move out of BASE! Start cell " + startPos
                        + " is BLOCKED by an opponentPiece block.");
                return false;
            }
            piece.setState("STANDARD");
            piece.setPosition(startPos);
            piece.setApproachPasses(0);
            piece.setCaptures(0);

            boolean heads = game.utils.CoinFlip.getInstance().flip();
            piece.setXChoiceDirectionClockwise(heads);
            String cName = piece.getColor().toString().substring(0, 1).toUpperCase() + piece.getColor().toString().substring(1).toLowerCase();
            System.out.println(cName + " player moves piece " + piece.getId() + " to the starting point");
            game.GameFacade.printPlayerStatus(piece.getColor());

            return CaptureManager.handleStandardCellLanding(piece, startPos);
        }
        return false;
    }

    private static boolean moveOppositeBlock(List<LudoPiece> block, int roll) {
        LudoPiece dominant = block.get(0);
        int baseStepsToMove = roll / block.size();
        int stepsToMove = AlphaEffect.calculateBlockEffectiveRoll(dominant, baseStepsToMove);

        System.out.println("  -> Opposite Block tried to move with roll " + roll + ". Base division results in "
                + baseStepsToMove + ". Effective steps: " + stepsToMove);
        if (stepsToMove == 0) {
            return false;
        }

        int maxDistance = -1;
        LudoPiece dominantPiece = null;
        for (LudoPiece currentPiece : block) {
            int distanceToHome = PathUtils.getDistanceToHome(currentPiece);
            if (distanceToHome > maxDistance) {
                maxDistance = distanceToHome;
                dominantPiece = currentPiece;
            }
        }

        System.out.println("  -> Opposite Block detected! Moving together. Roll: " + roll + " / " + block.size()
                + " pieces = " + stepsToMove + " steps.");
        System.out.println("  -> Dominant direction determined by piece " + dominantPiece.getId()
                + " (Distance to home: " + maxDistance + ").");

        int combinedBlockDirection = dominantPiece.isXChoiceDirectionClockwise() ? 1 : -1;
        for (LudoPiece currentPiece : block) {
            currentPiece.setCombinedBlockDirectionClockwise(combinedBlockDirection == 1);
        }

        int temporaryPosition = block.get(0).getPosition();
        for (int stepIndex = 1; stepIndex <= stepsToMove; stepIndex++) {
            temporaryPosition = (temporaryPosition + combinedBlockDirection + LudoBoard.STANDARD_PATH_LENGTH)
                    % LudoBoard.STANDARD_PATH_LENGTH;
            if (isPathBlockedByOpponent(LudoBoard.getInstance().getStandardPath()[temporaryPosition],
                    block.get(0).getColor(), block.size())) {
                System.out.println("  -> Block move completely blocked by an opponentPiece block at cell "
                        + temporaryPosition + "!");
                return false;
            }
        }

        int finalPos = temporaryPosition;
        Cell startingCell = LudoBoard.getInstance().getStandardPath()[block.get(0).getPosition()];
        boolean captured = false;

        Cell destinationCell = LudoBoard.getInstance().getStandardPath()[finalPos];
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
                    if (piecePosition == PathUtils.getApproachIndex(currentPiece.getColor())
                            && currentPiece.getCaptures() >= 1) {
                        if (currentPiece.isXChoiceDirectionClockwise() && piecePasses >= 1)
                            readyForHome = true;
                        if (!currentPiece.isXChoiceDirectionClockwise() && piecePasses >= 2)
                            readyForHome = true;
                    }

                    if (readyForHome) {
                        pieceState = "HOME_STRAIGHT";
                        piecePosition = 0;
                    } else {
                        piecePosition = (piecePosition + combinedBlockDirection + LudoBoard.STANDARD_PATH_LENGTH)
                                % LudoBoard.STANDARD_PATH_LENGTH;
                        if (piecePosition == PathUtils.getApproachIndex(currentPiece.getColor())) {
                            if ((currentPiece.isXChoiceDirectionClockwise() && combinedBlockDirection == 1)
                                    || (!currentPiece.isXChoiceDirectionClockwise() && combinedBlockDirection == -1)) {
                                piecePasses++;
                            } else {
                                piecePasses = Math.max(0, piecePasses - 1);
                            }
                        }
                    }
                } else if (pieceState.equals("HOME_STRAIGHT")) {
                    int nextPos = piecePosition + 1;
                    if (nextPos <= LudoBoard.HOME_STRAIGHT_LENGTH) {
                        piecePosition = nextPos;
                        if (piecePosition == LudoBoard.HOME_STRAIGHT_LENGTH) {
                            pieceState = "HOME";
                        }
                    }
                }
            }

            currentPiece.setPosition(piecePosition);
            currentPiece.setState(pieceState);
            currentPiece.setApproachPasses(piecePasses);

            if (pieceState.equals("STANDARD")) {
                LudoBoard.getInstance().getStandardPath()[finalPos].addPiece(currentPiece);
            } else {
                System.out.println("  -> Piece " + currentPiece.getId() + " broke off from the block and entered "
                        + pieceState + " at position " + piecePosition);
            }
        }

        if (finalPos == MysteryCellManager.getMysteryCellPosition()
                && MysteryCellManager.getMysteryCellPosition() != -1) {
            MysteryCellManager.triggerMysteryCellBlockTeleport(block);
        }

        return captured;
    }

    private static boolean moveSameWayBlock(List<LudoPiece> block, int roll) {
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
            temporaryPosition = (temporaryPosition + direction + LudoBoard.STANDARD_PATH_LENGTH)
                    % LudoBoard.STANDARD_PATH_LENGTH;
            if (isPathBlockedByOpponent(LudoBoard.getInstance().getStandardPath()[temporaryPosition],
                    dominant.getColor(), block.size())) {
                System.out.println("  -> Block move completely blocked by an opponentPiece block at cell "
                        + temporaryPosition + "!");
                return false;
            }
        }

        int finalPos = temporaryPosition;
        Cell startingCell = LudoBoard.getInstance().getStandardPath()[dominant.getPosition()];
        boolean captured = false;

        Cell destinationCell = LudoBoard.getInstance().getStandardPath()[finalPos];
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
                    if (piecePosition == PathUtils.getApproachIndex(currentPiece.getColor())
                            && currentPiece.getCaptures() >= 1) {
                        if (currentPiece.isXChoiceDirectionClockwise() && piecePasses >= 1)
                            readyForHome = true;
                        if (!currentPiece.isXChoiceDirectionClockwise() && piecePasses >= 2)
                            readyForHome = true;
                    }

                    if (readyForHome) {
                        pieceState = "HOME_STRAIGHT";
                        piecePosition = 0;
                    } else {
                        piecePosition = (piecePosition + direction + LudoBoard.STANDARD_PATH_LENGTH)
                                % LudoBoard.STANDARD_PATH_LENGTH;
                        if (piecePosition == PathUtils.getApproachIndex(currentPiece.getColor())) {
                            piecePasses++;
                        }
                    }
                } else if (pieceState.equals("HOME_STRAIGHT")) {
                    int nextPos = piecePosition + 1;
                    if (nextPos <= LudoBoard.HOME_STRAIGHT_LENGTH) {
                        piecePosition = nextPos;
                        if (piecePosition == LudoBoard.HOME_STRAIGHT_LENGTH) {
                            pieceState = "HOME";
                        }
                    }
                }
            }

            currentPiece.setPosition(piecePosition);
            currentPiece.setState(pieceState);
            currentPiece.setApproachPasses(piecePasses);

            if (pieceState.equals("STANDARD")) {
                LudoBoard.getInstance().getStandardPath()[finalPos].addPiece(currentPiece);
            } else {
                System.out.println("  -> Piece " + currentPiece.getId() + " broke off from the block and entered "
                        + pieceState + " at position " + piecePosition);
            }
        }

        if (finalPos == MysteryCellManager.getMysteryCellPosition()
                && MysteryCellManager.getMysteryCellPosition() != -1) {
            MysteryCellManager.triggerMysteryCellBlockTeleport(block);
        }

        return captured;
    }

    public static boolean movePiece(LudoPiece piece, int roll, boolean tryMoveAsBlock) {
        if (piece.getState().equals("BASE") && roll == 6) {
            return moveFromBaseToStart(piece);
        } else if (!piece.getState().equals("BASE")) {
            if (piece.getState().equals("STANDARD") && tryMoveAsBlock) {
                Cell currentCell = LudoBoard.getInstance().getStandardPath()[piece.getPosition()];
                List<LudoPiece> piecesOnCell = currentCell.getPieces();

                boolean hasClockwisePiece = false;
                boolean hasCounterClockwisePiece = false;
                List<LudoPiece> blockPieces = new ArrayList<>();

                for (LudoPiece currentPiece : piecesOnCell) {
                    if (currentPiece.getColor() == piece.getColor() && currentPiece.getState().equals("STANDARD")) {
                        blockPieces.add(currentPiece);
                        if (currentPiece.isXChoiceDirectionClockwise())
                            hasClockwisePiece = true;
                        else
                            hasCounterClockwisePiece = true;
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

            int oldPos = piece.getPosition();
            String oldState = piece.getState();
            int temporaryPosition = piece.getPosition();
            String temporaryState = piece.getState();
            int temporaryPasses = piece.getApproachPasses();
            int approachIndex = PathUtils.getApproachIndex(piece.getColor());
            int direction = piece.isXChoiceDirectionClockwise() ? 1 : -1;

            int actualSteps = 0;
            int effectiveRoll = AlphaEffect.calculateEffectiveRoll(piece, roll);

            for (int stepIndex = 1; stepIndex <= effectiveRoll; stepIndex++) {
                if (temporaryState.equals("STANDARD")) {
                    boolean readyForHome = false;
                    if (temporaryPosition == approachIndex && piece.getCaptures() >= 1) {
                        if (piece.isXChoiceDirectionClockwise() && temporaryPasses >= 1)
                            readyForHome = true;
                        if (!piece.isXChoiceDirectionClockwise() && temporaryPasses >= 2)
                            readyForHome = true;
                    }

                    if (readyForHome) {
                        temporaryState = "HOME_STRAIGHT";
                        temporaryPosition = 0;
                        actualSteps++;
                    } else {
                        int nextIndex = (temporaryPosition + direction + LudoBoard.STANDARD_PATH_LENGTH)
                                % LudoBoard.STANDARD_PATH_LENGTH;
                        if (isPathBlockedByOpponent(LudoBoard.getInstance().getStandardPath()[nextIndex],
                                piece.getColor(), 1)) {
                            break;
                        }
                        temporaryPosition = nextIndex;
                        if (temporaryPosition == approachIndex) {
                            temporaryPasses++;
                        }
                        actualSteps++;
                    }
                } else if (temporaryState.equals("HOME_STRAIGHT")) {
                    int nextPos = temporaryPosition + 1;
                    if (nextPos <= LudoBoard.HOME_STRAIGHT_LENGTH) {
                        temporaryPosition = nextPos;
                        actualSteps++;
                    } else {
                        return false;
                    }
                }
            }

            String cName = piece.getColor().toString().substring(0, 1).toUpperCase() + piece.getColor().toString().substring(1).toLowerCase();
            String dirStr = piece.isXChoiceDirectionClockwise() ? "clockwise" : "counter-clockwise";
            String L1 = oldState.equals("STANDARD") ? "L" + oldPos : (oldState.equals("HOME_STRAIGHT") ? "HomePath(" + oldPos + ")" : oldState);
            int intendedL2Index = (oldPos + direction * effectiveRoll + LudoBoard.STANDARD_PATH_LENGTH) % LudoBoard.STANDARD_PATH_LENGTH;
            String intendedL2 = "L" + intendedL2Index;

            if (actualSteps == 0) {
                Cell blockingCell = LudoBoard.getInstance().getStandardPath()[(oldPos + direction + LudoBoard.STANDARD_PATH_LENGTH) % LudoBoard.STANDARD_PATH_LENGTH];
                String blockStr = getBlockingPiecesString(blockingCell, piece.getColor());
                System.out.println(cName + " piece " + piece.getId() + " is blocked from moving from " + L1 + " to " + intendedL2 + " by " + blockStr + ".");
                System.out.println(cName + " does not have other pieces in the board to move instead of the blocked piece.");
                System.out.println("Ignoring the throw and moving on to the next player.");
                return false;
            }

            if (actualSteps < effectiveRoll) {
                Cell blockingCell = LudoBoard.getInstance().getStandardPath()[(temporaryPosition + direction + LudoBoard.STANDARD_PATH_LENGTH) % LudoBoard.STANDARD_PATH_LENGTH];
                String blockStr = getBlockingPiecesString(blockingCell, piece.getColor());
                String L3 = temporaryState.equals("STANDARD") ? "L" + temporaryPosition : (temporaryState.equals("HOME_STRAIGHT") ? "HomePath(" + temporaryPosition + ")" : temporaryState);
                System.out.println(cName + " piece " + piece.getId() + " is blocked from moving from " + L1 + " to " + intendedL2 + " by " + blockStr + ".");
                System.out.println("Moved the piece to square " + L3 + " which is the cell before the block");
            } else {
                String L2 = temporaryState.equals("STANDARD") ? "L" + temporaryPosition : (temporaryState.equals("HOME_STRAIGHT") ? "HomePath(" + temporaryPosition + ")" : temporaryState);
                System.out.println(cName + " moves piece " + piece.getId() + " from location " + L1 + " to " + L2 + " by " + roll + " units in " + dirStr + " direction");
            }

            LudoBoard.getInstance().removeFromCurrentCell(piece);
            piece.setState(temporaryState);
            piece.setPosition(temporaryPosition);
            piece.setApproachPasses(temporaryPasses);

            if (temporaryState.equals("HOME_STRAIGHT")) {
                if (temporaryPosition == LudoBoard.HOME_STRAIGHT_LENGTH) {
                    reachHome(piece);
                } else {
                    LudoBoard.getInstance().getHomeStraight(piece.getColor())[temporaryPosition].addPiece(piece);
                }
                return false;
            } else {
                return CaptureManager.handleStandardCellLanding(piece, temporaryPosition);
            }
        }
        return false;
    }

    public static void moveToHomeStraight(LudoPiece piece) {
        if (piece.getState().equals("STANDARD")
                && piece.getPosition() == PathUtils.getApproachIndex(piece.getColor())) {
            piece.setState("HOME_STRAIGHT");
            piece.setPosition(0);
        }
    }

    public static void reachHome(LudoPiece piece) {
        if (piece.getState().equals("HOME_STRAIGHT") && piece.getPosition() == LudoBoard.HOME_STRAIGHT_LENGTH) {
            piece.setState("HOME");
            piece.setPosition(LudoPiece.POSITION_REMOVED);
        }
    }

    public static boolean tryBreakBlockadeForConsecutiveSixes(Player player) {
        for (int cellIndex = 0; cellIndex < LudoBoard.STANDARD_PATH_LENGTH; cellIndex++) {
            Cell cell = LudoBoard.getInstance().getStandardPath()[cellIndex];
            List<LudoPiece> playerPieces = new ArrayList<>();
            for (LudoPiece currentPiece : cell.getPieces()) {
                if (currentPiece.getColor() == player.getColor() && currentPiece.getState().equals("STANDARD")) {
                    playerPieces.add(currentPiece);
                }
            }

            if (playerPieces.size() >= 2) {
                System.out.println("  -> Breaking blockade at cell " + cellIndex);
                for (int pieceIndex = 1; pieceIndex < playerPieces.size(); pieceIndex++) {
                    LudoPiece movingPiece = playerPieces.get(pieceIndex);
                    forceMovePiece(movingPiece, 6);
                }
                return true;
            }
        }
        return false;
    }

    private static void forceMovePiece(LudoPiece piece, int roll) {
        int temporaryPosition = piece.getPosition();
        String temporaryState = piece.getState();
        int temporaryPasses = piece.getApproachPasses();
        int direction = piece.isXChoiceDirectionClockwise() ? 1 : -1;
        int approachIndex = PathUtils.getApproachIndex(piece.getColor());

        System.out.println("  -> Forcing piece " + piece.getId() + " to move " + roll + " cells "
                + (direction == 1 ? "Clockwise" : "Counter-Clockwise") + "...");

        for (int stepIndex = 1; stepIndex <= roll; stepIndex++) {
            if (temporaryState.equals("STANDARD")) {
                boolean readyForHome = false;
                if (temporaryPosition == approachIndex && piece.getCaptures() >= 1) {
                    if (piece.isXChoiceDirectionClockwise() && temporaryPasses >= 1)
                        readyForHome = true;
                    if (!piece.isXChoiceDirectionClockwise() && temporaryPasses >= 2)
                        readyForHome = true;
                }

                if (readyForHome) {
                    temporaryState = "HOME_STRAIGHT";
                    temporaryPosition = 0;
                } else {
                    temporaryPosition = (temporaryPosition + direction + LudoBoard.STANDARD_PATH_LENGTH)
                            % LudoBoard.STANDARD_PATH_LENGTH;
                    if (temporaryPosition == approachIndex) {
                        temporaryPasses++;
                    }
                }
            } else if (temporaryState.equals("HOME_STRAIGHT")) {
                int nextPos = temporaryPosition + 1;
                if (nextPos <= LudoBoard.HOME_STRAIGHT_LENGTH) {
                    temporaryPosition = nextPos;
                    if (temporaryPosition == LudoBoard.HOME_STRAIGHT_LENGTH) {
                        temporaryState = "HOME";
                    }
                }
            }
        }

        if (piece.getState().equals("STANDARD")) {
            LudoBoard.getInstance().getStandardPath()[piece.getPosition()].removePiece(piece);
        }

        piece.setPosition(temporaryPosition);
        piece.setState(temporaryState);
        piece.setApproachPasses(temporaryPasses);

        if (temporaryState.equals("STANDARD")) {
            CaptureManager.handleStandardCellLanding(piece, temporaryPosition);
        } else {
            System.out.println("  -> Piece " + piece.getId() + " forcibly moved into " + temporaryState
                    + " at position " + temporaryPosition);
        }
    }
}

package game.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Cell;
import game.LudoBoard;
import game.players.components.LudoPiece;
import game.players.components.Player;
import game.players.components.PlayerColor;
import game.rules.Rule1BaseRule;
import game.rules.Rule2BlockMoveRule;
import game.rules.Rule3StandardPathRule;
import game.rules.Rule4HomeStraightRule;
import game.rules.MovementRule;

public class MovementManager {
    private static final MovementRule movementChain;

    static {
        MovementRule rule1 = new Rule1BaseRule();
        MovementRule rule2 = new Rule2BlockMoveRule();
        MovementRule rule3 = new Rule3StandardPathRule();
        MovementRule rule4 = new Rule4HomeStraightRule();

        rule1.setNextRule(rule2)
                .setNextRule(rule3)
                .setNextRule(rule4);

        movementChain = rule1;
    }

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

    public static String getBlockingPiecesString(Cell cell, PlayerColor movingColor) {
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
            if (!first)
                sb.append(" / ");
            String cName = entry.getKey().toString().substring(0, 1).toUpperCase()
                    + entry.getKey().toString().substring(1).toLowerCase();
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
            String cName = piece.getColor().toString().substring(0, 1).toUpperCase()
                    + piece.getColor().toString().substring(1).toLowerCase();
            System.out.println(cName + " player moves piece " + piece.getId() + " to the starting point");
            game.GameFacade.printPlayerStatus(piece.getColor());

            return CaptureManager.handleStandardCellLanding(piece, startPos);
        }
        return false;
    }

    public static boolean moveOppositeBlock(List<LudoPiece> block, int roll) {
        LudoPiece dominant = block.get(0);
        int baseStepsToMove = roll / block.size();
        int stepsToMove = dominant.getMovementStrategy().calculateBlockEffectiveRoll(baseStepsToMove);

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

    public static boolean moveSameWayBlock(List<LudoPiece> block, int roll) {
        LudoPiece dominant = block.get(0);
        int stepsToMove = dominant.getMovementStrategy().calculateBlockEffectiveRoll(roll);

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
        return movementChain.handleMove(piece, roll, tryMoveAsBlock);
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

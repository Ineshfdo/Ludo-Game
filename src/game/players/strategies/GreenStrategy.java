package game.players.strategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import game.Cell;
import game.LudoBoard;
import game.players.components.LudoPiece;
import game.utils.MovementManager;
import game.utils.PathUtils;

public class GreenStrategy implements Strategy {

    private class MoveOption implements Comparable<MoveOption> {
        LudoPiece piece;
        boolean tryMoveAsBlock;
        int priority;

        MoveOption(LudoPiece piece, boolean tryMoveAsBlock, int priority) {
            this.piece = piece;
            this.tryMoveAsBlock = tryMoveAsBlock;
            this.priority = priority;
        }

        @Override
        public int compareTo(MoveOption o) {
            return Integer.compare(o.priority, this.priority); // Descending priority
        }
    }

    @Override
    public boolean processMovement(LudoPiece[] pieces, int roll, LudoBoard board) {
        List<MoveOption> options = evaluateMoves(pieces, roll, board);
        Collections.sort(options);

        for (MoveOption opt : options) {
            int oldPos = opt.piece.getPosition();
            String oldState = opt.piece.getState().getStateName();

            boolean captured = MovementManager.movePiece(opt.piece, roll, opt.tryMoveAsBlock);

            if (!opt.piece.getState().getStateName().equals(oldState) || opt.piece.getPosition() != oldPos) {
                return captured; // Move was successful
            }
        }
        return false;
    }

    private List<MoveOption> evaluateMoves(LudoPiece[] pieces, int roll, LudoBoard board) {
        List<MoveOption> options = new ArrayList<>();

        for (LudoPiece currentPiece : pieces) {
            if (!currentPiece.getState().canMove())
                continue;

            if (currentPiece.getState().isBase()) {
                if (roll == 6) {
                    int startPos = PathUtils.getStartIndex(currentPiece.getColor());
                    boolean createsBlock = false;
                    for (LudoPiece other : board.getStandardPath()[startPos].getPieces()) {
                        if (other.getColor() == currentPiece.getColor() && other.getState().isStandard()) {
                            createsBlock = true;
                            break;
                        }
                    }
                    int priority = createsBlock ? 10 : 8;
                    options.add(new MoveOption(currentPiece, true, priority));
                }
            } else if (currentPiece.getState().isStandard() || currentPiece.getState().isHomeStraight()) {
                boolean inBlock = isInBlock(currentPiece, board);

                if (inBlock) {
                    // Try to move as block (Priority 6)
                    options.add(new MoveOption(currentPiece, true, 6));

                    // Try to break block (Priority 2 - Lowest)
                    options.add(new MoveOption(currentPiece, false, 2));
                } else {
                    // Normal move (Default Priority 4)
                    int priority = 4;
                    if (currentPiece.getState().isStandard()) {
                        Cell dest = simulateSinglePieceDestination(currentPiece, roll, board);
                        if (dest != null) {
                            boolean createsBlock = false;
                            for (LudoPiece other : dest.getPieces()) {
                                if (other.getColor() == currentPiece.getColor()
                                        && other.getState().isStandard()) {
                                    createsBlock = true;
                                    break;
                                }
                            }

                            if (createsBlock) {
                                priority = 10;
                            } else if (currentPiece.getCaptures() == 0) {
                                // If it needs a capture to enter home straight, and this move captures
                                boolean canCapture = false;
                                for (LudoPiece other : dest.getPieces()) {
                                    if (other.getColor() != currentPiece.getColor()
                                            && other.getState().isStandard()) {
                                        canCapture = true;
                                        break;
                                    }
                                }
                                if (canCapture) {
                                    priority = 5; // Slightly higher than normal move
                                }
                            }
                        }
                    }
                    options.add(new MoveOption(currentPiece, true, priority));
                }
            }
        }
        return options;
    }

    private boolean isInBlock(LudoPiece piece, LudoBoard board) {
        if (!piece.getState().isStandard())
            return false;
        Cell currentCell = board.getStandardPath()[piece.getPosition()];
        int greenCount = 0;
        for (LudoPiece cellPiece : currentCell.getPieces()) {
            if (cellPiece.getColor() == piece.getColor() && cellPiece.getState().isStandard()) {
                greenCount++;
            }
        }
        return greenCount >= 2;
    }

    private Cell simulateSinglePieceDestination(LudoPiece movingPiece, int roll, LudoBoard board) {
        int effectiveRoll = movingPiece.getState().calculateEffectiveRoll(roll);
        if (effectiveRoll == 0)
            return null;

        int tempPos = movingPiece.getPosition();
        String tempState = movingPiece.getState().getStateName();
        int tempPasses = movingPiece.getApproachPasses();
        int approachIndex = PathUtils.getApproachIndex(movingPiece.getColor());
        int direction = movingPiece.isXChoiceDirectionClockwise() ? 1 : -1;

        for (int stepIndex = 1; stepIndex <= effectiveRoll; stepIndex++) {
            if (tempState.equals("STANDARD")) {
                boolean readyForHome = false;
                if (tempPos == approachIndex && movingPiece.getCaptures() >= 1) {
                    if (movingPiece.isXChoiceDirectionClockwise() && tempPasses >= 1)
                        readyForHome = true;
                    if (!movingPiece.isXChoiceDirectionClockwise() && tempPasses >= 2)
                        readyForHome = true;
                }

                if (readyForHome) {
                    tempState = "HOME_STRAIGHT";
                    tempPos = 0;
                } else {
                    int nextIndex = (tempPos + direction + LudoBoard.STANDARD_PATH_LENGTH)
                            % LudoBoard.STANDARD_PATH_LENGTH;
                    if (MovementManager.isPathBlockedByOpponent(board.getStandardPath()[nextIndex],
                            movingPiece.getColor(), 1)) {
                        return null; // Blocked
                    }
                    tempPos = nextIndex;
                    if (tempPos == approachIndex) {
                        tempPasses++;
                    }
                }
            } else if (tempState.equals("HOME_STRAIGHT")) {
                int nextPos = tempPos + 1;
                if (nextPos <= LudoBoard.HOME_STRAIGHT_LENGTH) {
                    tempPos = nextPos;
                    if (tempPos == LudoBoard.HOME_STRAIGHT_LENGTH) {
                        tempState = "HOME";
                    }
                } else {
                    return null; // Overshoots home
                }
            }
        }

        if (tempState.equals("STANDARD")) {
            return board.getStandardPath()[tempPos];
        }
        return null; // Ends in home straight or home, doesn't create block
    }
}

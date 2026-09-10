package players;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import game.Cell;
import game.LudoBoard;
import game.effects.AlphaEffect;
import game.effects.BetaEffect;
import game.utils.MovementManager;
import game.utils.PathUtils;
import players.components.LudoPiece;
import players.components.Player;
import players.components.PlayerColor;

public class YellowPlayer extends Player {

    public YellowPlayer() {
        super(PlayerColor.YELLOW);
    }

    private class MoveOption implements Comparable<MoveOption> {
        LudoPiece piece;
        boolean tryMoveAsBlock;
        int priority;
        int distanceToHome;

        MoveOption(LudoPiece piece, boolean tryMoveAsBlock, int priority, int distanceToHome) {
            this.piece = piece;
            this.tryMoveAsBlock = tryMoveAsBlock;
            this.priority = priority;
            this.distanceToHome = distanceToHome;
        }

        @Override
        public int compareTo(MoveOption o) {
            if (this.priority != o.priority) {
                return Integer.compare(o.priority, this.priority); // Descending priority
            }
            // Tie-breaker: piece closest to home (smallest distance)
            return Integer.compare(this.distanceToHome, o.distanceToHome); // Ascending distance
        }
    }

    @Override
    protected boolean processMovement(int roll, LudoBoard board) {
        List<MoveOption> options = evaluateMoves(roll, board);
        Collections.sort(options);

        for (MoveOption opt : options) {
            int oldPos = opt.piece.getPosition();
            String oldState = opt.piece.getState();

            boolean captured = MovementManager.movePiece(opt.piece, roll, opt.tryMoveAsBlock);

            if (!opt.piece.getState().equals(oldState) || opt.piece.getPosition() != oldPos) {
                return captured; // Move was successful
            }
        }
        return false;
    }

    private List<MoveOption> evaluateMoves(int roll, LudoBoard board) {
        List<MoveOption> options = new ArrayList<>();

        for (LudoPiece currentPiece : pieces) {
            if (!BetaEffect.canMove(currentPiece)) continue;

            if (currentPiece.getState().equals("BASE")) {
                if (roll == 6) {
                    int startPos = PathUtils.getStartIndex(currentPiece.getColor());
                    if (!MovementManager.isPathBlockedByOpponent(board.getStandardPath()[startPos], currentPiece.getColor(), 1)) {
                        options.add(new MoveOption(currentPiece, true, 100, PathUtils.getDistanceToHome(currentPiece)));
                    }
                }
            } else if (currentPiece.getState().equals("STANDARD") || currentPiece.getState().equals("HOME_STRAIGHT")) {
                boolean inBlock = isInBlock(currentPiece, board);
                int dist = PathUtils.getDistanceToHome(currentPiece);

                Cell dest = simulateSinglePieceDestination(currentPiece, roll, board);
                boolean isRequiredCapture = false;

                if (dest != null && currentPiece.getCaptures() == 0) {
                    for (LudoPiece other : dest.getPieces()) {
                        if (other.getColor() != currentPiece.getColor() && other.getState().equals("STANDARD")) {
                            isRequiredCapture = true;
                            break;
                        }
                    }
                }

                int basePriority = isRequiredCapture ? 50 : 0;

                if (inBlock) {
                    // Try to move as block (Slightly better than breaking block)
                    options.add(new MoveOption(currentPiece, true, basePriority, dist));
                    // Try to break block (Fallback)
                    options.add(new MoveOption(currentPiece, false, basePriority - 1, dist));
                } else {
                    options.add(new MoveOption(currentPiece, true, basePriority, dist));
                }
            }
        }
        return options;
    }

    private boolean isInBlock(LudoPiece piece, LudoBoard board) {
        if (!piece.getState().equals("STANDARD")) return false;
        Cell currentCell = board.getStandardPath()[piece.getPosition()];
        int count = 0;
        for (LudoPiece cellPiece : currentCell.getPieces()) {
            if (cellPiece.getColor() == piece.getColor() && cellPiece.getState().equals("STANDARD")) {
                count++;
            }
        }
        return count >= 2;
    }

    private Cell simulateSinglePieceDestination(LudoPiece movingPiece, int roll, LudoBoard board) {
        int effectiveRoll = AlphaEffect.calculateEffectiveRoll(movingPiece, roll);
        if (effectiveRoll == 0) return null;

        int tempPos = movingPiece.getPosition();
        String tempState = movingPiece.getState();
        int tempPasses = movingPiece.getApproachPasses();
        int approachIndex = PathUtils.getApproachIndex(movingPiece.getColor());
        int direction = movingPiece.isXChoiceDirectionClockwise() ? 1 : -1;

        for (int stepIndex = 1; stepIndex <= effectiveRoll; stepIndex++) {
            if (tempState.equals("STANDARD")) {
                boolean readyForHome = false;
                if (tempPos == approachIndex && movingPiece.getCaptures() >= 1) {
                    if (movingPiece.isXChoiceDirectionClockwise() && tempPasses >= 1) readyForHome = true;
                    if (!movingPiece.isXChoiceDirectionClockwise() && tempPasses >= 2) readyForHome = true;
                }

                if (readyForHome) {
                    tempState = "HOME_STRAIGHT";
                    tempPos = 0;
                } else {
                    int nextIndex = (tempPos + direction + LudoBoard.STANDARD_PATH_LENGTH) % LudoBoard.STANDARD_PATH_LENGTH;
                    if (MovementManager.isPathBlockedByOpponent(board.getStandardPath()[nextIndex], movingPiece.getColor(), 1)) {
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
        return null; // Ends in home straight or home, doesn't capture
    }
}

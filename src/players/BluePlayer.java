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

public class BluePlayer extends Player {

    private int lastMovedPieceIndex = -1;

    public BluePlayer() {
        super(PlayerColor.BLUE);
    }

    private class MoveOption implements Comparable<MoveOption> {
        LudoPiece piece;
        int pieceIndex;
        boolean tryMoveAsBlock;
        int priority;
        int cyclicDistance;

        MoveOption(LudoPiece piece, int pieceIndex, boolean tryMoveAsBlock, int priority, int cyclicDistance) {
            this.piece = piece;
            this.pieceIndex = pieceIndex;
            this.tryMoveAsBlock = tryMoveAsBlock;
            this.priority = priority;
            this.cyclicDistance = cyclicDistance;
        }

        @Override
        public int compareTo(MoveOption o) {
            if (this.priority != o.priority) {
                return Integer.compare(o.priority, this.priority); // Descending priority
            }
            // Tie-breaker: cyclic distance (smallest distance wins)
            return Integer.compare(this.cyclicDistance, o.cyclicDistance); // Ascending distance
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
                lastMovedPieceIndex = opt.pieceIndex;
                return captured; // Move was successful
            }
        }
        return false;
    }

    private List<MoveOption> evaluateMoves(int roll, LudoBoard board) {
        List<MoveOption> options = new ArrayList<>();

        for (int i = 0; i < pieces.length; i++) {
            LudoPiece currentPiece = pieces[i];
            if (!BetaEffect.canMove(currentPiece)) continue;

            int cyclicDist = i - lastMovedPieceIndex;
            if (cyclicDist <= 0) cyclicDist += 4;

            if (currentPiece.getState().equals("BASE")) {
                if (roll == 6) {
                    int startPos = PathUtils.getStartIndex(currentPiece.getColor());
                    if (!MovementManager.isPathBlockedByOpponent(board.getStandardPath()[startPos], currentPiece.getColor(), 1)) {
                        options.add(new MoveOption(currentPiece, i, true, 1, cyclicDist));
                    }
                }
            } else if (currentPiece.getState().equals("STANDARD") || currentPiece.getState().equals("HOME_STRAIGHT")) {
                boolean inBlock = isInBlock(currentPiece, board);
                
                int destPos = simulateSinglePieceDestination(currentPiece, roll, board);
                int basePriority = 0;
                
                if (destPos != -1 && destPos == game.utils.MysteryCellManager.getMysteryCellPosition()) {
                    if (currentPiece.isXChoiceDirectionClockwise()) {
                        basePriority = -10; // Avoid landing on mystery cell
                    } else {
                        basePriority = 10; // Prioritize landing on mystery cell
                    }
                }

                if (inBlock) {
                    options.add(new MoveOption(currentPiece, i, true, basePriority * 10 + 1, cyclicDist));
                    options.add(new MoveOption(currentPiece, i, false, basePriority * 10, cyclicDist));
                } else {
                    options.add(new MoveOption(currentPiece, i, true, basePriority * 10, cyclicDist));
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

    private int simulateSinglePieceDestination(LudoPiece movingPiece, int roll, LudoBoard board) {
        int effectiveRoll = AlphaEffect.calculateEffectiveRoll(movingPiece, roll);
        if (effectiveRoll == 0) return -1;

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
                        return -1; // Blocked
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
                    return -1; // Overshoots home
                }
            }
        }
        
        if (tempState.equals("STANDARD")) {
            return tempPos;
        }
        return -1; // Ends in home straight or home
    }
}
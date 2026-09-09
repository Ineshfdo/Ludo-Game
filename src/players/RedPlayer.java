package players;

import java.util.ArrayList;
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

public class RedPlayer extends Player {

    public RedPlayer() {
        super(PlayerColor.RED);
    }

    private class MoveOption {
        LudoPiece piece;
        boolean isFromBase;
        boolean isCapture;
        int minOpponentDistanceToHome;
        boolean createsBlock;
        boolean leavesStandardPathEmpty;
        boolean isValid;

        MoveOption(LudoPiece piece, boolean isFromBase) {
            this.piece = piece;
            this.isFromBase = isFromBase;
            this.isCapture = false;
            this.minOpponentDistanceToHome = Integer.MAX_VALUE;
            this.createsBlock = false;
            this.leavesStandardPathEmpty = false;
            this.isValid = true;
        }
    }

    @Override
    protected boolean processMovement(int roll, LudoBoard board) {
        List<MoveOption> options = evaluateMoves(roll, board);

        if (options.isEmpty()) {
            System.out.println("  -> Red Player: No valid moves available.");
            return false;
        }

        MoveOption bestOption = selectBestMove(options);

        if (bestOption.isFromBase) {
            System.out.println("  -> Red Player taking piece " + bestOption.piece.getId() + " out of BASE.");
            boolean captured = MovementManager.movePiece(bestOption.piece, roll, true);
            if (!bestOption.piece.getState().equals("BASE")) {
                return captured;
            }
            return false;
        } else {
            System.out.println("  -> Red Player moving piece " + bestOption.piece.getId() + " on the board.");
            int oldPos = bestOption.piece.getPosition();
            String oldState = bestOption.piece.getState();
            boolean captured = MovementManager.movePiece(bestOption.piece, roll, true);
            if (!bestOption.piece.getState().equals(oldState) || bestOption.piece.getPosition() != oldPos) {
                return captured;
            }
            return false;
        }
    }

    private List<MoveOption> evaluateMoves(int roll, LudoBoard board) {
        List<MoveOption> options = new ArrayList<>();
        int piecesOnStandard = 0;

        for (LudoPiece currentPiece : pieces) {
            if (currentPiece.getState().equals("STANDARD")) {
                piecesOnStandard++;
            }
        }

        for (LudoPiece currentPiece : pieces) {
            if (!BetaEffect.canMove(currentPiece)) continue;

            if (currentPiece.getState().equals("BASE") && roll == 6) {
                MoveOption opt = new MoveOption(currentPiece, true);
                int startPos = PathUtils.getStartIndex(currentPiece.getColor());
                if (MovementManager.isPathBlockedByOpponent(board.getStandardPath()[startPos], currentPiece.getColor(), 1)) {
                    opt.isValid = false;
                } else {
                    checkDestination(board.getStandardPath()[startPos], currentPiece, opt);
                }
                if (opt.isValid) options.add(opt);

            } else if (currentPiece.getState().equals("STANDARD") || currentPiece.getState().equals("HOME_STRAIGHT")) {
                MoveOption opt = new MoveOption(currentPiece, false);
                simulateBoardMove(currentPiece, roll, board, opt, piecesOnStandard);

                if (opt.isValid) {
                    options.add(opt);
                }
            }
        }
        return options;
    }

    private void simulateBoardMove(LudoPiece movingPiece, int roll, LudoBoard board, MoveOption opt, int piecesOnStandard) {
        int tempPos = movingPiece.getPosition();
        String tempState = movingPiece.getState();
        int tempPasses = movingPiece.getApproachPasses();
        int approachIndex = PathUtils.getApproachIndex(movingPiece.getColor());
        int direction = movingPiece.isXChoiceDirectionClockwise() ? 1 : -1;
        int effectiveRoll = AlphaEffect.calculateEffectiveRoll(movingPiece, roll);

        int actualSteps = 0;

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
                    actualSteps++;
                } else {
                    int nextIndex = (tempPos + direction + LudoBoard.STANDARD_PATH_LENGTH) % LudoBoard.STANDARD_PATH_LENGTH;
                    if (MovementManager.isPathBlockedByOpponent(board.getStandardPath()[nextIndex], movingPiece.getColor(), 1)) {
                        break; // Blocked
                    }
                    tempPos = nextIndex;
                    if (tempPos == approachIndex) {
                        tempPasses++;
                    }
                    actualSteps++;
                }
            } else if (tempState.equals("HOME_STRAIGHT")) {
                int nextPos = tempPos + 1;
                if (nextPos <= LudoBoard.HOME_STRAIGHT_LENGTH) {
                    tempPos = nextPos;
                    if (tempPos == LudoBoard.HOME_STRAIGHT_LENGTH) {
                        tempState = "HOME";
                    }
                    actualSteps++;
                } else {
                    opt.isValid = false;
                    return;
                }
            }
        }

        if (actualSteps == 0) {
            opt.isValid = false;
            return;
        }

        if (movingPiece.getState().equals("STANDARD") && !tempState.equals("STANDARD")) {
            if (piecesOnStandard == 1) {
                opt.leavesStandardPathEmpty = true;
            }
        }

        if (tempState.equals("STANDARD")) {
            checkDestination(board.getStandardPath()[tempPos], movingPiece, opt);
        }
    }

    private void checkDestination(Cell destCell, LudoPiece movingPiece, MoveOption opt) {
        for (LudoPiece other : destCell.getPieces()) {
            if (other.getColor() != movingPiece.getColor() && other.getState().equals("STANDARD")) {
                opt.isCapture = true;
                int dist = PathUtils.getDistanceToHome(other);
                if (dist < opt.minOpponentDistanceToHome) {
                    opt.minOpponentDistanceToHome = dist;
                }
            } else if (other.getColor() == movingPiece.getColor() && other.getState().equals("STANDARD")) {
                opt.createsBlock = true;
            }
        }
    }

    private MoveOption selectBestMove(List<MoveOption> options) {
        MoveOption best = options.get(0);

        for (int i = 1; i < options.size(); i++) {
            MoveOption current = options.get(i);
            
            // 1. Capture priority
            if (current.isCapture != best.isCapture) {
                if (current.isCapture) best = current;
                continue;
            }

            if (current.isCapture && best.isCapture) {
                // Prioritize capturing the one closest to its home (min distance to home)
                if (current.minOpponentDistanceToHome < best.minOpponentDistanceToHome) {
                    best = current;
                }
                continue;
            }

            // 2. Base move priority if no captures
            if (current.isFromBase != best.isFromBase) {
                if (current.isFromBase) {
                    best = current;
                }
                continue;
            }

            // 3. Keep one on path
            if (current.leavesStandardPathEmpty != best.leavesStandardPathEmpty) {
                if (!current.leavesStandardPathEmpty) {
                    best = current;
                }
                continue;
            }

            // 4. Avoid creating blocks
            if (current.createsBlock != best.createsBlock) {
                if (!current.createsBlock) {
                    best = current;
                }
            }
        }

        return best;
    }
}

package game.rules;

import game.Cell;
import game.LudoBoard;
import game.utils.CaptureManager;
import game.utils.MovementManager;
import game.utils.PathUtils;
import players.components.LudoPiece;

public class Rule3StandardPathRule extends MovementRule {

    @Override
    public boolean handleMove(LudoPiece piece, int roll, boolean tryMoveAsBlock) {
        if (piece.getState().equals("STANDARD")) {
            int oldPos = piece.getPosition();
            String oldState = piece.getState();
            int temporaryPosition = piece.getPosition();
            String temporaryState = piece.getState();
            int temporaryPasses = piece.getApproachPasses();
            int approachIndex = PathUtils.getApproachIndex(piece.getColor());
            int direction = piece.isXChoiceDirectionClockwise() ? 1 : -1;

            int actualSteps = 0;
            int effectiveRoll = piece.getMovementStrategy().calculateEffectiveRoll(roll);

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
                        if (MovementManager.isPathBlockedByOpponent(LudoBoard.getInstance().getStandardPath()[nextIndex],
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

            String cName = piece.getColor().toString().substring(0, 1).toUpperCase()
                    + piece.getColor().toString().substring(1).toLowerCase();
            String dirStr = piece.isXChoiceDirectionClockwise() ? "clockwise" : "counter-clockwise";
            String L1 = oldState.equals("STANDARD") ? "L" + oldPos
                    : (oldState.equals("HOME_STRAIGHT") ? "HomePath(" + oldPos + ")" : oldState);
            int intendedL2Index = (oldPos + direction * effectiveRoll + LudoBoard.STANDARD_PATH_LENGTH)
                    % LudoBoard.STANDARD_PATH_LENGTH;
            String intendedL2 = "L" + intendedL2Index;

            if (actualSteps == 0) {
                Cell blockingCell = LudoBoard.getInstance()
                        .getStandardPath()[(oldPos + direction + LudoBoard.STANDARD_PATH_LENGTH)
                                % LudoBoard.STANDARD_PATH_LENGTH];
                String blockStr = MovementManager.getBlockingPiecesString(blockingCell, piece.getColor());
                System.out.println(cName + " piece " + piece.getId() + " is blocked from moving from " + L1 + " to "
                        + intendedL2 + " by " + blockStr + ".");
                System.out.println(
                        cName + " does not have other pieces in the board to move instead of the blocked piece.");
                System.out.println("Ignoring the throw and moving on to the next player.");
                return false;
            }

            if (actualSteps < effectiveRoll) {
                Cell blockingCell = LudoBoard.getInstance()
                        .getStandardPath()[(temporaryPosition + direction + LudoBoard.STANDARD_PATH_LENGTH)
                                % LudoBoard.STANDARD_PATH_LENGTH];
                String blockStr = MovementManager.getBlockingPiecesString(blockingCell, piece.getColor());
                String L3 = temporaryState.equals("STANDARD") ? "L" + temporaryPosition
                        : (temporaryState.equals("HOME_STRAIGHT") ? "HomePath(" + temporaryPosition + ")"
                                : temporaryState);
                System.out.println(cName + " piece " + piece.getId() + " is blocked from moving from " + L1 + " to "
                        + intendedL2 + " by " + blockStr + ".");
                System.out.println("Moved the piece to square " + L3 + " which is the cell before the block");
            } else {
                String L2 = temporaryState.equals("STANDARD") ? "L" + temporaryPosition
                        : (temporaryState.equals("HOME_STRAIGHT") ? "HomePath(" + temporaryPosition + ")"
                                : temporaryState);
                System.out.println(cName + " moves piece " + piece.getId() + " from location " + L1 + " to " + L2
                        + " by " + roll + " units in " + dirStr + " direction");
            }

            LudoBoard.getInstance().removeFromCurrentCell(piece);
            piece.setState(temporaryState);
            piece.setPosition(temporaryPosition);
            piece.setApproachPasses(temporaryPasses);

            if (temporaryState.equals("HOME_STRAIGHT")) {
                if (temporaryPosition == LudoBoard.HOME_STRAIGHT_LENGTH) {
                    MovementManager.reachHome(piece);
                } else {
                    LudoBoard.getInstance().getHomeStraight(piece.getColor())[temporaryPosition].addPiece(piece);
                }
                return false;
            } else {
                return CaptureManager.handleStandardCellLanding(piece, temporaryPosition);
            }
        }

        return checkNext(piece, roll, tryMoveAsBlock);
    }
}

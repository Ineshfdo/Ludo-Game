package game.rules;

import game.LudoBoard;
import game.utils.MovementManager;
import players.components.LudoPiece;

public class Rule4HomeStraightRule extends MovementRule {

    @Override
    public boolean handleMove(LudoPiece piece, int roll, boolean tryMoveAsBlock) {
        if (piece.getState().equals("HOME_STRAIGHT")) {
            int oldPos = piece.getPosition();
            int temporaryPosition = piece.getPosition();

            int actualSteps = 0;
            int effectiveRoll = piece.getMovementStrategy().calculateEffectiveRoll(roll);

            for (int stepIndex = 1; stepIndex <= effectiveRoll; stepIndex++) {
                int nextPos = temporaryPosition + 1;
                if (nextPos <= LudoBoard.HOME_STRAIGHT_LENGTH) {
                    temporaryPosition = nextPos;
                    actualSteps++;
                } else {
                    // Cannot move beyond home
                    return false;
                }
            }

            String cName = piece.getColor().toString().substring(0, 1).toUpperCase()
                    + piece.getColor().toString().substring(1).toLowerCase();
            String dirStr = piece.isXChoiceDirectionClockwise() ? "clockwise" : "counter-clockwise";
            String L1 = "HomePath(" + oldPos + ")";
            String L2 = "HomePath(" + temporaryPosition + ")";

            System.out.println(cName + " moves piece " + piece.getId() + " from location " + L1 + " to " + L2
                    + " by " + roll + " units in " + dirStr + " direction");

            LudoBoard.getInstance().removeFromCurrentCell(piece);
            piece.setPosition(temporaryPosition);

            if (temporaryPosition == LudoBoard.HOME_STRAIGHT_LENGTH) {
                MovementManager.reachHome(piece);
            } else {
                LudoBoard.getInstance().getHomeStraight(piece.getColor())[temporaryPosition].addPiece(piece);
            }
            return false;
        }

        return checkNext(piece, roll, tryMoveAsBlock);
    }
}

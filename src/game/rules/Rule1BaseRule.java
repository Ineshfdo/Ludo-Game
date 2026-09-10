package game.rules;

import game.utils.MovementManager;
import players.components.LudoPiece;

public class Rule1BaseRule extends MovementRule {

    @Override
    public boolean handleMove(LudoPiece piece, int roll, boolean tryMoveAsBlock) {
        if (piece.getState().equals("BASE")) {
            if (roll == 6) {
                return MovementManager.moveFromBaseToStart(piece);
            }
            return false; // Cannot move out of base without a 6
        }
        return checkNext(piece, roll, tryMoveAsBlock);
    }
}

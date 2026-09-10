package game.rules;

import game.players.components.LudoPiece;
import game.utils.MovementManager;

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

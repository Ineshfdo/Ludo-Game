package game.rules;

import players.components.LudoPiece;

public abstract class MovementRule {
    protected MovementRule nextRule;

    public MovementRule setNextRule(MovementRule nextRule) {
        this.nextRule = nextRule;
        return nextRule;
    }

    public abstract boolean handleMove(LudoPiece piece, int roll, boolean tryMoveAsBlock);

    protected boolean checkNext(LudoPiece piece, int roll, boolean tryMoveAsBlock) {
        if (nextRule == null) {
            return false;
        }
        return nextRule.handleMove(piece, roll, tryMoveAsBlock);
    }
}

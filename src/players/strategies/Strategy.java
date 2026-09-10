package players.strategies;

import game.LudoBoard;
import players.components.LudoPiece;

public interface Strategy {
    boolean processMovement(LudoPiece[] pieces, int roll, LudoBoard board);
}

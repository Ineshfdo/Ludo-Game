package game.players.strategies;

import game.LudoBoard;
import game.players.components.LudoPiece;

public interface Strategy {
    boolean processMovement(LudoPiece[] pieces, int roll, LudoBoard board);
}

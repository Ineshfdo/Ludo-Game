package game.players;

import game.players.components.Player;
import game.players.components.PlayerColor;
import game.players.strategies.BlueStrategy;

public class BluePlayer extends Player {

    public BluePlayer() {
        super(PlayerColor.BLUE, new BlueStrategy());
    }

}
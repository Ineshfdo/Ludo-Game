package players;

import players.components.Player;
import players.components.PlayerColor;
import players.strategies.BlueStrategy;

public class BluePlayer extends Player {

    public BluePlayer() {
        super(PlayerColor.BLUE, new BlueStrategy());
    }

}
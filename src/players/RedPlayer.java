package players;

import players.components.Player;
import players.components.PlayerColor;
import players.strategies.RedStrategy;

public class RedPlayer extends Player {

    public RedPlayer() {
        super(PlayerColor.RED, new RedStrategy());
    }

}

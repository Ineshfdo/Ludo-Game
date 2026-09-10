package players;

import players.components.Player;
import players.components.PlayerColor;
import players.strategies.GreenStrategy;

public class GreenPlayer extends Player {

    public GreenPlayer() {
        super(PlayerColor.GREEN, new GreenStrategy());
    }

}

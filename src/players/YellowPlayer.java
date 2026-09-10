package players;

import players.components.Player;
import players.components.PlayerColor;
import players.strategies.YellowStrategy;

public class YellowPlayer extends Player {

    public YellowPlayer() {
        super(PlayerColor.YELLOW, new YellowStrategy());
    }

}

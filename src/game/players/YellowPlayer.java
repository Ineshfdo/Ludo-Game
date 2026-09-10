package game.players;

import game.players.components.Player;
import game.players.components.PlayerColor;
import game.players.strategies.YellowStrategy;

public class YellowPlayer extends Player {

    public YellowPlayer() {
        super(PlayerColor.YELLOW, new YellowStrategy());
    }

}

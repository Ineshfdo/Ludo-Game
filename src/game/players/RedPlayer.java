package game.players;

import game.players.components.Player;
import game.players.components.PlayerColor;
import game.players.strategies.RedStrategy;

public class RedPlayer extends Player {

    public RedPlayer() {
        super(PlayerColor.RED, new RedStrategy());
    }

}

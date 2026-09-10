package game.players;

import game.players.components.Player;
import game.players.components.PlayerColor;
import game.players.strategies.GreenStrategy;

public class GreenPlayer extends Player {

    public GreenPlayer() {
        super(PlayerColor.GREEN, new GreenStrategy());
    }

}

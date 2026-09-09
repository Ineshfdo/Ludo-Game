public class Main {
    public static void main(String[] args) {
        // Use the Facade to initialize and start the Ludo game
        game.GameFacade gameFacade = new game.GameFacade();
        gameFacade.startGame();
    }
}


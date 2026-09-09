
public class Main {
    public static void main(String[] args) {
        // We use the Facade to initialize and start the Ludo game simulation
        core.GameFacade gameFacade = new core.GameFacade();
        gameFacade.startGame();
    }
}


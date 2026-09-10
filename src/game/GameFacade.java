package game;

import game.players.BluePlayer;
import game.players.GreenPlayer;
import game.players.RedPlayer;
import game.players.YellowPlayer;
import game.players.components.LudoPiece;
import game.players.components.Player;
import game.players.components.PlayerColor;
import game.utils.MysteryCellManager;

public class GameFacade {
    private LudoBoard board;
    private Dice dice;
    private static Player[] players;

    public GameFacade() {
        // 1. Get Board Instance
        this.board = LudoBoard.getInstance();

        // 2. Get Dice Instance
        this.dice = Dice.getInstance();

        // 3. Create Players in exact clockwise order: Green -> Yellow -> Blue -> Red
        GameFacade.players = new Player[] {
                new GreenPlayer(),
                new YellowPlayer(),
                new BluePlayer(),
                new RedPlayer()
        };

        System.out.println("The red player has four (04) pieces named R1, R2, R3, and R4.");
        System.out.println("The blue player has four (04) pieces named B1, B2, B3, and B4.");
        System.out.println("The yellow player has four (04) pieces named Y1, Y2, Y3, and Y4.");
        System.out.println("The green player has four (04) pieces named G1, G2, G3, and G4.\n");
    }

    public static Player getPlayer(PlayerColor color) {
        if (players == null)
            return null;
        for (Player p : players) {
            if (p.getColor() == color)
                return p;
        }
        return null;
    }

    public static void printPlayerStatus(PlayerColor color) {
        Player p = getPlayer(color);
        if (p != null) {
            int onBoard = 0, inBase = 0;
            for (LudoPiece piece : p.getPieces()) {
                if (piece.getState().isBase())
                    inBase++;
                else if (piece.getState().isStandard() || piece.getState().isHomeStraight())
                    onBoard++;
            }
            String cName = color.toString().substring(0, 1).toUpperCase() + color.toString().substring(1).toLowerCase();
            System.out.println(cName + " player now has " + onBoard + "/4 on pieces on the board and " + inBase
                    + "/4 pieces on the base.");
        }
    }

    public void startGame() {
        // 1. Identify who will roll first
        int currentPlayerIndex = determineFirstPlayerIndex();

        // 2. Start the game loop
        int startingPlayerIndex = currentPlayerIndex;
        int roundsCompleted = 0;
        boolean isGameRunning = true;
        int nextRank = 1; // Track placements
        PlayerColor[] finalRankings = new PlayerColor[4]; // Track exact placements

        int firstPieceStandardPathRound = -1;
        int nextMysteryCellSpawnRound = -1;

        while (isGameRunning) {
            if (currentPlayerIndex == startingPlayerIndex) {
                System.out.println("____________________");
                System.out.println((roundsCompleted + 1) + ". Round " + (roundsCompleted + 1));
                System.out.println("____________________\n");
            }

            Player currentPlayer = players[currentPlayerIndex];

            // If the player has already finished, skip their turn
            if (currentPlayer.hasFinished()) {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.length;

                // Still need to track rounds if they were the starting player
                if (currentPlayerIndex == startingPlayerIndex) {
                    roundsCompleted++;

                    if (firstPieceStandardPathRound == -1) {
                        if (board.hasPiecesOnStandardPath()) {
                            firstPieceStandardPathRound = roundsCompleted;
                            nextMysteryCellSpawnRound = roundsCompleted + 2;
                        }
                    }
                    if (nextMysteryCellSpawnRound != -1 && roundsCompleted == nextMysteryCellSpawnRound) {
                        MysteryCellManager.spawnMysteryCell();
                        nextMysteryCellSpawnRound = roundsCompleted + 4;
                    }
                    printRoundSummary(roundsCompleted, players, nextMysteryCellSpawnRound);
                }
                continue;
            }

            System.out.println("- " + currentPlayer.getColor() + " Player's Turn -");

            // Player rolls the dice once and executes their logic
            currentPlayer.executeTurn(dice, board);

            // Check if they just finished
            if (currentPlayer.hasFinished()) {
                System.out.println("\n" + currentPlayer.getColor() + " player wins!!!");
                finalRankings[nextRank - 1] = currentPlayer.getColor();
                nextRank++;

                if (nextRank >= 4) {
                    // Automatically assign 4th place to the remaining player
                    for (Player p : players) {
                        if (!p.hasFinished()) {
                            finalRankings[3] = p.getColor();
                            break;
                        }
                    }
                    isGameRunning = false;
                    break;
                }
            }

            // After the turn, the dice passes clockwise to the player on the left-hand
            // side.
            currentPlayerIndex = (currentPlayerIndex + 1) % players.length;

            // Check if a full round has been completed (the turn comes back to the person
            // who started)
            if (currentPlayerIndex == startingPlayerIndex) {
                roundsCompleted++;

                if (firstPieceStandardPathRound == -1) {
                    if (board.hasPiecesOnStandardPath()) {
                        firstPieceStandardPathRound = roundsCompleted;
                        nextMysteryCellSpawnRound = roundsCompleted + 2;
                    }
                }
                if (nextMysteryCellSpawnRound != -1 && roundsCompleted == nextMysteryCellSpawnRound) {
                    MysteryCellManager.spawnMysteryCell();
                    nextMysteryCellSpawnRound = roundsCompleted + 4;
                }

                printRoundSummary(roundsCompleted, players, nextMysteryCellSpawnRound);

                if (roundsCompleted >= 10000) {
                    isGameRunning = false;
                }
            }
        }

        System.out.println("\n============================");
        System.out.println("FINAL RANKINGS");
        System.out.println("============================");
        System.out.println("1st Place: " + finalRankings[0] + " Player");
        System.out.println("2nd Place: " + finalRankings[1] + " Player");
        System.out.println("3rd Place: " + finalRankings[2] + " Player");
        System.out.println("4th Place: " + finalRankings[3] + " Player");
        System.out.println("============================\n");
    }

    private void printRoundSummary(int roundsCompleted, Player[] players, int nextMysteryCellSpawnRound) {
        for (Player p : players) {
            int onBoard = 0;
            int inBase = 0;
            for (LudoPiece piece : p.getPieces()) {
                if (piece.getState().isBase())
                    inBase++;
                else if (piece.getState().isStandard() || piece.getState().isHomeStraight())
                    onBoard++;
            }
            System.out.println(p.getColor() + " player now has " + onBoard + "/4 on pieces on the board and " + inBase
                    + "/4 pieces on the base.");

            System.out.println("============================");
            System.out.println("Location of pieces " + p.getColor());
            System.out.println("============================");
            for (LudoPiece piece : p.getPieces()) {
                String loc;
                if (piece.getState().isStandard()) {
                    loc = "L" + piece.getPosition();
                } else if (piece.getState().isHomeStraight()) {
                    loc = "HomePath(" + piece.getPosition() + ")";
                } else if (piece.getState().isBase()) {
                    loc = "Base";
                } else {
                    loc = "Home";
                }
                System.out.println("Piece " + piece.getId() + " -> " + loc);
            }
            int mysteryPos = MysteryCellManager.getMysteryCellPosition();
            if (mysteryPos != -1) {
                int roundsLeft = nextMysteryCellSpawnRound - roundsCompleted;
                System.out.println("The mystery cell is at L" + mysteryPos
                        + " and will be at that location for the next " + roundsLeft + " rounds.");
            }
            System.out.println();
        }
    }

    // Rolls the dice for each player to identify who will first roll.
    // Handles ties by having everyone re-roll until there's a clear highest roll.
    private int determineFirstPlayerIndex() {
        while (true) {
            int highestRoll = 0;
            int startingPlayerIndex = -1;
            boolean isTie = false;

            for (int i = 0; i < players.length; i++) {
                int roll = dice.roll();
                System.out.println("- " + players[i].getColor().toString().toLowerCase() + " rolls " + roll);

                if (roll > highestRoll) {
                    highestRoll = roll;
                    startingPlayerIndex = i;
                    isTie = false;
                } else if (roll == highestRoll) {
                    isTie = true;
                }
            }

            if (!isTie) {
                System.out.println("\n- The " + players[startingPlayerIndex].getColor().toString().toLowerCase()
                        + " player has the highest roll and will begin the game.");
                System.out.println(
                        "- After the first player takes his turn, play continues to the player \"on the left\".\n");
                return startingPlayerIndex;
            }
        }
    }
}

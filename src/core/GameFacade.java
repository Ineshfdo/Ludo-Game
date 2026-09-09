package core;

import players.BluePlayer;
import players.GreenPlayer;
import players.Player;
import players.RedPlayer;
import players.YellowPlayer;

public class GameFacade {
    private LudoBoard board;
    private Dice dice;
    private Player[] players;

    public GameFacade() {
        System.out.println("Initializing Game Facade...");
        // 1. Get Board Instance
        this.board = LudoBoard.getInstance();
        System.out.println("Board initialized.");

        // 2. Get Dice Instance
        this.dice = Dice.getInstance();
        System.out.println("Dice initialized.");

        // 3. Create Players in exact clockwise order: Green -> Yellow -> Blue -> Red
        this.players = new Player[] {
                new GreenPlayer(),
                new YellowPlayer(),
                new BluePlayer(),
                new RedPlayer()
        };
        System.out.println("Players created.");
    }

    public void startGame() {
        System.out.println("Starting the Ludo game!");

        // 1. Identify who will roll first
        int currentPlayerIndex = determineFirstPlayerIndex();

        // 2. Start the game loop
        int startingPlayerIndex = currentPlayerIndex;
        int roundsCompleted = 0;
        boolean gameRunning = true;
        int nextRank = 1; // Track placements
        players.PlayerColor[] finalRankings = new players.PlayerColor[4]; // Track exact placements

        int firstPieceStandardPathRound = -1;
        int mysteryCellNextSpawnRound = -1;

        while (gameRunning) {
            if (currentPlayerIndex == startingPlayerIndex) {
                int mysteryPos = board.getMysteryCellPosition();
                String mysteryText = mysteryPos != -1 ? " (Mystery Cell at " + mysteryPos + ")" : "";
                System.out.println("\n" + (roundsCompleted + 1) + ". Round " + (roundsCompleted + 1) + mysteryText);
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
                            mysteryCellNextSpawnRound = roundsCompleted + 2;
                        }
                    }
                    if (mysteryCellNextSpawnRound != -1 && roundsCompleted == mysteryCellNextSpawnRound) {
                        board.spawnMysteryCell();
                        mysteryCellNextSpawnRound = roundsCompleted + 4;
                    }

                    printRoundSummary(roundsCompleted, players);

                    if (roundsCompleted >= 200) {
                        System.out.println("*** 200 rounds finished. Stopping the loop for debugging purposes! ***");
                        gameRunning = false;
                    }
                }
                continue;
            }

            System.out.println("- " + currentPlayer.getColor() + " Player's Turn -");

            // Player rolls the dice once and executes their logic
            currentPlayer.executeTurn(dice, board);

            // Check if they just finished
            if (currentPlayer.hasFinished()) {
                System.out.println(
                        "\n*** " + currentPlayer.getColor() + " PLAYER HAS FINISHED IN PLACE " + nextRank + "! ***\n");
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

                    System.out.println("==================================================");
                    System.out.println("                   GAME OVER!                     ");
                    System.out.println("==================================================");
                    System.out.println("FINAL STANDINGS:");
                    System.out.println("1st Place: " + finalRankings[0]);
                    System.out.println("2nd Place: " + finalRankings[1]);
                    System.out.println("3rd Place: " + finalRankings[2]);
                    System.out.println("4th Place: " + finalRankings[3]);
                    System.out.println("==================================================");
                    gameRunning = false;
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
                        mysteryCellNextSpawnRound = roundsCompleted + 2;
                    }
                }
                if (mysteryCellNextSpawnRound != -1 && roundsCompleted == mysteryCellNextSpawnRound) {
                    board.spawnMysteryCell();
                    mysteryCellNextSpawnRound = roundsCompleted + 4;
                }

                printRoundSummary(roundsCompleted, players);

                if (roundsCompleted >= 10000) {
                    System.out.println("*** 200 rounds finished. Stopping the loop for debugging purposes! ***");
                    gameRunning = false;
                }
            }
        }
    }

    private void printRoundSummary(int roundsCompleted, Player[] players) {
        System.out.println("\n=============");
        int mysteryPos = board.getMysteryCellPosition();
        String mysteryText = mysteryPos != -1 ? " (Mystery Cell at " + mysteryPos + ")" : "";
        System.out.println("Round " + roundsCompleted + " completed!" + mysteryText);
        System.out.println("=============\n");

        // Display the current state of all pieces
        System.out.println("Round " + roundsCompleted + " Current Board State");
        System.out.println("==================");
        System.out.println("-------------------------------");
        for (Player p : players) {
            System.out.print(p.getColor() + ": ");
            java.util.Map<String, java.util.List<players.LudoPiece>> locationMap = new java.util.LinkedHashMap<>();
            for (players.LudoPiece piece : p.getPieces()) {
                String loc = piece.getState();
                if (loc.equals("STANDARD")) {
                    int pos = piece.getPosition();
                    if (pos == 2 || pos == 15 || pos == 28 || pos == 41) {
                        loc = "X(" + pos + ")";
                    } else if (pos == 0 || pos == 13 || pos == 26 || pos == 39) {
                        loc = "Approach(" + pos + ")";
                    } else {
                        loc = "Cell(" + pos + ")";
                    }
                } else if (loc.equals("HOME_STRAIGHT")) {
                    loc = "HomePath(" + piece.getPosition() + ")";
                } else if (loc.equals("BASE")) {
                    loc = "BASE";
                } else if (loc.equals("HOME")) {
                    loc = "HOME";
                }

                locationMap.putIfAbsent(loc, new java.util.ArrayList<>());
                locationMap.get(loc).add(piece);
            }

            boolean firstGroup = true;
            for (java.util.Map.Entry<String, java.util.List<players.LudoPiece>> entry : locationMap.entrySet()) {
                java.util.List<players.LudoPiece> group = entry.getValue();
                String locName = entry.getKey();

                if (!firstGroup) {
                    System.out.print("  ");
                }
                firstGroup = false;

                if (group.size() >= 2 && !locName.equals("BASE") && !locName.equals("HOME")) {
                    boolean hasClockwise = false;
                    boolean hasCounter = false;
                    for (players.LudoPiece piece : group) {
                        if (piece.isXChoiceDirectionClockwise())
                            hasClockwise = true;
                        else
                            hasCounter = true;
                    }

                    String blockName = (hasClockwise && hasCounter) ? "Opposite-Direction Block"
                            : "Same-Direction Block";
                    System.out.print("[" + blockName + ": ");
                    for (int i = 0; i < group.size(); i++) {
                        players.LudoPiece piece = group.get(i);
                        System.out.print(piece.getId() + "(" + locName + ", Caps:" + piece.getCaptures() + ")");
                        if (i < group.size() - 1)
                            System.out.print(" ");
                    }
                    System.out.print("]");
                } else {
                    for (int i = 0; i < group.size(); i++) {
                        players.LudoPiece piece = group.get(i);
                        System.out.print(piece.getId() + "(" + locName + ", Caps:" + piece.getCaptures() + ")");
                        if (i < group.size() - 1)
                            System.out.print("  ");
                    }
                }
            }
            if (p.hasFinished()) {
                System.out.print(" [FINISHED]");
            }
            System.out.println();
        }
        System.out.println("-------------------------------\n");
    }

    // Rolls the dice for each player to identify who will first roll.
    // Handles ties by having everyone re-roll until there's a clear highest roll.
    private int determineFirstPlayerIndex() {
        System.out.println("\nRolling dice to determine who goes first...");

        while (true) {
            int highestRoll = 0;
            int startingPlayerIndex = -1;
            boolean isTie = false;

            for (int i = 0; i < players.length; i++) {
                int roll = dice.roll();
                System.out.println(players[i].getColor() + " Player rolled a " + roll);

                if (roll > highestRoll) {
                    highestRoll = roll;
                    startingPlayerIndex = i;
                    isTie = false; // We have a new clear winner
                } else if (roll == highestRoll) {
                    isTie = true; // We have a tie for the highest roll
                }
            }

            if (!isTie) {
                System.out.println(players[startingPlayerIndex].getColor() + " Player won the toss with a "
                        + highestRoll + " and goes first!\n");
                return startingPlayerIndex;
            } else {
                System.out.println("There was a tie for the highest roll (" + highestRoll + ")! Everyone rerolls...\n");
            }
        }
    }
}

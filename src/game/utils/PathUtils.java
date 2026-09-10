package game.utils;

import game.LudoBoard;
import game.players.components.LudoPiece;
import game.players.components.PlayerColor;

public class PathUtils {
    public static int getDistanceToHome(LudoPiece piece) {
        if (piece.getState().equals("HOME"))
            return 0;
        if (piece.getState().equals("BASE"))
            return 1000;

        int temporaryPosition = piece.getPosition();
        String temporaryState = piece.getState();
        int temporaryPasses = piece.getApproachPasses();
        int approachIndex = getApproachIndex(piece.getColor());
        int direction = piece.isXChoiceDirectionClockwise() ? 1 : -1;

        int steps = 0;
        while (!temporaryState.equals("HOME")) {
            if (steps > 100)
                return 100; // Failsafe
            if (temporaryState.equals("STANDARD")) {
                boolean readyForHome = false;
                if (temporaryPosition == approachIndex) {
                    if (piece.isXChoiceDirectionClockwise() && temporaryPasses >= 1)
                        readyForHome = true;
                    if (!piece.isXChoiceDirectionClockwise() && temporaryPasses >= 2)
                        readyForHome = true;
                }

                if (readyForHome) {
                    temporaryState = "HOME_STRAIGHT";
                    temporaryPosition = 0;
                } else {
                    temporaryPosition = (temporaryPosition + direction + LudoBoard.STANDARD_PATH_LENGTH)
                            % LudoBoard.STANDARD_PATH_LENGTH;
                    if (temporaryPosition == approachIndex) {
                        temporaryPasses++;
                    }
                }
            } else if (temporaryState.equals("HOME_STRAIGHT")) {
                temporaryPosition++;
                if (temporaryPosition >= LudoBoard.HOME_STRAIGHT_LENGTH) {
                    temporaryState = "HOME";
                }
            }
            steps++;
        }
        return steps;
    }

    public static int getStartIndex(PlayerColor color) {
        switch (color) {
            case YELLOW:
                return 2;
            case BLUE:
                return 15;
            case RED:
                return 28;
            case GREEN:
                return 41;
            default:
                return 2;
        }
    }

    public static int getApproachIndex(PlayerColor color) {
        switch (color) {
            case YELLOW:
                return 0;
            case BLUE:
                return 13;
            case RED:
                return 26;
            case GREEN:
                return 39;
            default:
                return 0;
        }
    }
}

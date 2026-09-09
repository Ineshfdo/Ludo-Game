package game.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import game.Cell;
import game.LudoBoard;
import game.effects.AlphaEffect;
import game.effects.BetaEffect;
import game.effects.GammaEffect;
import players.components.LudoPiece;

public class MysteryCellManager {
    private static int currentMysteryCellPosition = -1;
    private static int previousMysteryCellPosition = -1;

    public static int getMysteryCellPosition() {
        return currentMysteryCellPosition;
    }

    public static void spawnMysteryCell() {
        Cell[] standardPath = LudoBoard.getInstance().getStandardPath();
        List<Integer> emptyCells = new ArrayList<>();
        for (int cellIndex = 0; cellIndex < LudoBoard.STANDARD_PATH_LENGTH; cellIndex++) {
            if (standardPath[cellIndex].getPieces().isEmpty() && cellIndex != previousMysteryCellPosition) {
                emptyCells.add(cellIndex);
            }
        }

        if (!emptyCells.isEmpty()) {
            int index = new Random().nextInt(emptyCells.size());
            currentMysteryCellPosition = emptyCells.get(index);
            previousMysteryCellPosition = currentMysteryCellPosition;
            System.out.println("\n*** A Mystery Cell has appeared at Cell(" + currentMysteryCellPosition + ")! ***\n");
        }
    }

    public static boolean triggerMysteryCellEffect(LudoPiece piece) {
        int option = new Random().nextInt(6) + 1;
        System.out.println("  -> Mystery Cell activates Option " + option + "!");

        switch (option) {
            case 1:
                System.out.println("  -> Teleporting to Alpha (Cell 9)");
                piece.setPosition(9);
                boolean didCapture = CaptureManager.handleStandardCellLanding(piece, 9, true);
                AlphaEffect.applyIndividual(piece);
                return didCapture;
            case 2:
                System.out.println("  -> Teleporting to Beta (Cell 27)");
                piece.setPosition(27);
                BetaEffect.applyIndividual(piece);
                return CaptureManager.handleStandardCellLanding(piece, 27, true);
            case 3:
                int optionChoice = GammaEffect.determineTeleportOption(piece.isXChoiceDirectionClockwise());
                if (optionChoice == 3) {
                    piece.setPosition(46);
                    GammaEffect.applyIndividual(piece);
                    return CaptureManager.handleStandardCellLanding(piece, 46, true);
                } else {
                    piece.setPosition(27);
                    BetaEffect.applyIndividual(piece);
                    return CaptureManager.handleStandardCellLanding(piece, 27, true);
                }
            case 4:
                System.out.println("  -> Teleporting to BASE!");
                piece.resetToDefault();
                return false;
            case 5:
                int startCellIndex = PathUtils.getStartIndex(piece.getColor());
                System.out
                        .println("  -> Teleporting to X (" + piece.getColor() + " start cell " + startCellIndex + ")");
                piece.setPosition(startCellIndex);
                piece.setApproachPasses(0);
                return CaptureManager.handleStandardCellLanding(piece, startCellIndex, true);
            case 6:
                int approachCellIndex = PathUtils.getApproachIndex(piece.getColor());
                System.out.println("  -> Teleporting to Approach (" + piece.getColor() + " approach cell "
                        + approachCellIndex + ")");
                piece.setPosition(approachCellIndex);
                return CaptureManager.handleStandardCellLanding(piece, approachCellIndex, true);
            default:
                return false;
        }
    }

    public static void triggerMysteryCellBlockTeleport(List<LudoPiece> block) {
        System.out.println("  -> The Block landed on the Mystery Cell!");
        int option = new Random().nextInt(6) + 1;
        System.out.println("  -> Mystery Cell activates Option " + option + " for the entire block!");

        Cell[] standardPath = LudoBoard.getInstance().getStandardPath();

        if (option == 4) {
            System.out.println("  -> Teleporting Block to BASE!");
            for (LudoPiece currentPiece : block) {
                if (currentPiece.getState().equals("STANDARD")) {
                    standardPath[currentPiece.getPosition()].removePiece(currentPiece);
                    currentPiece.resetToDefault();
                }
            }
            return;
        }

        LudoPiece dominantPiece = block.get(0);
        int minDistance = Integer.MAX_VALUE;
        for (LudoPiece currentPiece : block) {
            int distanceToHome = PathUtils.getDistanceToHome(currentPiece);
            if (distanceToHome < minDistance) {
                minDistance = distanceToHome;
                dominantPiece = currentPiece;
            }
        }
        boolean isBlockClockwise = dominantPiece.isXChoiceDirectionClockwise();

        if (option == 3) {
            option = GammaEffect.determineTeleportOption(isBlockClockwise);
            if (option == 3) {
                GammaEffect.applyBlock(block);
            }
        }

        int destinationPosition = -1;
        switch (option) {
            case 1:
                destinationPosition = 9;
                break;
            case 2:
                destinationPosition = 27;
                break;
            case 3:
                destinationPosition = 46;
                break;
            case 5:
                destinationPosition = PathUtils.getStartIndex(block.get(0).getColor());
                break;
            case 6:
                destinationPosition = PathUtils.getApproachIndex(block.get(0).getColor());
                break;
        }

        System.out.println("  -> Teleporting Block to Cell " + destinationPosition);

        for (LudoPiece currentPiece : block) {
            if (currentPiece.getState().equals("STANDARD")) {
                standardPath[currentPiece.getPosition()].removePiece(currentPiece);
                currentPiece.setPosition(destinationPosition);
                if (option == 5)
                    currentPiece.setApproachPasses(0);
                standardPath[destinationPosition].addPiece(currentPiece);
            }
        }

        if (option == 1) {
            AlphaEffect.applyBlock(block);
        } else if (option == 2) {
            BetaEffect.applyBlock(block);
        }

        Cell destinationCell = standardPath[destinationPosition];
        List<LudoPiece> opponentPieces = new ArrayList<>();
        for (LudoPiece currentPiece : destinationCell.getPieces()) {
            if (currentPiece.getColor() != block.get(0).getColor()) {
                opponentPieces.add(currentPiece);
            }
        }
        if (!opponentPieces.isEmpty()) {
            for (LudoPiece opponentPiece : opponentPieces) {
                opponentPiece.resetToDefault();
                destinationCell.removePiece(opponentPiece);
                System.out.println("  -> CAPTURE! Teleported Block captured " + opponentPiece.getId() + "!");
            }
            for (LudoPiece currentPiece : block) {
                currentPiece.incrementCaptures();
            }
        }
    }
}

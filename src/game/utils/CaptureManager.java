package game.utils;

import java.util.ArrayList;
import java.util.List;

import game.Cell;
import game.LudoBoard;
import players.components.LudoPiece;

public class CaptureManager {

    public static boolean handleStandardCellLanding(LudoPiece piece, int newPos) {
        return handleStandardCellLanding(piece, newPos, false);
    }

    public static boolean handleStandardCellLanding(LudoPiece piece, int newPos, boolean ignoreMysteryCell) {
        int currentMysteryCellPosition = MysteryCellManager.getMysteryCellPosition();
        if (!ignoreMysteryCell && newPos == currentMysteryCellPosition && currentMysteryCellPosition != -1) {
            System.out.println("  -> Piece " + piece.getId() + " landed on the Mystery Cell!");
            return MysteryCellManager.triggerMysteryCellEffect(piece);
        }

        boolean captured = false;
        Cell destinationCell = LudoBoard.getInstance().getStandardPath()[newPos];

        List<LudoPiece> piecesOnCell = new ArrayList<>(destinationCell.getPieces());
        for (LudoPiece otherPiece : piecesOnCell) {
            if (otherPiece.getColor() != piece.getColor()) {
                // Capture!
                otherPiece.resetToDefault();
                destinationCell.removePiece(otherPiece);
                System.out.println("  -> CAPTURE! " + piece.getId() + " captured " + otherPiece.getId() + "!");
                captured = true;
                piece.incrementCaptures(); // Increment captures for the attacking piece
            }
        }

        destinationCell.addPiece(piece);
        return captured;
    }
}

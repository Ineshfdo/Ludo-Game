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
        java.util.Map<players.components.PlayerColor, List<String>> capturedMap = new java.util.HashMap<>();
        
        for (LudoPiece otherPiece : piecesOnCell) {
            if (otherPiece.getColor() != piece.getColor()) {
                // Capture!
                otherPiece.resetToDefault();
                destinationCell.removePiece(otherPiece);
                
                capturedMap.putIfAbsent(otherPiece.getColor(), new ArrayList<>());
                capturedMap.get(otherPiece.getColor()).add(otherPiece.getId());
                
                captured = true;
                piece.incrementCaptures(); // Increment captures for the attacking piece
            }
        }

        if (captured) {
            String cName = piece.getColor().toString().substring(0, 1).toUpperCase() + piece.getColor().toString().substring(1).toLowerCase();
            System.out.println(cName + " piece " + piece.getId() + " lands on square L" + newPos);
            
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (java.util.Map.Entry<players.components.PlayerColor, List<String>> entry : capturedMap.entrySet()) {
                if (!first) sb.append(" and ");
                String capName = entry.getKey().toString().substring(0, 1).toUpperCase() + entry.getKey().toString().substring(1).toLowerCase();
                sb.append(capName).append(" pieces [").append(String.join(", ", entry.getValue())).append("]");
                first = false;
            }
            
            System.out.println(cName + " piece " + piece.getId() + " captures " + sb.toString());
            game.GameFacade.printPlayerStatus(piece.getColor());
        }

        destinationCell.addPiece(piece);
        return captured;
    }
}

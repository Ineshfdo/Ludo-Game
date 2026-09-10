package game.rules;

import game.Cell;
import game.LudoBoard;
import game.players.components.LudoPiece;
import game.utils.MovementManager;

import java.util.ArrayList;
import java.util.List;

public class Rule2BlockMoveRule extends MovementRule {

    @Override
    public boolean handleMove(LudoPiece piece, int roll, boolean tryMoveAsBlock) {
        if (piece.getState().isStandard() && tryMoveAsBlock) {
            Cell currentCell = LudoBoard.getInstance().getStandardPath()[piece.getPosition()];
            List<LudoPiece> piecesOnCell = currentCell.getPieces();

            boolean hasClockwisePiece = false;
            boolean hasCounterClockwisePiece = false;
            List<LudoPiece> blockPieces = new ArrayList<>();

            for (LudoPiece currentPiece : piecesOnCell) {
                if (currentPiece.getColor() == piece.getColor() && currentPiece.getState().isStandard()) {
                    blockPieces.add(currentPiece);
                    if (currentPiece.isXChoiceDirectionClockwise())
                        hasClockwisePiece = true;
                    else
                        hasCounterClockwisePiece = true;
                }
            }

            if (blockPieces.size() >= 2) {
                if (hasClockwisePiece && hasCounterClockwisePiece) {
                    return MovementManager.moveOppositeBlock(blockPieces, roll);
                } else {
                    return MovementManager.moveSameWayBlock(blockPieces, roll);
                }
            }
        }

        return checkNext(piece, roll, tryMoveAsBlock);
    }
}

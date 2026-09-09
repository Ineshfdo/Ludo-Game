import re

with open('src/game/LudoBoard.java', 'r') as f:
    content = f.read()

content = content.replace('for (int cellIndex = 0; cellIndex < STANDARD_PATH_LENGTH; stepIndex++)', 'for (int cellIndex = 0; cellIndex < STANDARD_PATH_LENGTH; cellIndex++)')
content = content.replace('for (int cellIndex = 0; cellIndex < HOME_STRAIGHT_LENGTH; stepIndex++)', 'for (int cellIndex = 0; cellIndex < HOME_STRAIGHT_LENGTH; cellIndex++)')

content = re.sub(r'boolean isStart = \(i == (\d+) \|\| i == (\d+) \|\| i == (\d+) \|\| i == (\d+)\);', r'boolean isStart = (cellIndex == \1 || cellIndex == \2 || cellIndex == \3 || cellIndex == \4);', content)
content = re.sub(r'boolean isApproach = \(i == (\d+) \|\| i == (\d+) \|\| i == (\d+) \|\| i == (\d+)\);', r'boolean isApproach = (cellIndex == \1 || cellIndex == \2 || cellIndex == \3 || cellIndex == \4);', content)
content = content.replace('String cellId = String.valueOf(i);', 'String cellId = String.valueOf(cellIndex);')
content = content.replace('standardPath[cellIndex] = new Cell(cellId, i, (PlayerColor) null, isStart, isApproach);', 'standardPath[cellIndex] = new Cell(cellId, cellIndex, (PlayerColor) null, isStart, isApproach);')

content = content.replace('String cellId = colorPrefix + \"home path\" + i;', 'String cellId = colorPrefix + \"home path\" + cellIndex;')
content = content.replace('straight[cellIndex] = new Cell(cellId, i, color, false, false);', 'straight[cellIndex] = new Cell(cellId, cellIndex, color, false, false);')

content = content.replace('System.out.println(\"  -> Breaking blockade at cell \" + i);', 'System.out.println(\"  -> Breaking blockade at cell \" + cellIndex);')

content = content.replace('if (standardPath[cellIndex].getPieces().isEmpty() && i != previousMysteryCellPosition) {', 'if (standardPath[cellIndex].getPieces().isEmpty() && cellIndex != previousMysteryCellPosition) {')
content = content.replace('emptyCells.add(i);', 'emptyCells.add(cellIndex);')

content = content.replace('for (int j = 1; j < playerPieces.size(); j++)', 'for (int pieceIndex = 1; pieceIndex < playerPieces.size(); pieceIndex++)')
content = content.replace('LudoPiece movingPiece = playerPieces.get(j);', 'LudoPiece movingPiece = playerPieces.get(pieceIndex);')

with open('src/game/LudoBoard.java', 'w') as f:
    f.write(content)

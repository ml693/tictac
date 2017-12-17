package tictac;

import java.util.ArrayList;

class Position {
	private static final int FIVE = 5;
	
	// Indicates what's placed on the square.
	public static final int EMPTY = 0;
	public static final int X = 1;
	public static final int O = -1;

	public static final int WHITE_WON = 0;
	public static final int BLACK_WON = 1;
	public static final int DRAW = 2;
	// The game is still going on.
	public static final int UNKNOWN = 3;
	
	public static final int WIDTH = 25;
	public static final int HEIGHT = 25;
	int board[][] = new int[WIDTH][HEIGHT];

	Position() {}

	Position(Position position) {
		for (int row = 0; row < HEIGHT; row++) {
			for (int col = 0; col < WIDTH; col++) {
				board[row][col] = position.board[row][col];
			}
		}
	}

	// White plays with x pieces, black plays with o pieces.
	int gameResult() {
		boolean gameContinues = false;
		for (int row = 0; row < HEIGHT; row++) {
			for (int col = 0; col < WIDTH; col++) {
				int piece = board[row][col];
				if (piece == EMPTY) {
					gameContinues = true;
				} else {
					int direction[][] = {{0, 1}, {1, 1}, {1, 0}, {1, -1}};
					for (int d = 0; d < direction.length; d++) {
						for (int i = 0; i <= 5; i++) {
							if (i == 5) {
								return piece == Position.X ? 
									WHITE_WON : BLACK_WON;
							}
							if (row + direction[d][0] * i < 0 ||
						            row + direction[d][0] * i >= HEIGHT) {
								break;
							}
							if (col + direction[d][1] * i < 0 ||
         						    col + direction[d][1] * i >= WIDTH) {
								break;
							}
							if (board[row + direction[d][0] * i][col + direction[d][1] * i] != piece) {
								break;
							}
						}
					}
				}
			}
		}
		return gameContinues ? UNKNOWN : DRAW;
	}

	boolean whiteMovedLast() {
		int xCount = 0;
		int oCount = 0;
		for (int row = 0; row < HEIGHT; row++) {
			for (int col = 0; col < WIDTH; col++) {
				if (board[row][col] == Position.X) {
					xCount++;
				}
				if (board[row][col] == Position.O) {
					oCount++;
				}
			}
		}
		return xCount > oCount;
	}

	ArrayList<Position> nextPositions() {
		ArrayList<Position> nextPositions = new ArrayList<Position>();
		int gameResult = gameResult();
		if (gameResult == UNKNOWN) {
			boolean whiteMovedLast = whiteMovedLast();
			for (int row = 0; row < HEIGHT; row++) {
				for (int col = 0; col < WIDTH; col++) {
					if (board[row][col] == EMPTY) {
						Position newPosition = new Position(this);
						if (whiteMovedLast) {
							newPosition.board[row][col] = Position.O;
						} else {
							newPosition.board[row][col] = Position.X;
						}
						nextPositions.add(newPosition);
					}
				}
			}
		}
		return nextPositions;
	}
}

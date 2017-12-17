package tictac;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

class Computer {
	private double[][] weights = new double[Position.HEIGHT][Position.WIDTH];

	private static final double MAX_REWARD = 1000000.0;

	private static final double LAMBDA = 0.98;

	private static final double RANDOM_MOVE_PROBABILITY = 0.1;

	private static final int TRAINING_AMOUNT = 10;
	
	private Random random = new Random();

	Computer(String fileName) {
		try {
		    Scanner scanner = new Scanner(new File(fileName));
		    int i = 0;
		    while (scanner.hasNextInt()) {
			weights[i / Position.WIDTH][i % Position.WIDTH] = 
				scanner.nextInt();
			i++;
		    }
		} catch (Exception exception) { System.out.println("Exception!"); }
	}

	Computer() {
		this("./tictac/NeuralNetwork.txt");
	}

	public double evaluate(Position position) {
		Position adjustedPosition = new Position(position);
		if (!position.whiteMovedLast()) {
			for (int row = 0; row < Position.HEIGHT; row++) {
				for (int col = 0; col < Position.WIDTH; col++) {
					if (position.board[row][col] == Position.X) {
						adjustedPosition.board[row][col] = Position.O;
					}
					if (position.board[row][col] == Position.O) {
						adjustedPosition.board[row][col] = Position.X;
					}		
				}
			}
		}
		
		int gameResult = adjustedPosition.gameResult();
		if (gameResult == Position.WHITE_WON) {
			return MAX_REWARD;
		}
		if (gameResult == Position.BLACK_WON) {
			return -MAX_REWARD;
		}
		double reward = 0;
		for (int row = 0; row < Position.HEIGHT; row++) {
			for (int col = 0; col < Position.WIDTH; col++) {
			    reward += weights[row][col] * ((double) position.board[row][col]);
			}
		}
		return reward;
	}

	public Position makeBestMove(Position currentPosition) {
		ArrayList<Position> positions = currentPosition.nextPositions();
		Position nextPosition = null;
		double bestScore = -MAX_REWARD;
		for (Position position : positions) {
			double score = evaluate(position);
			if (score > bestScore) {
				nextPosition = position;
				bestScore = score;
			}
		}
		if (nextPosition == null) {
			throw new RuntimeException("Should be a valid move");
		} else {
			return nextPosition;
		}
	}

	public Position makeRandomMove(Position currentPosition) {
		ArrayList<Position> nextPositions = currentPosition.nextPositions();
		return nextPositions.get(random.nextInt(nextPositions.size()));
	}

	private void adjustWeights(double nextEvaluation, double currentEvaluation,
				   ArrayList<Position> history) {
		double lambdaToI = 1;
		double diff = nextEvaluation - currentEvaluation;
		for (int i = 0; i < history.size(); i++) {
			for (int row = 0; row < Position.HEIGHT; row++) {
				for (int col = 0; col < Position.WIDTH; col++) {
					weights[row][col] += diff * lambdaToI * 
						history.get(history.size() - 1 - i).board[row][col];
				}
			}
			lambdaToI *= LAMBDA;
		}
	}

	private void printWeightsToFile(String fileName) {
		try {
			File file = new File(fileName);
			FileWriter writer = new FileWriter(file);
			for (int row = 0; row < Position.HEIGHT; row++) {
				for (int col = 0; col < Position.WIDTH; col++) {
					writer.write(weights[row][col] + "\n");
				}
			}
			writer.close();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	// Call to train the model
	public static void main(String args[]) {
		for (int i = 0; i < TRAINING_AMOUNT; i++) {
			Computer white = new Computer("./tictac/newWeights.txt");
			Computer black = new Computer("./tictac/newWeights.txt");
			ArrayList<Position> history = new ArrayList<Position>();
			Position position = new Position();
			while (position.gameResult() == Position.UNKNOWN) {
				history.add(position);
				int randomNumber = white.random.nextInt(100);
				position = (randomNumber >= 10) ? white.makeBestMove(position) :
					white.makeRandomMove(position);
				double currentEvaluation = white.evaluate(position);
				double nextEvaluation;
				if (position.gameResult() == Position.WHITE_WON) {
					nextEvaluation = MAX_REWARD;
				} else {
					position = black.makeBestMove(position);
					if (position.gameResult() == Position.BLACK_WON) {
						nextEvaluation = -MAX_REWARD;
					} else {
						nextEvaluation = white.evaluate(position);
					} 
				}
				white.adjustWeights(nextEvaluation, currentEvaluation, history);
			}
			white.printWeightsToFile("./tictac/newWeights" + i);
		}
	}
}

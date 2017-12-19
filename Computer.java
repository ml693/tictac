package tictac;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

class Computer
{
    private double[][] weights = new double[Position.HEIGHT][Position.WIDTH];

    private static final double WIN_REWARD = 1.0;
    private static final double LOSS_REWARD = -1.0;
    private static final double DRAW_REWARD = 0.0;

    private static final double LAMBDA = 0.98;

    private static final double RANDOM_MOVE_PROBABILITY = 0.1;

    private static final int TRAINING_AMOUNT = 10;
    
    Random random = new Random();

    Computer(String fileName)
    {
        try {
            Scanner scanner = new Scanner(new File(fileName));
            for (int row = 0; row < Position.HEIGHT; row++) {
		for (int col = 0; col < Position.WIDTH; col++) {
			weights[row][col] = scanner.nextDouble();
		}
	    }
	    scanner.close();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    Computer()
    {
        this("./tictac/NeuralNetwork.txt");
    }

    private static double functionF(double x) {
        return x > 0 ? x + 1 : 1 / (1 - x);
    }

    private static double fDerivative(double x) {
        return x > 0 ? 1 : 1 / (1 - x) * (1 - x);
    }

    public double evaluate(Position position)
    {
        Position adjustedPosition = new Position(position);
        if (!position.whiteMovedLast()) {
	    // Swapping black and white pieces
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
	switch (adjustedPosition.gameResult()) {
		case Position.WHITE_WON:
			return WIN_REWARD;
		case Position.BLACK_WON:
			return LOSS_REWARD;
		case Position.DRAW:
			return DRAW_REWARD;
		case Position.UNKNOWN:
			return 1 / (1 + functionF(dotProduct(weights, adjustedPosition)));
		default: throw new RuntimeException("Unexpected game result");
	}
    }

    private static double dotProduct(double[][] weights, Position position) {
        double product = 0;
        for (int row = 0; row < Position.HEIGHT; row++) {
            for (int col = 0; col < Position.WIDTH; col++) {
                product += weights[row][col] * ((double) position.board[row][col]);
            }
        }
	return product;
    }

    public Position makeBestMove(Position currentPosition)
    {
        ArrayList<Position> positions = currentPosition.nextPositions();
        Position nextPosition = null;
        double bestScore = LOSS_REWARD;
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

    public Position makeRandomMove(Position currentPosition)
    {
        ArrayList<Position> nextPositions = currentPosition.nextPositions();
        return nextPositions.get(random.nextInt(nextPositions.size()));
    }

    private void adjustWeights(double nextEvaluation, double currentEvaluation,
                               ArrayList<Position> history)
    {
        double lambdaToI = 1;
        double diff = nextEvaluation - currentEvaluation;
        for (int i = 0; i < history.size(); i++) {
	    int k = history.size() - 1 - i;
            for (int row = 0; row < Position.HEIGHT; row++) {
                for (int col = 0; col < Position.WIDTH; col++) {
		    double product = dotProduct(weights, history.get(k));
		    double factor = -1 * fDerivative(product) / ((1 + functionF(product)) * 
                                                                (1 + functionF(product)));
                    weights[row][col] += diff * lambdaToI * factor * history.get(k).board[row][col];
                }
            }	
            lambdaToI *= LAMBDA;
        }
    }

    private void printWeightsToFile(String fileName)
    {
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
    public static void main(String args[])
    {
        for (int i = 0; i < TRAINING_AMOUNT; i++) {
            System.out.println("Iteration nr. " + i);

	    Computer white = new Computer("./tictac/newWeights.txt");
            Computer black = new Computer("./tictac/newWeights.txt");
            ArrayList<Position> history = new ArrayList<Position>();
            Position position = new Position();

            while (position.gameResult() == Position.UNKNOWN) {
                history.add(position);
                double currentEvaluation = white.evaluate(position);
		int randomNumber = white.random.nextInt(100);
                position = (randomNumber >= 10) ? white.makeBestMove(position) :
                           white.makeRandomMove(position);
                double nextEvaluation;
                if (position.gameResult() == Position.WHITE_WON) {
                    nextEvaluation = WIN_REWARD;
                } else {
                    position = black.makeBestMove(position);
                    if (position.gameResult() == Position.BLACK_WON) {
                        nextEvaluation = LOSS_REWARD;
                    } else {
                        nextEvaluation = white.evaluate(position);
                    }
                }
                white.adjustWeights(nextEvaluation, currentEvaluation, history);
            }
            white.printWeightsToFile("./tictac/newWeights.txt");
        }
    }
}

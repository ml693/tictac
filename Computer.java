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
    private static final double WIN_REWARD = 1.0;
    private static final double DRAW_REWARD = 1.0;
    private static final double LOSS_REWARD = 0.0;

    private static final double LAMBDA = 0.98;

    private static final double RANDOM_MOVE_PROBABILITY = 0.1;

    private static final int TRAINING_AMOUNT = 1000;

    Random random = new Random();

    NeuralNetwork neuralNetwork;

    Computer(File readParametersFrom)
    {
        neuralNetwork = new NeuralNetwork(readParametersFrom);
    }

    Computer()
    {
        this(new File("./tictac/NeuralNetwork.txt"));
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

        switch (adjustedPosition.gameResult()) {
        case Position.WHITE_WON:
            return WIN_REWARD;
        case Position.BLACK_WON:
            return LOSS_REWARD;
        case Position.DRAW:
            return DRAW_REWARD;
        case Position.UNKNOWN:
            return neuralNetwork.apply(adjustedPosition);
        default: throw new RuntimeException("Unexpected game result");
        }
    }

    public Position makeBestMove(Position currentPosition)
    {
        ArrayList<Position> positions = currentPosition.nextPositions();
        Position nextPosition = positions.get(0);
        double bestScore = LOSS_REWARD;
        for (Position position : positions) {
            double score = evaluate(position);
            if (score > bestScore) {
                nextPosition = position;
                bestScore = score;
            }
        }
        return nextPosition;
    }

    public Position makeRandomMove(Position currentPosition)
    {
        ArrayList<Position> nextPositions = currentPosition.nextPositions();
        return nextPositions.get(random.nextInt(nextPositions.size()));
    }

    // Call to train the model
    public static void main(String args[])
    {
        for (int i = 0; i < TRAINING_AMOUNT; i++) {
            System.out.println("Iteration nr. " + i);

            Computer white = new Computer(new File("./tictac/newWeights.txt"));
            Computer black = new Computer(new File("./tictac/newWeights.txt"));
            ArrayList<Position> history = new ArrayList<Position>();
            Position position = new Position();

            while (position.gameResult() == Position.UNKNOWN) {
                history.add(position);
                double currentEvaluation = white.evaluate(position);
                position = (white.random.nextInt(100) > 10) ? white.makeBestMove(position) :
                           white.makeRandomMove(position);
                double nextEvaluation;
                if (position.gameResult() == Position.WHITE_WON) {
                    nextEvaluation = WIN_REWARD;
                } else {
                    position = black.makeBestMove(position);
                    nextEvaluation = white.evaluate(position);
                }
                white.neuralNetwork.adjustWeights(LAMBDA, nextEvaluation - currentEvaluation, history);
            }

            white.neuralNetwork.parametersToFile(new File("./tictac/newWeights.txt"));
        }
    }
}

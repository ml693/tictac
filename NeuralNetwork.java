package tictac;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class NeuralNetwork
{
    private static final int HIDDEN_LAYER_SIZE = 30;

    private class Parameters {
        private double[][] firstLayer =
            new double[HIDDEN_LAYER_SIZE][Position.HEIGHT * Position.WIDTH];
        private double[] bias = new double[HIDDEN_LAYER_SIZE];
        private double[] hiddenLayer = new double[HIDDEN_LAYER_SIZE];
        private double hiddenBias = 1.0;

        Parameters() {}
        Parameters(File file) {
            try {
            Scanner scanner = new Scanner(file);
            for (int h = 0; h < HIDDEN_LAYER_SIZE; h++) {
                for (int i = 0; i < Position.HEIGHT * Position.WIDTH; i++) {
                    firstLayer[h][i] = scanner.nextDouble();
                }
            }
            
            for (int h = 0; h < HIDDEN_LAYER_SIZE; h++) {
                    bias[h] = scanner.nextDouble();
            }
            for (int h = 0; h < HIDDEN_LAYER_SIZE; h++) {
                    hiddenLayer[h] = scanner.nextDouble();
            }
            hiddenBias = scanner.nextDouble();
            scanner.close(); 
            } catch (FileNotFoundException exception) {
                throw new RuntimeException(exception);
            }
        
        }

        void toFile(File file) {
            try {
                FileWriter writer = new FileWriter(file);

                for (int h = 0; h < HIDDEN_LAYER_SIZE; h++) {
                    for (int i = 0; i < Position.HEIGHT * Position.WIDTH; i++) {
                        writer.write(firstLayer[h][i] + "\n");
                    }
                }
                for (int h = 0; h < HIDDEN_LAYER_SIZE; h++) {
                    writer.write(bias[h] + "\n");
                }

                for (int h = 0; h < HIDDEN_LAYER_SIZE; h++) {
                    writer.write(hiddenLayer[h] + "\n");
                }
                writer.write(hiddenBias + "\n");

                writer.close();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        void multiply(double d) {
            for (int i = 0; i < HIDDEN_LAYER_SIZE; i++) {
                for (int j = 0; j < Position.HEIGHT * Position.WIDTH; j++) {
                    firstLayer[i][j] *= d;
                }
                bias[i] *= d;
                hiddenLayer[i] *= d;
            }
            hiddenBias *= d;
        }

        void add(Parameters p) {
            for (int i = 0; i < HIDDEN_LAYER_SIZE; i++) {
                for (int j = 0; j < Position.HEIGHT * Position.WIDTH; j++) {
                    firstLayer[i][j] += p.firstLayer[i][j];
                }
                bias[i] += p.bias[i];
                hiddenLayer[i] += p.hiddenLayer[i];
            }
            hiddenBias += p.hiddenBias;
        }
    };

    Parameters parameters;

    public NeuralNetwork(File parametersFile) {
        parameters = new Parameters(parametersFile);
    }

    void parametersToFile(File file) {
       parameters.toFile(file);
    }

    private static double activation(double x) {
        return x > 0 ? -1 / (x + 2) + 1 : 1 / (2 - x);
    }
    private static double activationDerivative(double x) {
        return x > 0 ? 1 / ((x + 2) * (x + 2)) : 1 / ((2 - x) * (2 - x));
    }

    private static double dotProduct(double[] vector1, double[] vector2) {
        double product = 0;
        for (double d1 : vector1) {
            for (double d2 : vector2) {
                product += d1 * d2;
            }
        }
        return product;
    }

    double apply(Position position)
    {
        double board[] = new double[Position.HEIGHT * Position.WIDTH];
        for (int row = 0; row < Position.HEIGHT; row++) {
            for (int col = 0; col < Position.WIDTH; col++) {
                board[row * Position.WIDTH + col] = ((double) position.board[row][col]);
            }
        }

        double[] hiddenLayer = new double[HIDDEN_LAYER_SIZE];
        for (int h = 0; h < HIDDEN_LAYER_SIZE; h++) {
            hiddenLayer[h] = activation(dotProduct(parameters.firstLayer[h], board) + 
                parameters.bias[h]);
        }

        return activation(dotProduct(parameters.hiddenLayer, parameters.hiddenLayer) + 
            parameters.hiddenBias);
    }

    private double[] getBoard(Position position) {
        double[] board = new double[Position.WIDTH * Position.HEIGHT];
        for (int i = 0; i < board.length; i++) {
            board[i] = ((double) position.board[i / Position.WIDTH][i % Position.WIDTH]);
        }
        return board;
    }

    Parameters computeGradient(Position position) {
        Parameters gradient = new Parameters();
        gradient.hiddenBias = apply(position);

        for (int i = 0; i < NeuralNetwork.HIDDEN_LAYER_SIZE; i++) {
           gradient.hiddenLayer[i] = gradient.hiddenBias * 
                activation(dotProduct(parameters.firstLayer[i], getBoard(position)) + 
                           gradient.bias[i]);
           for (int j = 0; j < Position.WIDTH * Position.HEIGHT; j++) {
               gradient.firstLayer[i][j] = gradient.hiddenLayer[i] * 
                   position.board[j / Position.WIDTH][j % Position.WIDTH];
           }
        }

        return gradient;
    }

    void adjustWeights(double lambda, double evaluationDifference,
        ArrayList<Position> history) {
        double lambdaToI = 1;
        for (int i = 0; i < history.size(); i++) {
            int k = history.size() - 1 - i;
            Parameters gradient = computeGradient(history.get(k));
            gradient.multiply(lambdaToI * evaluationDifference);
            parameters.add(gradient);
            lambdaToI *= lambda;
        }
    }
}

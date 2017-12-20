package tictac;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.FileWriter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

class Play
{
    private static Computer computer = new Computer();
    private static Position position = new Position();
    private static JButton[][] buttons = new
    JButton[Position.HEIGHT][Position.WIDTH];

    public static void colorBoard(Position position)
    {
        for (int row = 0; row < Position.HEIGHT; row++) {
            for (int col = 0; col < Position.WIDTH; col++) {
                if (position.board[row][col] == Position.X) {
                    ImageIcon xIcon = new ImageIcon("./tictac/x.png");
                    buttons[row][col].setIcon(xIcon);
                }
                if (position.board[row][col] == Position.O) {
                    ImageIcon oIcon = new ImageIcon("./tictac/o.jpg");
                    buttons[row][col].setIcon(oIcon);
                }
            }
        }
    }


    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Let's play!");
        frame.setSize(800, 800);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(Position.HEIGHT, Position.WIDTH));
        for (int r = 0; r < Position.HEIGHT; r++) {
            for (int c = 0; c < Position.WIDTH; c++) {
                final int row = r;
                final int col = c;
                JButton button = new JButton();
                buttons[row][col] = button;
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (position.board[row][col] == Position.EMPTY) {
                            position.board[row][col] = Position.X;
                            colorBoard(position);

                            int gameResult = position.gameResult();
                            if (gameResult == Position.WHITE_WON) {
                                System.out.println("You won");
                                return;
                            }
                            if (gameResult == Position.DRAW) {
                                System.out.println("Draw");
                                return;
                            }
                            position = computer.makeBestMove(position);
                            colorBoard(position);
                            if (position.gameResult() == Position.BLACK_WON) {
                                System.out.println("I won");
                                return;
                            }
                        }
                    }
                });
                panel.add(button);
            }
        }
        frame.getContentPane().add(panel);
    }
}
